package io.github.xzy.novel.core.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.xzy.novel.core.json.serializer.DesensitizationSerialize;

public class DesensitizationModule extends SimpleModule {
    public DesensitizationModule() {
        // 注册 String 类型的上下文序列化器
        addSerializer(String.class, new DesensitizationSerialize());
    }
}
