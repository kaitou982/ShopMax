package com.shop.common.security;

/**
 * 角色常量
 *
 * @author shop
 * @since 2026-05-21
 */
public final class RoleConstants {

    /** 管理员 */
    public static final String ADMIN = "ADMIN";

    /** 店家用户 */
    public static final String STORE = "STORE";

    /** 普通用户 */
    public static final String USER = "USER";

    /** Spring Security 角色前缀 */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_STORE = "ROLE_STORE";
    public static final String ROLE_USER = "ROLE_USER";

    private RoleConstants() {
    }
}
