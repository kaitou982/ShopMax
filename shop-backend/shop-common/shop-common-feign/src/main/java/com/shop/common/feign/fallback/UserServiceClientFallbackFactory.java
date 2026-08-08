package com.shop.common.feign.fallback;

import com.shop.common.feign.client.UserServiceClient;
import com.shop.common.feign.dto.user.*;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务 FeignClient 降级工厂
 *
 * @author shop
 * @since 2026-06-17
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("用户服务调用失败: {}", cause.getMessage(), cause);
        return new UserServiceClient() {
            @Override
            public Result<UserLoginResponse> login(UserLoginRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<UserLoginResponse> loginByPhone(PhoneLoginRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<UserLoginResponse> loginByWx(WxLoginRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<UserLoginResponse> loginByEmail(EmailLoginRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<UserRegisterResponse> register(UserRegisterRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Boolean> existsByEmail(String email) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Void> resetPassword(ResetPasswordRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Void> sendSmsCode(SendSmsCodeRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Void> sendEmailCode(SendEmailCodeRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Void> applyStore(Long userId, StoreApplyRequest request) {
                return Result.error(503, "用户服务暂时不可用，请稍后再试");
            }
        };
    }
}
