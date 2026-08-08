package com.shop.common.feign.client;

import com.shop.common.feign.dto.user.*;
import com.shop.common.feign.fallback.UserServiceClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务 FeignClient（供 shop-auth 调用）
 *
 * @author shop
 * @since 2026-06-17
 */
@FeignClient(name = "shop-user-service", contextId = "userServiceClient",
             path = "/internal/users", fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {

    @PostMapping("/login")
    Result<UserLoginResponse> login(@RequestBody UserLoginRequest request);

    @PostMapping("/login/phone")
    Result<UserLoginResponse> loginByPhone(@RequestBody PhoneLoginRequest request);

    @PostMapping("/login/wx")
    Result<UserLoginResponse> loginByWx(@RequestBody WxLoginRequest request);

    @PostMapping("/login/email")
    Result<UserLoginResponse> loginByEmail(@RequestBody EmailLoginRequest request);

    @PostMapping("/register")
    Result<UserRegisterResponse> register(@RequestBody UserRegisterRequest request);

    @GetMapping("/check-email")
    Result<Boolean> existsByEmail(@RequestParam("email") String email);

    @PostMapping("/reset-password")
    Result<Void> resetPassword(@RequestBody ResetPasswordRequest request);

    @PostMapping("/sms/send")
    Result<Void> sendSmsCode(@RequestBody SendSmsCodeRequest request);

    @PostMapping("/email/send-code")
    Result<Void> sendEmailCode(@RequestBody SendEmailCodeRequest request);

    @PostMapping("/store/apply")
    Result<Void> applyStore(@RequestHeader("X-User-Id") Long userId, @RequestBody StoreApplyRequest request);
}
