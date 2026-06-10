package com.shop.user.service;

/**
 * 邮箱验证码服务接口
 *
 * @author shop
 * @since 2026-06-10
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @param type  验证码类型: login-登录, register-注册
     */
    void sendCode(String email, String type);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱地址
     * @param code  验证码
     * @param type  验证码类型
     * @return 验证是否通过
     */
    boolean verifyCode(String email, String code, String type);
}
