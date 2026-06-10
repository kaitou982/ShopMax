package com.shop.user.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送邮箱验证码请求
 *
 * @author shop
 * @since 2026-06-10
 */
@Data
@Schema(description = "发送邮箱验证码请求")
public class SendEmailCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "验证码类型不能为空")
    @Schema(description = "验证码类型: login-登录, register-注册", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
}
