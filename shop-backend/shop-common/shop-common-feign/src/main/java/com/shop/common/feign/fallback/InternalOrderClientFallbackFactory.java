package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalOrderClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InternalOrderClientFallbackFactory implements FallbackFactory<InternalOrderClient> {
    @Override
    public InternalOrderClient create(Throwable cause) {
        log.error("订单服务内部调用失败: {}", cause.getMessage(), cause);
        return new InternalOrderClient() {
            @Override
            public Result<Void> updateOrderStatus(Long id, Map<String, Object> request) {
                return Result.error(503, "订单服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getOrderInfo(Long id) {
                return Result.error(503, "订单服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getOrderInfoByOrderNo(String orderNo) {
                return Result.error(503, "订单服务暂时不可用");
            }
            @Override
            public Result<Void> updateOrderStatusByOrderNo(Map<String, Object> request) {
                return Result.error(503, "订单服务暂时不可用");
            }
            @Override
            public Result<List<Map<String, Object>>> getOrderItems(Long id) {
                return Result.error(503, "订单服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getDashboardStats() {
                return Result.error(503, "订单服务暂时不可用");
            }
        };
    }
}
