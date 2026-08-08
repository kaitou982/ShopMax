package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 邮箱验证码登录请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class EmailLoginRequest {

    private String email;

    private String verifyCode;

    private String ip;
}
