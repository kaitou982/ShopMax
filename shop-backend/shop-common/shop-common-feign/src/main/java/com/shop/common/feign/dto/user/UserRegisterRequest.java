package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 用户注册请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class UserRegisterRequest {

    private String phone;

    private String email;

    private String password;

    private String verifyCode;

    private String username;

    private String nickname;

    private String referralCode;
}
