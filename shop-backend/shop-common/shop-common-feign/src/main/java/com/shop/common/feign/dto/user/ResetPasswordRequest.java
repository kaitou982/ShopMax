package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 重置密码请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class ResetPasswordRequest {

    private String email;

    private String verifyCode;

    private String newPassword;
}
