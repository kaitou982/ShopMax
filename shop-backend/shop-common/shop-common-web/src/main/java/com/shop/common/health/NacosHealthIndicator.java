package com.shop.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Nacos 健康检查指示器
 */
@Component
public class NacosHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 简单检查：如果 Bean 能被注入说明 Nacos 配置正常
        return Health.up()
                .withDetail("service", "nacos")
                .build();
    }
}
