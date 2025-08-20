package io.github.xzy.novel.core.aspect;

import io.github.xzy.novel.core.annotation.Key;
import io.github.xzy.novel.core.annotation.Lock;
import io.github.xzy.novel.core.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 切面
 *
 * @author xiongxiaoyang
 * @date 2022/6/20
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LockAspect {
    
    private final RedissonClient redissonClient;

    private static final String KEY_PREFIX = "Lock";

    private static final String KEY_SEPARATOR = "::";

    // 该切面方法绑定在所有Lock注解的方法上，类型为Around
    @Around(value = "@annotation(io.github.xzy.novel.core.annotation.Lock)")
    @SneakyThrows
    public Object doAround(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        Lock lock = targetMethod.getAnnotation(Lock.class);
        // 构建唯一锁ID
        String lockKey = KEY_PREFIX + buildLockKey(lock.prefix(), targetMethod,
            joinPoint.getArgs());
        // 使用redisson的RLock构建分布式锁
        RLock rLock = redissonClient.getLock(lockKey);
        // 如果无法立即获取锁，则等待指定时间（lock注解的属性waitTime，默认3秒）
        // 如果可以立即获取锁，则直接执行目标方法
        if (lock.isWait() ? rLock.tryLock(lock.waitTime(), TimeUnit.SECONDS) : rLock.tryLock()) {
            try {
                return joinPoint.proceed();
            } finally {
                rLock.unlock();
            }
        }
        // 规定时间内无法获取锁，则抛出异常
        throw new BusinessException(lock.failCode());
    }

    // 基于Lock注解的prefix、所绑定方法的方法名、参数列表，构建唯一的锁ID
    // 确保分布式环境下，同一时间只有一个线程可以访问该方法
    private String buildLockKey(String prefix, Method method, Object[] args) {
        StringBuilder builder = new StringBuilder();
        // 允许prefix为空
        if (StringUtils.hasText(prefix)) {
            builder.append(KEY_SEPARATOR).append(prefix);
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            builder.append(KEY_SEPARATOR);
            if (parameters[i].isAnnotationPresent(Key.class)) {
                Key key = parameters[i].getAnnotation(Key.class);
                builder.append(parseKeyExpr(key.expr(), args[i]));
            }
        }
        return builder.toString();
    }

    private String parseKeyExpr(String expr, Object arg) {
        if (!StringUtils.hasText(expr)) {
            return arg.toString();
        }
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(expr, new TemplateParserContext());
        return expression.getValue(arg, String.class);
    }

}
