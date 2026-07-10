package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 刷新Token请求
 *
 * @author shop
 * @since 2026-06-23
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新令牌
     */
    private String refreshToken;
}
