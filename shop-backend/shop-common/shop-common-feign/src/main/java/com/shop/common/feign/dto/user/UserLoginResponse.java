package com.shop.common.feign.dto.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户登录响应（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class UserLoginResponse {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private Integer gender;

    private Integer memberLevel;

    private String memberLevelName;

    private Integer integral;

    private BigDecimal balance;

    private Integer growthValue;

    private LocalDateTime lastLoginTime;

    private String role;

    private Integer storeStatus;

    private String storeName;

    private String token;

    private String refreshToken;
}
