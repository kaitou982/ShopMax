package com.shop.user.controller;

import com.shop.common.web.Result;
import com.shop.user.controller.request.*;
import com.shop.user.controller.response.*;
import com.shop.user.service.EmailService;
import com.shop.user.service.SmsService;
import com.shop.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务内部接口（供其他微服务通过 Feign 调用，不经过 Gateway 路由）
 *
 * @author shop
 * @since 2026-06-17
 */
@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;
    private final SmsService smsService;
    private final EmailService emailService;

    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/login/phone")
    public Result<UserLoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        return Result.success(userService.loginByPhone(request));
    }

    @PostMapping("/login/wx")
    public Result<UserLoginResponse> loginByWx(@Valid @RequestBody WxLoginRequest request) {
        return Result.success(userService.loginByWx(request));
    }

    @PostMapping("/login/email")
    public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request) {
        return Result.success(userService.loginByEmail(request));
    }

    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @GetMapping("/check-email")
    public Result<Boolean> existsByEmail(@RequestParam String email) {
        return Result.success(userService.existsByEmail(email));
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success();
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        smsService.sendCode(request.getPhone(), request.getType());
        return Result.success();
    }

    @PostMapping("/email/send-code")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        emailService.sendCode(request.getEmail(), request.getType());
        return Result.success();
    }

    @PostMapping("/store/apply")
    public Result<Void> applyStore(@RequestHeader("X-User-Id") Long userId,
                                   @Valid @RequestBody StoreApplyRequest request) {
        userService.applyStore(userId, request);
        return Result.success();
    }
}
