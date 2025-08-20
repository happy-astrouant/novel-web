package io.github.xzy.novel.core.aspect;

import io.github.xzy.novel.core.annotation.ValidateSortOrder;
import io.github.xzy.novel.core.common.req.PageReqDto;
import io.github.xzy.novel.core.common.util.SortWhitelistUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 排序字段和排序方式的安全校验切面类
 * <p>
 * 该切面用于拦截所有 Mapper 方法的调用，对带有 @ValidateSortOrder 注解的参数进行统一处理，
 * 校验并清理其中的排序字段（sort）和排序方式（order）参数，防止 SQL 注入攻击。
 * <p>
 * 支持处理以下类型的参数：
 * - PageReqDto 类型对象
 * - Map 类型参数
 * - 任意带有 sort/order 字段的 POJO 对象
 *
 * @author xiongxiaoyang
 * @date 2025/7/17
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SortOrderValidationAspect {

    // 缓存：记录哪些方法需要校验，避免重复反射
    private static final ConcurrentHashMap<Method, Boolean> validationRequiredCache = new ConcurrentHashMap<>();

    /**
     * 拦截所有 Mapper 方法的调用，检查参数中是否包含 @ValidateSortOrder 注解。
     * 如果有，则对参数中的 sort 和 order 字段进行安全校验和清理。
     */
    @SneakyThrows
    @Around("execution(* io.github.xzy.*.dao.mapper.*Mapper.*(..))")
    public Object validateSortOrder(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 1. 使用缓存判断是否需要处理（首次计算，后续直接命中）
        if (!shouldValidate(method)) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();

        boolean modified = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && hasValidateSortOrderAnnotation(method, i)) {
                try {
                    // 执行参数清洗与校验
                    handleAnnotatedParameter(args[i]);
//                    if (cleaned != args[i]) {
//                        args[i] = cleaned;
//                        modified = true;
//                    }
                } catch (IllegalArgumentException e) {
                    // 记录非法请求
                    log.warn("非法排序参数拦截 - 方法:{} 参数值:{}", method.toGenericString(), args[i], e);
//                    incrementIllegalRequestCounter();
                    throw e;
                }
            }
        }

        // 2. 执行原方法
        // 继续执行原方法
        return joinPoint.proceed(args);
    }

    // 缓存驱动：判断某方法是否需要校验
    private boolean shouldValidate(Method method) {
        return validationRequiredCache.computeIfAbsent(method, m -> {
            // 只要有一个参数带有 @ValidateSortOrder 就需要校验
            Annotation[][] paramAnns = m.getParameterAnnotations();
            for (Annotation[] paramAnn : paramAnns) {
                for (Annotation ann : paramAnn) {
                    if (ann.annotationType().equals(ValidateSortOrder.class)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    // 缓存参数注解检查（也可进一步缓存到二维数组）
    private boolean hasValidateSortOrderAnnotation(Method method, int paramIndex) {
        Annotation[][] paramAnns = method.getParameterAnnotations();
        if (paramIndex >= paramAnns.length) return false;
        return Arrays.stream(paramAnns[paramIndex])
                .anyMatch(a -> a.annotationType().equals(ValidateSortOrder.class));
    }


    /**
     * 根据参数类型，分别处理不同形式的 sort/order 字段。
     * 导航方法
     */
    @SneakyThrows
    private void handleAnnotatedParameter(Object obj) {
        if (obj instanceof PageReqDto dto) {
            processPageReqDto(dto);
        } else if (obj instanceof Map<?, ?> map) {
            processMap(map);
        } else {
            processGenericObject(obj);
        }
    }

    /**
     * 处理 PageReqDto 类型参数中的 sort 和 order 字段。
     */
    private void processPageReqDto(PageReqDto dto) {
        if (dto.getSort() != null) {
            dto.setSort(SortWhitelistUtil.sanitizeColumn(dto.getSort()));
        }
        if (dto.getOrder() != null) {
            dto.setOrder(SortWhitelistUtil.sanitizeOrder(dto.getOrder()));
        }
    }

    /**
     * 处理 Map 类型参数中的 sort 和 order 字段。
     */
    private void processMap(Map map) {
        if (map.get("sort") instanceof String sortStr) {
            map.put("sort", SortWhitelistUtil.sanitizeColumn(sortStr));
        }
        if (map.get("order") instanceof String orderStr) {
            map.put("order", SortWhitelistUtil.sanitizeOrder(orderStr));
        }
    }

    /**
     * 使用反射处理任意对象中的 sort 和 order 字段。
     * 支持任何带有这两个字段的 POJO。
     */
    @SneakyThrows
    private void processGenericObject(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            switch (field.getName()) {
                case "sort", "order" -> {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof String strValue) {
                        String sanitized = "sort".equals(field.getName())
                            ? SortWhitelistUtil.sanitizeColumn(strValue)
                            : SortWhitelistUtil.sanitizeOrder(strValue);
                        field.set(obj, sanitized);
                    }
                }
                default -> {
                    // 忽略其他字段
                }
            }
        }
    }
}