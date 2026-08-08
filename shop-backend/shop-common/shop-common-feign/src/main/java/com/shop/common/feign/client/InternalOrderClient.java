package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalOrderClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "shop-order-service", contextId = "internalOrderClient",
             path = "/internal/orders", fallbackFactory = InternalOrderClientFallbackFactory.class)
public interface InternalOrderClient {

    @PostMapping("/{id}/update-status")
    Result<Void> updateOrderStatus(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @GetMapping("/{id}/info")
    Result<Map<String, Object>> getOrderInfo(@PathVariable("id") Long id);

    @GetMapping("/by-order-no")
    Result<Map<String, Object>> getOrderInfoByOrderNo(@RequestParam("orderNo") String orderNo);

    @PostMapping("/by-order-no/update-status")
    Result<Void> updateOrderStatusByOrderNo(@RequestBody Map<String, Object> request);

    @GetMapping("/{id}/items")
    Result<List<Map<String, Object>>> getOrderItems(@PathVariable("id") Long id);

    @GetMapping("/dashboard-stats")
    Result<Map<String, Object>> getDashboardStats();
}
