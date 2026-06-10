package com.shop.user.service;

/**
 * 短信验证码服务接口
 *
 * @author shop
 * @since 2026-05-13
 */
public interface SmsService {

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @param type  验证码类型: login-登录, register-注册
     */
    void sendCode(String phone, String type);

    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @param type  验证码类型
     * @return 验证是否通过
     */
    boolean verifyCode(String phone, String code, String type);
}
