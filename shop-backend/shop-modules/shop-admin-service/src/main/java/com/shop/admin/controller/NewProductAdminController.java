package com.shop.admin.controller;

import com.shop.common.feign.client.InternalProductClient;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Tag(name = "新品管理")
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NewProductAdminController {

    private final InternalProductClient internalProductClient;

    @Operation(summary = "新品分页列表")
    @GetMapping("/new")
    public Result<Map<String, Object>> listNewProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId) {
        log.info("查询新品列表: pageNum={}, pageSize={}, categoryId={}", pageNum, pageSize, categoryId);
        try {
            Result<Map<String, Object>> result = internalProductClient.getNewProductPage(pageNum, pageSize, categoryId);
            log.info("查询新品列表成功: code={}", result.getCode());
            return result;
        } catch (Exception e) {
            log.error("查询新品列表失败", e);
            return Result.error("查询新品列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量标记新品")
    @PutMapping("/new/batch-mark")
    public Result<Void> batchMarkNew(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("请选择商品");
        }
        Map<String, Object> request = new HashMap<>();
        request.put("ids", ids);
        return internalProductClient.batchMarkNew(request);
    }

    @Operation(summary = "批量取消新品")
    @PutMapping("/new/batch-unmark")
    public Result<Void> batchUnmarkNew(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.badRequest("请选择商品");
        }
        Map<String, Object> request = new HashMap<>();
        request.put("ids", ids);
        return internalProductClient.batchUnmarkNew(request);
    }

    @Operation(summary = "更新新品设置")
    @PutMapping("/{id}/new-settings")
    public Result<Void> updateNewSettings(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return internalProductClient.updateNewProductSettings(id, body);
    }

    @Operation(summary = "新品统计")
    @GetMapping("/new/stats")
    public Result<Map<String, Object>> getStats() {
        return internalProductClient.getNewProductStats();
    }
}
