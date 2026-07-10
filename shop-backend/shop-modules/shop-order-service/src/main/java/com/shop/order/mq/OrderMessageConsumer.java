package com.shop.order.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单消息消费者
 *
 * @author shop
 * @since 2026-06-23
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "order-topic",
        selectorExpression = "order-created",
        consumerGroup = "order-created-consumer"
)
public class OrderMessageConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String orderId) {
        log.info("收到订单创建消息: orderId={}", orderId);
        // 处理订单创建后的业务逻辑
        // 例如：发送通知、更新统计等
    }
}
