package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 手机号登录请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class PhoneLoginRequest {

    private String phone;

    private String verifyCode;

    private String ip;
}
