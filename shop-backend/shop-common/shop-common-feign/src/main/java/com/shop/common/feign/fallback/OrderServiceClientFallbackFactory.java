package com.shop.common.feign.fallback;

import com.shop.common.feign.client.OrderServiceClient;
import com.shop.common.feign.dto.order.OrderSimpleResponse;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 订单服务 FeignClient 降级工厂
 *
 * @author shop
 * @since 2026-06-17
 */
@Slf4j
@Component
public class OrderServiceClientFallbackFactory implements FallbackFactory<OrderServiceClient> {

    @Override
    public OrderServiceClient create(Throwable cause) {
        log.error("订单服务调用失败: {}", cause.getMessage(), cause);
        return new OrderServiceClient() {
            @Override
            public Result<OrderSimpleResponse> getByOrderNo(String orderNo, Long userId) {
                return Result.error(503, "订单服务暂时不可用，请稍后再试");
            }
        };
    }
}
