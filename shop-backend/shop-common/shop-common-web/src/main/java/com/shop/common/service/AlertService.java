package com.shop.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 告警服务
 *
 * @author shop
 * @since 2026-06-15
 */
@Slf4j
@Service
public class AlertService {

    /**
     * 发送告警
     *
     * @param title   告警标题
     * @param content 告警内容
     */
    public void sendAlert(String title, String content) {
        // TODO: 对接告警系统（如钉钉、企业微信、邮件等）
        log.error("【告警】{}: {}", title, content);
    }

    /**
     * 发送秒杀异常告警
     *
     * @param orderNo 订单号
     * @param message 错误信息
     */
    public void sendSeckillAlert(String orderNo, String message) {
        sendAlert("秒杀订单异常", "订单号: " + orderNo + ", 原因: " + message);
    }

    /**
     * 发送死信消息告警
     *
     * @param messageType 消息类型
     * @param businessId  业务ID
     */
    public void sendDeadLetterAlert(String messageType, String businessId) {
        sendAlert("死信消息告警", "消息类型: " + messageType + ", 业务ID: " + businessId);
    }
}
