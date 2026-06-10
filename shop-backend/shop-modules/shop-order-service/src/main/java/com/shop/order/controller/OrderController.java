package com.shop.order.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.order.entity.Order;
import com.shop.order.entity.OrderLog;
import com.shop.order.service.OrderService;
import com.shop.order.service.impl.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 *
 * @author shop
 * @since 2026-04-22
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Order> create(@RequestAttribute("userId") Long userId, @RequestBody Order order) {
        order.setUserId(userId);
        return Result.success(orderService.create(order));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam String reason) {
        orderService.cancel(id, reason);
        return Result.success();
    }

    @Operation(summary = "支付订单")
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id, @RequestParam Integer payType) {
        orderService.pay(id, payType);
        return Result.success();
    }

    @Operation(summary = "订单发货")
    @PutMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Void> ship(@PathVariable Long id) {
        orderService.ship(id);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{id}/receive")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        orderService.confirmReceive(id);
        return Result.success();
    }

    @Operation(summary = "申请退款")
    @PutMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id, @RequestBody Map<String,String> body) {
        orderService.refund(id, body.getOrDefault("reason", "用户申请退款"));
        return Result.success();
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @Operation(summary = "分页查询订单")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<PageResult<Order>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo) {
        return Result.success(orderService.page(pageNum, pageSize, userId, status, orderNo));
    }

    @Operation(summary = "获取我的订单列表")
    @GetMapping("/my")
    public Result<List<Order>> listMyOrders(@RequestAttribute("userId") Long userId) {
        return Result.success(orderService.listByUserId(userId));
    }

    @Operation(summary = "获取订单操作日志")
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<List<OrderLog>> getLogs(@PathVariable Long id) {
        if (orderService instanceof OrderServiceImpl impl) {
            return Result.success(impl.getLogs(id));
        }
        return Result.success(List.of());
    }
}
