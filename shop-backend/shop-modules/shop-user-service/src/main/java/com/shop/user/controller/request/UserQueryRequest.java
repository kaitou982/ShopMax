package com.shop.user.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "会员等级")
    private Integer memberLevel;

    @Schema(description = "角色: ADMIN/STORE/USER")
    private String role;
}
