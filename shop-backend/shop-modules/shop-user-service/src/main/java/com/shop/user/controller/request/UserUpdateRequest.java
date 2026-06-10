package com.shop.user.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户信息更新请求
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateRequest {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别: 0-未知 1-男 2-女")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "角色: ADMIN/STORE/USER")
    private String role;
}
