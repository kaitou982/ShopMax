package com.shop.user.controller;

import com.shop.common.web.Result;
import com.shop.user.controller.request.PhoneLoginRequest;
import com.shop.user.controller.request.SendSmsCodeRequest;
import com.shop.user.controller.request.StoreApplyRequest;
import com.shop.user.controller.request.UserLoginRequest;
import com.shop.user.controller.request.UserRegisterRequest;
import com.shop.user.controller.request.WxLoginRequest;
import com.shop.user.service.SmsService;
import com.shop.user.controller.response.UserLoginResponse;
import com.shop.user.controller.response.UserRegisterResponse;
import com.shop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @author shop
 * @since 2026-05-11
 */
@Slf4j
@Tag(name = "认证管理")
@RestController("userAuthController")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SmsService smsService;

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

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = userService.register(request);
        log.info("用户注册成功: username={}", response.getUsername());
        return Result.success(response);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute("userId") Long userId) {
        log.info("用户登出: userId={}", userId);
        return Result.success();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<UserLoginResponse> refreshToken(@RequestAttribute("userId") Long userId) {
        log.info("刷新Token: userId={}", userId);
        return Result.success(new UserLoginResponse());
    }

    @Operation(summary = "获取邀请信息")
    @GetMapping("/referral")
    public Result<Map<String, Object>> getReferralInfo(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getReferralInfo(userId));
    }

    @Operation(summary = "申请成为店家")
    @PostMapping("/store/apply")
    public Result<Void> applyStore(@RequestAttribute("userId") Long userId,
                                   @Valid @RequestBody StoreApplyRequest request) {
        userService.applyStore(userId, request);
        log.info("用户申请入驻: userId={}, storeName={}", userId, request.getStoreName());
        return Result.success();
    }

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
