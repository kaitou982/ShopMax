package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 用户登录请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class UserLoginRequest {

    private String username;

    private String password;

    private String ip;
}
