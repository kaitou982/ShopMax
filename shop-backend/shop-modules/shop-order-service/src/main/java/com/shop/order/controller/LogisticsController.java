package com.shop.order.controller;

import com.shop.common.web.Result;
import com.shop.order.controller.request.LogisticsCreateRequest;
import com.shop.order.controller.request.LogisticsTraceRequest;
import com.shop.order.entity.Logistics;
import com.shop.order.entity.LogisticsTrace;
import com.shop.order.service.DrivingRouteService;
import com.shop.order.service.DrivingRouteService.RouteResult;
import com.shop.order.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物流控制器
 *
 * @author shop
 * @since 2026-06-07
 */
@Tag(name = "物流管理")
@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;
    private final DrivingRouteService drivingRouteService;

    @Operation(summary = "创建物流信息")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Logistics> create(@Valid @RequestBody LogisticsCreateRequest request) {
        return Result.success(logisticsService.createLogistics(request));
    }

    @Operation(summary = "添加物流轨迹")
    @PostMapping("/{id}/traces")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Void> addTrace(@PathVariable Long id, @Valid @RequestBody LogisticsTraceRequest request) {
        logisticsService.addTrace(id, request);
        return Result.success();
    }

    @Operation(summary = "根据订单ID查询物流")
    @GetMapping("/order/{orderId}")
    public Result<Logistics> getByOrderId(@PathVariable Long orderId) {
        Logistics logistics = logisticsService.getLogisticsWithApiUpdate(orderId);
        return Result.success(logistics);
    }

    @Operation(summary = "获取物流详情")
    @GetMapping("/{id}")
    public Result<Logistics> getDetail(@PathVariable Long id) {
        return Result.success(logisticsService.getLogisticsDetail(id));
    }

    @Operation(summary = "获取物流轨迹")
    @GetMapping("/{id}/traces")
    public Result<List<LogisticsTrace>> getTraces(@PathVariable Long id) {
        return Result.success(logisticsService.getTraces(id));
    }

    @Operation(summary = "更新物流状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        logisticsService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "签收物流")
    @PutMapping("/{id}/sign")
    public Result<Void> sign(@PathVariable Long id) {
        logisticsService.signLogistics(id);
        return Result.success();
    }

    @Operation(summary = "刷新物流信息")
    @PostMapping("/{id}/refresh")
    public Result<Void> refresh(@PathVariable Long id) {
        logisticsService.refreshLogistics(id);
        return Result.success();
    }

    @Operation(summary = "获取驾车路线")
    @GetMapping("/route")
    public Result<RouteResult> getRoute(
            @RequestParam BigDecimal originLng,
            @RequestParam BigDecimal originLat,
            @RequestParam BigDecimal destLng,
            @RequestParam BigDecimal destLat) {
        RouteResult result = drivingRouteService.getDrivingRoute(originLng, originLat, destLng, destLat);
        return Result.success(result);
    }
}
