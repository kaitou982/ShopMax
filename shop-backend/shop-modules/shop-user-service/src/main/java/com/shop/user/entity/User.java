package com.shop.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@TableName("ums_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别: 0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 会员等级: 1-普通 2-银卡 3-金卡 4-钻石
     */
    private Integer memberLevel;

    /**
     * 积分
     */
    private Integer integral;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 成长值
     */
    private Integer growthValue;

    /**
     * 微信小程序openid
     */
    private String openidMp;

    /**
     * 微信APP openid
     */
    private String openidApp;

    /**
     * 微信unionid
     */
    private String unionid;

    /**
     * 角色: ADMIN-管理员 STORE-店家 USER-普通用户
     */
    private String role;

    /**
     * 店家审核状态: 0-待审核 1-已通过 2-已拒绝
     */
    private Integer storeStatus;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 店铺Logo
     */
    private String storeLogo;

    /**
     * 店铺简介
     */
    private String storeDescription;

    /**
     * 申请入驻时间
     */
    private LocalDateTime storeApplyTime;

    /**
     * 入驻审核时间
     */
    private LocalDateTime storeAuditTime;

    /**
     * 入驻拒绝原因
     */
    private String storeRejectReason;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 邀请码
     */
    private String referralCode;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
//    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
