package com.shop.common.feign.client;

import com.shop.common.feign.dto.order.OrderSimpleResponse;
import com.shop.common.feign.fallback.OrderServiceClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务 FeignClient（供 shop-customer-service 调用）
 *
 * @author shop
 * @since 2026-06-17
 */
@FeignClient(name = "shop-order-service", contextId = "orderServiceClient",
             fallbackFactory = OrderServiceClientFallbackFactory.class)
public interface OrderServiceClient {

    /**
     * 按订单号和用户ID查询订单
     */
    @GetMapping("/internal/orders/by-order-no")
    Result<OrderSimpleResponse> getByOrderNo(
            @RequestParam("orderNo") String orderNo,
            @RequestParam("userId") Long userId);
}
