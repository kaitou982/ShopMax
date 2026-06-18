package com.shop.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.web.Result;
import com.shop.common.util.SlideWindowRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关限流过滤器
 *
 * @author shop
 * @since 2026-06-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final SlideWindowRateLimiter redisRateLimiter;
    private final ObjectMapper objectMapper;

    /**
     * IP 每秒最大请求数
     */
    private static final int IP_RATE_LIMIT = 10;

    /**
     * 用户每秒最大请求数
     */
    private static final int USER_RATE_LIMIT = 5;

    /**
     * 秒杀接口每秒最大请求数
     */
    private static final int SECKILL_RATE_LIMIT = 3;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 只对秒杀接口进行限流
        if (!path.contains("/seckill/execute")) {
            return chain.filter(exchange);
        }

        // 获取客户端 IP
        String clientIp = getClientIp(request);

        // IP 限流
        String ipKey = "rate:ip:" + clientIp;
        if (!redisRateLimiter.isAllowed(ipKey, 1, IP_RATE_LIMIT)) {
            log.warn("IP 限流触发: ip={}", clientIp);
            return writeErrorResponse(exchange, "请求过于频繁，请稍后再试");
        }

        // 获取用户 ID（如果已登录）
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            // 用户限流
            String userKey = "rate:user:" + userId;
            if (!redisRateLimiter.isAllowed(userKey, 1, USER_RATE_LIMIT)) {
                log.warn("用户限流触发: userId={}", userId);
                return writeErrorResponse(exchange, "请求过于频繁，请稍后再试");
            }
        }

        // 秒杀接口单独限流
        String seckillKey = "rate:seckill:" + clientIp;
        if (!redisRateLimiter.isAllowed(seckillKey, 1, SECKILL_RATE_LIMIT)) {
            log.warn("秒杀接口限流触发: ip={}", clientIp);
            return writeErrorResponse(exchange, "秒杀请求过于频繁，请稍后再试");
        }

        return chain.filter(exchange);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个 IP，取第一个
            return ip.split(",")[0].trim();
        }

        ip = request.getHeaders().getFirst("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeaders().getFirst("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddress() != null ?
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 写入错误响应
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error(429, message);
        String body;
        try {
            body = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            body = "{\"code\":429,\"message\":\"" + message + "\"}";
        }

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -200; // 限流在认证之前执行（AuthGlobalFilter order=-100）
    }
}
