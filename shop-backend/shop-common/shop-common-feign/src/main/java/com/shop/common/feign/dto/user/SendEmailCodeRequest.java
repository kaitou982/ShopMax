package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 发送邮箱验证码请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class SendEmailCodeRequest {

    private String email;

    private String type;
}
