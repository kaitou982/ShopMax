package com.shop.user.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户注册响应
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "用户注册响应")
public class UserRegisterResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;
}
