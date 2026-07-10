package com.shop.admin.controller;

import com.shop.admin.mapper.NewProductMapper;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新品管理控制器（Admin端）
 *
 * @author shop
 * @since 2026-06-17
 */
@Tag(name = "新品管理")
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NewProductAdminController {

    private final NewProductMapper newProductMapper;

    @Operation(summary = "新品分页列表")
    @GetMapping("/new")
    public Result<Map<String, Object>> listNewProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId) {
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> records;
        int total;
        if (categoryId != null) {
            records = newProductMapper.selectNewProductPageByCategory(categoryId, offset, pageSize);
            total = newProductMapper.countNewProductsByCategory(categoryId);
        } else {
            records = newProductMapper.selectNewProductPage(offset, pageSize);
            total = newProductMapper.countNewProducts();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return Result.success(result);
    }

    @Operation(summary = "批量标记新品")
    @PutMapping("/new/batch-mark")
    public Result<Void> batchMarkNew(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("请选择商品");
        }
        newProductMapper.batchMarkNew(ids);
        return Result.success();
    }

    @Operation(summary = "批量取消新品")
    @PutMapping("/new/batch-unmark")
    public Result<Void> batchUnmarkNew(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("请选择商品");
        }
        newProductMapper.batchUnmarkNew(ids);
        return Result.success();
    }

    @Operation(summary = "更新新品设置")
    @PutMapping("/{id}/new-settings")
    public Result<Void> updateNewSettings(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer sort = body.get("sort") != null ? ((Number) body.get("sort")).intValue() : 0;
        String startTime = (String) body.get("startTime");
        String endTime = (String) body.get("endTime");
        newProductMapper.updateNewProductSettings(id, sort, startTime, endTime);
        return Result.success();
    }

    @Operation(summary = "新品统计")
    @GetMapping("/new/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", newProductMapper.countNewProducts());
        stats.put("active", newProductMapper.countActiveNewProducts());
        stats.put("expiring", newProductMapper.countExpiringNewProducts());
        stats.put("todayNew", newProductMapper.countTodayNewProducts());
        return Result.success(stats);
    }
}
