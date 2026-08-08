package com.shop.auth.controller;

import com.shop.common.exception.BusinessException;
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
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int MAX_FAIL_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    public static boolean isBlacklisted(RedisUtil redisUtil, String token) {
        return redisUtil.hasKey(TOKEN_BLACKLIST_PREFIX + token);
    }

    @Operation(summary = "用户登录-用户名密码")
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request,
                                           HttpServletRequest servletRequest) {
        checkLoginLock(request.getUsername());
        request.setIp(getClientIp(servletRequest));
        Result<UserLoginResponse> result = userServiceClient.login(request);
        if (result.getCode() == 200) {
            clearLoginFailures(request.getUsername());
            log.info("用户登录成功: username={}", request.getUsername());
        } else {
            recordLoginFailure(request.getUsername());
        }
        return result;
    }

    @Operation(summary = "用户登录-手机号验证码")
    @PostMapping("/login/phone")
    public Result<UserLoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request,
                                                  HttpServletRequest servletRequest) {
        checkLoginLock(request.getPhone());
        request.setIp(getClientIp(servletRequest));
        Result<UserLoginResponse> result = userServiceClient.loginByPhone(request);
        if (result.getCode() == 200) {
            clearLoginFailures(request.getPhone());
            log.info("手机号登录成功: phone={}", request.getPhone());
        } else {
            recordLoginFailure(request.getPhone());
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
        checkLoginLock(request.getEmail());
        request.setIp(getClientIp(servletRequest));
        Result<UserLoginResponse> result = userServiceClient.loginByEmail(request);
        if (result.getCode() == 200) {
            clearLoginFailures(request.getEmail());
            log.info("邮箱登录成功: email={}", request.getEmail());
        } else {
            recordLoginFailure(request.getEmail());
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
    public Result<UserLoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. 验证 refreshToken 有效性
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error(400, "refreshToken不能为空");
        }
        if (!jwtUtil.validateToken(refreshToken)) {
            return Result.error(401, "refreshToken无效或已过期");
        }

        // 2. 验证是否是 refreshToken（不是 access token）
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            return Result.error(400, "传入的不是refreshToken");
        }

        // 3. 检查 refreshToken 是否在黑名单中
        if (isBlacklisted(redisUtil, refreshToken)) {
            return Result.error(401, "refreshToken已被注销");
        }

        // 4. 从 refreshToken 中提取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        if (userId == null) {
            return Result.error(401, "refreshToken中无有效用户信息");
        }

        // 5. 生成新的 access token 和 refresh token
        String newAccessToken = jwtUtil.generateToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        // 6. 将旧的 refresh token 加入黑名单
        long remainingMs = jwtUtil.getExpirationDateFromToken(refreshToken).getTime() - System.currentTimeMillis();
        if (remainingMs > 0) {
            redisUtil.set(TOKEN_BLACKLIST_PREFIX + refreshToken, "1", remainingMs, TimeUnit.MILLISECONDS);
        }

        // 7. 返回新的 token 对
        UserLoginResponse response = new UserLoginResponse();
        response.setToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
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
     * 检查登录是否被锁定
     */
    private void checkLoginLock(String identifier) {
        Object attempts = redisUtil.get(LOGIN_FAIL_PREFIX + identifier);
        if (attempts != null && Integer.parseInt(attempts.toString()) >= MAX_FAIL_ATTEMPTS) {
            throw new BusinessException("登录失败次数过多，请" + LOCK_DURATION_MINUTES + "分钟后再试");
        }
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailure(String identifier) {
        String key = LOGIN_FAIL_PREFIX + identifier;
        Long count = redisUtil.increment(key);
        if (count != null && count == 1) {
            redisUtil.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFailures(String identifier) {
        redisUtil.delete(LOGIN_FAIL_PREFIX + identifier);
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
