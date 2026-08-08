package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 用户注册响应（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class UserRegisterResponse {

    private Long userId;

    private String username;

    private String nickname;
}
