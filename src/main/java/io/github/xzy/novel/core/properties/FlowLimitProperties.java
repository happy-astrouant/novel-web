package io.github.xzy.novel.core.properties;

import io.github.xzy.novel.core.common.constant.FlowMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "novel.flow-limit")
@Data
public class FlowLimitProperties {
    private boolean enabled = true;
    private FlowMode mode = FlowMode.TOKEN_BUCKET;
    private double qps = 2000;
    private double ipQps = 50;
    private int ipMinuteLimit = 1000;
}
