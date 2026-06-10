package com.shop.admin.controller;

import com.shop.admin.service.DashboardService;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STORE')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getStats());
    }

    @Operation(summary = "获取7天销售趋势")
    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> getSalesTrend() {
        return Result.success(dashboardService.getSalesTrend());
    }

    @Operation(summary = "获取最新订单")
    @GetMapping("/recent-orders")
    public Result<List<Map<String, Object>>> getRecentOrders() {
        return Result.success(dashboardService.getRecentOrders());
    }
}
