package com.shop.user.service.impl;

import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.user.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现
 *
 * @author shop
 * @since 2026-05-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisUtil redisUtil;

    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long SEND_INTERVAL_SECONDS = 60;
    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    private static final String SMS_INTERVAL_KEY_PREFIX = "sms:interval:";

    @Override
    public void sendCode(String phone, String type) {
        // 检查发送间隔
        String intervalKey = SMS_INTERVAL_KEY_PREFIX + type + ":" + phone;
        if (redisUtil.hasKey(intervalKey)) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        // 生成6位验证码
        String code = generateCode();

        // 存储验证码到Redis，5分钟有效
        String codeKey = SMS_CODE_KEY_PREFIX + type + ":" + phone;
        redisUtil.set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送间隔限制
        redisUtil.set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        log.info("发送短信验证码: phone={}, type={}, code={}", phone, type, code);
    }

    @Override
    public boolean verifyCode(String phone, String code, String type) {
        if (phone == null || code == null) {
            return false;
        }

        String codeKey = SMS_CODE_KEY_PREFIX + type + ":" + phone;
        String savedCode = redisUtil.get(codeKey);

        if (savedCode == null) {
            log.warn("验证码不存在或已过期: phone={}, type={}", phone, type);
            return false;
        }

        if (!savedCode.equals(code)) {
            log.warn("验证码错误: phone={}, type={}, input={}, saved={}", phone, type, code, savedCode);
            return false;
        }

        // 验证成功后删除验证码
        redisUtil.delete(codeKey);
        log.info("验证码验证成功: phone={}, type={}", phone, type);

        return true;
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
