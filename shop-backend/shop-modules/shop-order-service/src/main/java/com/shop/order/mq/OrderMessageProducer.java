package com.shop.order.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 订单消息生产者
 *
 * @author shop
 * @since 2026-06-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    private static final String ORDER_TOPIC = "order-topic";

    /**
     * 发送订单创建消息
     */
    public void sendOrderCreatedMessage(Long orderId, Long userId) {
        Message<String> message = MessageBuilder
                .withPayload(orderId.toString())
                .setHeader("orderId", orderId)
                .setHeader("userId", userId)
                .setHeader("eventType", "ORDER_CREATED")
                .build();

        rocketMQTemplate.send(ORDER_TOPIC + ":order-created", message);
        log.info("发送订单创建消息: orderId={}, userId={}", orderId, userId);
    }

    /**
     * 发送订单支付成功消息
     */
    public void sendOrderPaidMessage(Long orderId, Long userId) {
        Message<String> message = MessageBuilder
                .withPayload(orderId.toString())
                .setHeader("orderId", orderId)
                .setHeader("userId", userId)
                .setHeader("eventType", "ORDER_PAID")
                .build();

        rocketMQTemplate.send(ORDER_TOPIC + ":order-paid", message);
        log.info("发送订单支付成功消息: orderId={}, userId={}", orderId, userId);
    }

    /**
     * 发送订单取消消息
     */
    public void sendOrderCancelledMessage(Long orderId, Long userId) {
        Message<String> message = MessageBuilder
                .withPayload(orderId.toString())
                .setHeader("orderId", orderId)
                .setHeader("userId", userId)
                .setHeader("eventType", "ORDER_CANCELLED")
                .build();

        rocketMQTemplate.send(ORDER_TOPIC + ":order-cancelled", message);
        log.info("发送订单取消消息: orderId={}, userId={}", orderId, userId);
    }
}
