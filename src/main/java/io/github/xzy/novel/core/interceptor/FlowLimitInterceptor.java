package io.github.xzy.novel.core.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xzy.novel.core.common.constant.ErrorCodeEnum;
import io.github.xzy.novel.core.common.constant.FlowMode;
import io.github.xzy.novel.core.common.resp.RestResp;
import io.github.xzy.novel.core.common.util.IpUtils;
import io.github.xzy.novel.core.properties.FlowLimitProperties;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 流量限制 拦截器：实现接口防刷和限流
 *
 * @author xiongxiaoyang
 * @date 2022/6/1
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(FlowLimitProperties.class)
@Slf4j
public class FlowLimitInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final FlowLimitProperties flowLimitProperties;

    /**
     * novel 项目所有的资源
     */
    private static final String NOVEL_RESOURCE = "novelResource";

    @PostConstruct
    public void init() {
        if (!flowLimitProperties.isEnabled()) {
            log.info("限流功能已关闭");
            return;
        }

        // 全局限流规则
        FlowRule rule1 = new FlowRule();
        rule1.setResource(NOVEL_RESOURCE);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setCount(flowLimitProperties.getQps());

        // 根据配置选择控制行为
        if (flowLimitProperties.getMode() == FlowMode.RATE_LIMITER) {
            rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
            // 排队最长 500ms
            rule1.setMaxQueueingTimeMs(500);
        } else if (flowLimitProperties.getMode() == FlowMode.TOKEN_BUCKET) {
            // 快速失败
            rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        }

        FlowRuleManager.loadRules(Collections.singletonList(rule1));

        // IP 级防刷规则（每秒）
        ParamFlowRule rule2 = new ParamFlowRule(NOVEL_RESOURCE)
                .setParamIdx(0)
                .setCount(flowLimitProperties.getIpQps())
                .setDurationInSec(1);

        // IP 级防刷规则（每分钟）
        ParamFlowRule rule3 = new ParamFlowRule(NOVEL_RESOURCE)
                .setParamIdx(0)
                .setCount(flowLimitProperties.getIpMinuteLimit())
                .setDurationInSec(60);

        ParamFlowRuleManager.loadRules(Arrays.asList(rule2, rule3));

        log.info("限流规则已加载：模式={}, QPS={}", flowLimitProperties.getMode(), flowLimitProperties.getQps());
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        String ip = IpUtils.getRealIp(request);
        Entry entry = null;
        try {
            // 若需要配置例外项，则传入的参数只支持基本类型。
            // EntryType 代表流量类型，其中系统规则只对 IN 类型的埋点生效
            // count 大多数情况都填 1，代表统计为一次调用。
            entry = SphU.entry(NOVEL_RESOURCE, EntryType.IN, 1, ip);
            // Your logic here.
            return HandlerInterceptor.super.preHandle(request, response, handler);
        } catch (BlockException ex) {
            // Handle request rejection.
            log.info("IP:{}被限流了！", ip);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter()
                .write(objectMapper.writeValueAsString(RestResp.fail(ErrorCodeEnum.USER_REQ_MANY)));
        } finally {
            // 注意：exit 的时候也一定要带上对应的参数，否则可能会有统计错误。
            if (entry != null) {
                entry.exit(1, ip);
            }
        }
        return false;
    }

}
