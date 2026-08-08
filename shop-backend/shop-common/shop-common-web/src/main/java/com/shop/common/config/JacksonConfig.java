package com.shop.common.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Jackson 序列化配置
 * 将 Long/long 类型序列化为 String，避免 JavaScript 精度丢失
 * 注意：分页字段（total, pages, current, size）保持 Long 类型
 *
 * @author shop
 * @since 2026-07-22
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Order(Integer.MIN_VALUE) // 确保最高优先级
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 将 Long 类型序列化为 String，避免 JavaScript 精度丢失
            // 注意：这会影响所有 Long 字段，包括 PageResult 中的分页字段
            // 前端需要将分页字段转为数字类型
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
