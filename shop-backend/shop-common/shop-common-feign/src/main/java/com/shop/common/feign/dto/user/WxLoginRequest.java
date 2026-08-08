package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 微信登录请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class WxLoginRequest {

    private String openid;

    private String unionid;

    private String nickname;

    private String avatar;

    private Integer gender;

    private String phone;

    private String ip;
}
