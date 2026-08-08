package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 发送短信验证码请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class SendSmsCodeRequest {

    private String phone;

    private String type;
}
