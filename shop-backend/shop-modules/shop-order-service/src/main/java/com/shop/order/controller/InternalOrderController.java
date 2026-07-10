package com.shop.order.controller;

import com.shop.common.web.Result;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单服务内部接口（供其他微服务通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @PostMapping("/{id}/update-status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer status = (Integer) request.get("status");
        orderService.updateOrderStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id}/info")
    public Result<Map<String, Object>> getOrderInfo(@PathVariable Long id) {
        Map<String, Object> info = orderService.getOrderBasicInfo(id);
        return Result.success(info);
    }
}
