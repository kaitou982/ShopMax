package com.shop.auth.controller;

import com.shop.common.redis.RedisUtil;
import com.shop.common.security.jwt.JwtUtil;
import com.shop.common.web.Result;
import com.shop.user.controller.request.UserLoginRequest;
import com.shop.user.controller.request.PhoneLoginRequest;
import com.shop.user.controller.request.SendSmsCodeRequest;
import com.shop.user.controller.request.SendEmailCodeRequest;
import com.shop.user.controller.request.EmailLoginRequest;
import com.shop.user.controller.request.ResetPasswordRequest;
import com.shop.user.controller.request.StoreApplyRequest;
import com.shop.user.controller.request.WxLoginRequest;
import com.shop.user.controller.request.UserRegisterRequest;
import com.shop.user.service.SmsService;
import com.shop.user.service.EmailService;
import com.shop.user.controller.response.UserLoginResponse;
import com.shop.user.controller.response.UserRegisterResponse;
import com.shop.user.service.UserService;
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

    private final UserService userService;
    private final SmsService smsService;
    private final EmailService emailService;
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
        UserLoginResponse response = userService.login(request);
        log.info("用户登录成功: username={}", request.getUsername());
        return Result.success(response);
    }

    @Operation(summary = "用户登录-手机号验证码")
    @PostMapping("/login/phone")
    public Result<UserLoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request,
                                                  HttpServletRequest servletRequest) {
        request.setIp(getClientIp(servletRequest));
        UserLoginResponse response = userService.loginByPhone(request);
        log.info("手机号登录成功: phone={}", request.getPhone());
        return Result.success(response);
    }

    @Operation(summary = "用户登录-微信授权")
    @PostMapping("/login/wx")
    public Result<UserLoginResponse> loginByWx(@Valid @RequestBody WxLoginRequest request,
                                               HttpServletRequest servletRequest) {
        request.setIp(getClientIp(servletRequest));
        UserLoginResponse response = userService.loginByWx(request);
        log.info("微信登录成功: openid={}", request.getOpenid());
        return Result.success(response);
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        smsService.sendCode(request.getPhone(), request.getType());
        log.info("验证码发送成功: phone={}, type={}", request.getPhone(), request.getType());
        return Result.success();
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email/send-code")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        emailService.sendCode(request.getEmail(), request.getType());
        log.info("邮箱验证码发送成功: email={}, type={}", request.getEmail(), request.getType());
        return Result.success();
    }

    @Operation(summary = "用户登录-邮箱验证码")
    @PostMapping("/login/email")
    public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request,
                                                  HttpServletRequest servletRequest) {
        request.setIp(getClientIp(servletRequest));
        UserLoginResponse response = userService.loginByEmail(request);
        log.info("邮箱登录成功: email={}", request.getEmail());
        return Result.success(response);
    }

    @Operation(summary = "检查邮箱是否已注册")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam @jakarta.validation.constraints.Email String email) {
        return Result.success(userService.existsByEmail(email));
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        log.info("重置密码成功: email={}", request.getEmail());
        return Result.success();
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = userService.register(request);
        log.info("用户注册成功: username={}", response.getUsername());
        return Result.success(response);
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
        userService.applyStore(userId, request);
        log.info("用户申请入驻: userId={}, storeName={}", userId, request.getStoreName());
        return Result.success();
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
        // 多个代理情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}