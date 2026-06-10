package com.shop.user.service.impl;

import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.user.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现
 *
 * @author shop
 * @since 2026-06-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RedisUtil redisUtil;
    private final JavaMailSender javaMailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String mailFrom;

    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long SEND_INTERVAL_SECONDS = 60;
    private static final String EMAIL_CODE_KEY_PREFIX = "email:code:";
    private static final String EMAIL_INTERVAL_KEY_PREFIX = "email:interval:";

    @Override
    public void sendCode(String email, String type) {
        String intervalKey = EMAIL_INTERVAL_KEY_PREFIX + type + ":" + email;
        if (redisUtil.hasKey(intervalKey)) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        String code = generateCode();

        String codeKey = EMAIL_CODE_KEY_PREFIX + type + ":" + email;
        redisUtil.set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        redisUtil.set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 使用 Virtual Threads 异步发送邮件
        Thread.ofVirtual().name("email-send-" + email).start(() -> {
            try {
                doSendEmail(email, code, type);
            } catch (Exception e) {
                log.error("邮件发送失败: email={}, error={}", email, e.getMessage(), e);
            }
        });

        log.info("邮箱验证码已生成: email={}, type={}, code={}", email, type, code);
    }

    @Override
    public boolean verifyCode(String email, String code, String type) {
        if (email == null || code == null) {
            return false;
        }

        String codeKey = EMAIL_CODE_KEY_PREFIX + type + ":" + email;
        String savedCode = redisUtil.get(codeKey);

        if (savedCode == null) {
            log.warn("邮箱验证码不存在或已过期: email={}, type={}", email, type);
            return false;
        }

        if (!savedCode.equals(code)) {
            log.warn("邮箱验证码错误: email={}, type={}, input={}, saved={}", email, type, code, savedCode);
            return false;
        }

        redisUtil.delete(codeKey);
        log.info("邮箱验证码验证成功: email={}, type={}", email, type);

        return true;
    }

    private void doSendEmail(String email, String code, String type) throws MessagingException {
        String subject = "register".equals(type) ? "ShopMax 注册验证码" : "ShopMax 登录验证码";
        String content = buildEmailContent(code, type);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailFrom);
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true);

        javaMailSender.send(message);
        log.info("邮件发送成功: email={}, type={}", email, type);
    }

    private String buildEmailContent(String code, String type) {
        String action = "register".equals(type) ? "注册" : "登录";
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                  <h2 style="color: #FF5000;">ShopMax 验证码</h2>
                  <p>您正在进行<strong>%s</strong>操作，验证码为：</p>
                  <p style="font-size: 32px; font-weight: bold; color: #FF5000; letter-spacing: 8px;">%s</p>
                  <p style="color: #999;">验证码 %d 分钟内有效，请勿泄露给他人。</p>
                  <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                  <p style="color: #999; font-size: 12px;">如非本人操作，请忽略此邮件。</p>
                </body>
                </html>
                """.formatted(action, code, CODE_EXPIRE_MINUTES);
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
