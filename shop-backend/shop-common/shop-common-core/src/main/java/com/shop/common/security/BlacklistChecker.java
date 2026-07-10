package com.shop.common.security;

/**
 * Token 黑名单检查接口
 *
 * @author shop
 * @since 2026-06-23
 */
@FunctionalInterface
public interface BlacklistChecker {

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token JWT Token
     * @return true 表示在黑名单中，应拒绝访问
     */
    boolean isBlacklisted(String token);
}
