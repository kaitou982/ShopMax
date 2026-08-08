package com.shop.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.feign.dto.order.OrderSimpleResponse;
import com.shop.common.web.Result;
import com.shop.order.entity.Order;
import com.shop.order.mapper.OrderMapper;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单服务内部接口（供其他微服务通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

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

    @GetMapping("/by-order-no")
    public Result<OrderSimpleResponse> getByOrderNo(
            @RequestParam String orderNo,
            @RequestParam(required = false) Long userId) {

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getDeleted, 0);
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }

        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            return Result.error(404, "未找到该订单");
        }

        OrderSimpleResponse response = new OrderSimpleResponse();
        response.setOrderNo(order.getOrderNo());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setPayAmount(order.getPayAmount());
        response.setFreightAmount(order.getFreightAmount());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setReceiverAddress(order.getReceiverAddress());
        response.setCreateTime(order.getCreateTime());
        response.setPayTime(order.getPayTime());
        response.setDeliveryTime(order.getDeliveryTime());

        return Result.success(response);
    }

    @PostMapping("/by-order-no/update-status")
    public Result<Void> updateOrderStatusByOrderNo(@RequestBody Map<String, Object> request) {
        String orderNo = (String) request.get("orderNo");
        Integer status = (Integer) request.get("status");
        orderService.updateOrderStatusByOrderNo(orderNo, status);
        return Result.success();
    }

    @GetMapping("/{id}/items")
    public Result<List<Map<String, Object>>> getOrderItems(@PathVariable Long id) {
        List<Map<String, Object>> items = orderService.getOrderItems(id);
        return Result.success(items);
    }

    @GetMapping("/dashboard-stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = orderService.getDashboardStats();
        return Result.success(stats);
    }
}
