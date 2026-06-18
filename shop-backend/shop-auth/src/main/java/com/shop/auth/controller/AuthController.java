package com.shop.auth.controller;

import com.shop.common.feign.client.UserServiceClient;
import com.shop.common.feign.dto.user.*;
import com.shop.common.redis.RedisUtil;
import com.shop.common.security.jwt.JwtUtil;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 *
 * @author shop
 * @since 2026-04-19
 */
@Slf4j
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    public static boolean isBlacklisted(RedisUtil redisUtil, String token) {
        return redisUtil.hasKey(TOKEN_BLACKLIST_PREFIX + token);
    }

    @Operation(summary = "用户登录-用户名密码")
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request,
                                           HttpServletRequest servletRequest) {
        request.setIp(getClientIp(servletRequest));
        Result<UserLoginResponse> result = userServiceClient.login(request);
        if (result.getCode() == 200) {
            log.info("用户登录成功: username={}", request.getUsername());
        }
        return result;
    }

    @Operation(summary = "用户登录-手机号验证码")
    @PostMapping("/login/phone")
    public Result<UserLoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request,
                                                  HttpServletRequest servletRequest) {
        request.setIp(getClientIp(servletRequest));
        Result<UserLoginResponse> result = userServiceClient.loginByPhone(request);
        if (result.getCode() == 200) {
            log.info("手机号登录成功: phone={}", request.getPhone());
        }
        return result;
    }

    @Operation(summary = "用户登录-微信授权")
    @PostMapping("/login/wx")
    public Result<UserLoginResponse> loginByWx(@Valid @RequestBody WxLoginRequest request,
                                               HttpServletRequest servletRequest) {
        request.setIp(getClientIp(servletRequest));
        Result<UserLoginResponse> result = userServiceClient.loginByWx(request);
        if (result.getCode() == 200) {
            log.info("微信登录成功: openid={}", request.getOpenid());
        }
        return result;
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        Result<Void> result = userServiceClient.sendSmsCode(request);
        if (result.getCode() == 200) {
            log.info("验证码发送成功: phone={}, type={}", request.getPhone(), request.getType());
        }
        return result;
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email/send-code")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        Result<Void> result = userServiceClient.sendEmailCode(request);
        if (result.getCode() == 200) {
            log.info("邮箱验证码发送成功: email={}, type={}", request.getEmail(), request.getType());
        }
        return result;
    }

    @Operation(summary = "用户登录-邮箱验证码")
    @PostMapping("/login/email")
    public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request,
                                                  HttpServletRequest servletRequest) {
        request.setIp(getClientIp(servletRequest));
        Result<UserLoginResponse> result = userServiceClient.loginByEmail(request);
        if (result.getCode() == 200) {
            log.info("邮箱登录成功: email={}", request.getEmail());
        }
        return result;
    }

    @Operation(summary = "检查邮箱是否已注册")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam @jakarta.validation.constraints.Email String email) {
        return userServiceClient.existsByEmail(email);
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Result<Void> result = userServiceClient.resetPassword(request);
        if (result.getCode() == 200) {
            log.info("重置密码成功: email={}", request.getEmail());
        }
        return result;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        Result<UserRegisterResponse> result = userServiceClient.register(request);
        if (result.getCode() == 200 && result.getData() != null) {
            log.info("用户注册成功: username={}", result.getData().getUsername());
        }
        return result;
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute("userId") Long userId, HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token != null) {
            redisUtil.set(TOKEN_BLACKLIST_PREFIX + token, "1", jwtExpiration, TimeUnit.MILLISECONDS);
            log.info("Token已加入黑名单: userId={}", userId);
        }
        return Result.success();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<UserLoginResponse> refreshToken(@RequestAttribute("userId") Long userId) {
        String newToken = jwtUtil.generateToken(userId);
        UserLoginResponse response = new UserLoginResponse();
        response.setToken(newToken);
        log.info("Token刷新成功: userId={}", userId);
        return Result.success(response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @Operation(summary = "申请成为店家")
    @PostMapping("/store/apply")
    public Result<Void> applyStore(@RequestAttribute("userId") Long userId,
                                   @Valid @RequestBody StoreApplyRequest request) {
        Result<Void> result = userServiceClient.applyStore(userId, request);
        if (result.getCode() == 200) {
            log.info("用户申请入驻: userId={}, storeName={}", userId, request.getStoreName());
        }
        return result;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
