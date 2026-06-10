package com.shop.user.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息响应
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "用户信息响应")
public class UserInfoResponse {

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

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别: 0-未知 1-男 2-女")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;

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

    @Schema(description = "店铺Logo")
    private String storeLogo;

    @Schema(description = "店铺简介")
    private String storeDescription;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
