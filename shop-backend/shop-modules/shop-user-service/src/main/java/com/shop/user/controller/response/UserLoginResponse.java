package com.shop.user.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户登录响应
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "用户登录响应")
public class UserLoginResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别: 0-未知 1-男 2-女")
    private Integer gender;

    @Schema(description = "会员等级")
    private Integer memberLevel;

    @Schema(description = "会员等级名称")
    private String memberLevelName;

    @Schema(description = "积分")
    private Integer integral;

    @Schema(description = "账户余额")
    private BigDecimal balance;

    @Schema(description = "成长值")
    private Integer growthValue;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "角色: ADMIN/STORE/USER")
    private String role;

    @Schema(description = "店家审核状态: 0-待审核 1-已通过 2-已拒绝")
    private Integer storeStatus;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "访问令牌")
    private String token;
}
