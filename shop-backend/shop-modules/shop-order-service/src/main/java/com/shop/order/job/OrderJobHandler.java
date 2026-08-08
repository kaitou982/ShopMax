package com.shop.order.job;

import com.shop.order.service.OrderService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单服务 XXL-JOB 任务处理器
 *
 * @author shop
 * @since 2026-06-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderJobHandler {

    private final OrderService orderService;

    /**
     * 自动取消超时订单
     * 原 @Scheduled(fixedDelay = 60000) 在 OrderServiceImpl
     */
    @XxlJob("autoCancelOrdersJob")
    public void autoCancelOrders() {
        log.info("XXL-JOB: 自动取消超时订单开始");
        try {
            orderService.autoCancelTimeoutOrders();
            log.info("XXL-JOB: 自动取消超时订单完成");
        } catch (Exception e) {
            log.error("XXL-JOB: 自动取消超时订单失败", e);
        }
    }
}
