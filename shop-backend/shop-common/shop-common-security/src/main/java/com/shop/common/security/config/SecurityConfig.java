package com.shop.common.security.config;

import com.shop.common.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Spring Security 配置类
 *
 * @author shop
 * @since 2026-04-15
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("初始化 Spring Security 配置...");

        // 纯公开路径（所有方法均放行：认证入口、静态资源、文档、回调）
        final String[] FULLY_PUBLIC = {
                "/internal/**",  // 内部 Feign 调用（不经过 Gateway）
                "/api/v1/auth/login/**",
                "/api/v1/auth/register",
                "/api/v1/auth/sms/send",
                "/api/v1/auth/email/send-code",
                "/api/v1/auth/check-email",
                "/api/v1/auth/reset-password",
                "/api/v1/files/default/**",
                "/api/v1/banners/**",
                "/api/v1/payments/callback/**",
                "/api/v1/search/hot",
                "/api/v1/search/suggest",
                "/api/v1/live/srs/**",  // SRS 流媒体回调（无需认证）
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/actuator/**",
                "/health",
                "/ws/**",
        };

        // 混合路径（GET 放行供游客浏览，POST/PUT/DELETE 走认证供 @PreAuthorize 鉴权）
        final String[] PUBLIC_GET = {
                "/api/v1/products/**",
                "/api/v1/categories/**",
                "/api/v1/brands/**",
                "/api/v1/marketing/seckill/**",
                "/api/v1/marketing/group-buy/**",
                "/api/v1/marketing/coupons/**",
                "/api/v1/marketing/promotions/**",
                "/api/v1/live/**",
                "/api/v1/community/**",
                "/api/v1/cs/faqs/**",
                "/api/v1/logistics/**",
        };

        http
                // 禁用 CSRF（前后端分离项目不需要）
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 由 Gateway 统一处理，服务层禁用 CORS 避免重复头/跨域拒绝
                .cors(AbstractHttpConfigurer::disable)

                // 配置会话管理 - 无状态（使用 JWT）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(FULLY_PUBLIC).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                        .anyRequest().authenticated()
                )

                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 认证失败处理器
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.warn("认证失败: {}", authException.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\",\"timestamp\":"
                    + System.currentTimeMillis() + "}");
        };
    }

    /**
     * 权限不足处理器
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("权限不足: {}", accessDeniedException.getMessage());
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"timestamp\":"
                    + System.currentTimeMillis() + "}");
        };
    }
}
