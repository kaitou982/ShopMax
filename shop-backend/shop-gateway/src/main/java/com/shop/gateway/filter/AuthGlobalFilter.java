package com.shop.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关全局认证过滤器
 *
 * @author shop
 * @since 2026-05-13
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * 白名单路径（前缀匹配）— 无需认证直接放行
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/sms/send",
            "/api/v1/auth/email/send-code",
            "/api/v1/auth/check-email",
            "/api/v1/auth/reset-password"
    );

    /**
     * 公开路径前缀（无需认证直接放行）
     * 注意：只放行纯公开路径，混合路径（既有公开GET又有管理接口）不放行，
     * 由下游服务的 @PreAuthorize 和 SecurityConfig 分别处理鉴权。
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/swagger-ui", "/v3/api-docs", "/swagger-resources",
            "/webjars", "/actuator", "/health", "/ws",
            "/api/v1/files/default",
            "/api/v1/banners",
            "/api/v1/payments/callback",
            "/api/v1/search/hot",
            "/api/v1/search/suggest",
            "/api/v1/products",
            "/api/v1/categories",
            "/api/v1/brands",
            "/api/v1/marketing/seckill",
            "/api/v1/marketing/group-buy",
            "/api/v1/marketing/coupons",
            "/api/v1/marketing/promotions",
            "/api/v1/live",
            "/api/v1/community",
            "/api/v1/cs/faqs",
            "/api/v1/logistics"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // CORS 预检请求直接放行，不进行认证
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // 公开路径直接放行
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 白名单路径直接放行（使用 startsWith 前缀匹配）
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 提取 Token
        String token = extractToken(exchange.getRequest());
        if (token == null || token.isEmpty()) {
            return unauthorized(exchange, "未提供认证Token");
        }

        // 验证 Token
        if (!validateToken(token)) {
            return unauthorized(exchange, "Token无效或已过期");
        }

        // 将 userId 和 role 写入请求头传递给下游服务
        Long userId = getUserIdFromToken(token);
        if (userId != null) {
            String userType = getUserTypeFromToken(token);
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Role", userType != null ? userType : "USER")
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 回退：从请求参数中获取
        String tokenParam = request.getQueryParams().getFirst("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        return null;
    }

    /**
     * 验证 Token 有效性
     */
    private boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中提取 userId
     */
    private Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            Object userId = claims.get("userId");
            if (userId instanceof Integer intVal) {
                return intVal.longValue();
            }
            if (userId instanceof Long longVal) {
                return longVal;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中提取 userType
     */
    private String getUserTypeFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            Object userType = claims.get("userType");
            if (userType instanceof String s) {
                return s;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 JWT Token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取签名密钥（与各业务服务使用相同的密钥逻辑）
     */
    private SecretKey getSigningKey() {
        StringBuilder sb = new StringBuilder(secret);
        while (sb.length() < 32) {
            sb.append("shopmax");
        }
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8)));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 返回 401 未授权响应（含 CORS 头，确保浏览器能读取响应）
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                exchange.getRequest().getHeaders().getOrigin());
        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
