package com.shop.common.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shop.common.security.RoleConstants;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 *
 * @author shop
 * @since 2026-04-19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 不需要验证Token的路径（仅匿名端点）
     */
    private static final String[] SKIP_PATHS = {
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/actuator",
            "/health",
            "/ws",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/sms/send",
            "/api/v1/auth/email/send-code",
            "/api/v1/auth/check-email",
            "/api/v1/auth/reset-password",
            "/api/v1/files/default",
            "/api/v1/banners",
            "/api/v1/payments/callback",
            "/api/v1/search/hot",
            "/api/v1/search/suggest",
            "/api/v1/live/srs"  // SRS 流媒体回调（无需认证）
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.debug("JWT过滤器处理请求: {}", requestUri);

        // 跳过不需要验证的路径
        for (String path : SKIP_PATHS) {
            if (requestUri.startsWith(path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 获取Token
        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            // 验证Token
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                if (userId != null) {
                    log.debug("Token验证成功, 用户ID: {}, 用户名: {}", userId, username);

                    String userType = jwtUtil.getUserTypeFromToken(token);

                    // 将用户信息存入request属性，供后续使用
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    request.setAttribute("userType", userType);

                    // 根据角色设置 Spring Security 权限
                    var authorities = buildAuthorities(userType);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                log.warn("Token验证失败: {}", token.substring(0, Math.min(token.length(), 20)));
            }
        } else {
            log.debug("请求未携带Token: {}", requestUri);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 从Header中获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 从请求参数中获取（用于某些特殊情况，如WebSocket）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    /**
     * 根据用户角色构建 Spring Security 权限列表
     */
    private List<org.springframework.security.core.authority.SimpleGrantedAuthority> buildAuthorities(String userType) {
        String role = switch (userType) {
            case RoleConstants.ADMIN -> RoleConstants.ROLE_ADMIN;
            case RoleConstants.STORE -> RoleConstants.ROLE_STORE;
            default -> RoleConstants.ROLE_USER;
        };
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));
    }
}
