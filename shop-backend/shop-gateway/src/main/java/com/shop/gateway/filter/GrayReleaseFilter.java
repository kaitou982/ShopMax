package com.shop.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

/**
 * 灰度发布过滤器
 * <p>
 * 支持两种灰度策略：
 * 1. 请求头 X-Gray-Version 指定灰度版本
 * 2. 按百分比随机分配灰度流量
 *
 * @author shop
 * @since 2026-06-23
 */
@Slf4j
@Component
public class GrayReleaseFilter implements GlobalFilter, Ordered {

    private static final String GRAY_HEADER = "X-Gray-Version";
    private static final String GRAY_USER_HEADER = "X-Gray-User-Id";

    @Value("${gray.release.enabled:false}")
    private boolean grayEnabled;

    @Value("${gray.release.version:}")
    private String grayVersion;

    @Value("${gray.release.percentage:0}")
    private int grayPercentage;

    @Value("${gray.release.user-ids:}")
    private List<String> grayUserIds;

    private final Random random = new Random();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!grayEnabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 跳过内部接口和健康检查
        if (path.startsWith("/internal") || path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        // 策略1: 请求头指定灰度版本
        String headerVersion = request.getHeaders().getFirst(GRAY_HEADER);
        if (headerVersion != null && !headerVersion.isEmpty()) {
            log.debug("灰度路由 - 请求头指定版本: {}, 路径: {}", headerVersion, path);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Version", headerVersion)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // 策略2: 指定用户ID灰度
        String userId = request.getHeaders().getFirst(GRAY_USER_HEADER);
        if (userId != null && grayUserIds.contains(userId)) {
            log.debug("灰度路由 - 用户ID命中灰度: {}, 路径: {}", userId, path);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Version", grayVersion)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // 策略3: 按百分比随机分配
        if (grayPercentage > 0 && random.nextInt(100) < grayPercentage) {
            log.debug("灰度路由 - 随机命中灰度 ({}%), 路径: {}", grayPercentage, path);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Version", grayVersion)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50; // 在认证过滤器之后执行
    }
}
