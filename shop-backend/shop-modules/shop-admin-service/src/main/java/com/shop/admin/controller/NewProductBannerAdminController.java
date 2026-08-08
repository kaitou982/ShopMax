package com.shop.admin.controller;

import com.shop.common.feign.client.InternalProductClient;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 新品Banner管理控制器（Admin端，通过 Feign 调用 product-service）
 *
 * @author shop
 * @since 2026-06-17
 */
@Slf4j
@Tag(name = "新品Banner管理")
@RestController
@RequestMapping("/api/v1/admin/new-product-banners")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NewProductBannerAdminController {

    private final InternalProductClient internalProductClient;

    @Operation(summary = "Banner分页列表")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return internalProductClient.getBannerPage(pageNum, pageSize);
    }

    @Operation(summary = "新增Banner")
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> banner) {
        if (banner.get("title") == null || banner.get("title").toString().isBlank()) {
            return Result.badRequest("标题不能为空");
        }
        if (banner.get("imageUrl") == null || banner.get("imageUrl").toString().isBlank()) {
            return Result.badRequest("图片URL不能为空");
        }
        return internalProductClient.createBanner(banner);
    }

    @Operation(summary = "编辑Banner")
    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> banner) {
        return internalProductClient.updateBanner(id, banner);
    }

    @Operation(summary = "删除Banner")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return internalProductClient.deleteBanner(id);
    }
}
