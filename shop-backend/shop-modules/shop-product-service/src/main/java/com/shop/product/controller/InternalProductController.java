package com.shop.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.product.entity.NewProductBanner;
import com.shop.product.mapper.NewProductBannerMapper;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务内部接口（供其他微服务通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;
    private final NewProductBannerMapper bannerMapper;

    @PostMapping("/{id}/deduct-stock")
    public Result<Void> deductStock(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer quantity = (Integer) request.get("quantity");
        productService.deductStock(id, quantity);
        return Result.success();
    }

    @PostMapping("/{id}/restore-stock")
    public Result<Void> restoreStock(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer quantity = (Integer) request.get("quantity");
        productService.restoreStock(id, quantity);
        return Result.success();
    }

    @PostMapping("/{id}/add-sales")
    public Result<Void> addSales(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer quantity = (Integer) request.get("quantity");
        productService.addSales(id, quantity);
        return Result.success();
    }

    @PostMapping("/batch-restore-stock")
    public Result<Void> batchRestoreStock(@RequestBody List<Map<String, Object>> items) {
        productService.batchRestoreStock(items);
        return Result.success();
    }

    @GetMapping("/new-products/page")
    public Result<Map<String, Object>> getNewProductPage(@RequestParam int pageNum,
                                                          @RequestParam int pageSize,
                                                          @RequestParam(required = false) Long categoryId) {
        Map<String, Object> result = productService.getNewProductPage(pageNum, pageSize, categoryId);
        return Result.success(result);
    }

    @GetMapping("/new-products/stats")
    public Result<Map<String, Object>> getNewProductStats() {
        Map<String, Object> stats = productService.getNewProductStats();
        return Result.success(stats);
    }

    @PostMapping("/batch-mark-new")
    public Result<Void> batchMarkNew(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        productService.batchMarkNew(ids);
        return Result.success();
    }

    @PostMapping("/batch-unmark-new")
    public Result<Void> batchUnmarkNew(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        productService.batchUnmarkNew(ids);
        return Result.success();
    }

    @PutMapping("/{id}/new-product-settings")
    public Result<Void> updateNewProductSettings(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer sort = request.get("sort") != null ? ((Number) request.get("sort")).intValue() : 0;
        String startTime = (String) request.get("startTime");
        String endTime = (String) request.get("endTime");
        productService.updateNewProductSettings(id, sort, startTime, endTime);
        return Result.success();
    }

    @GetMapping("/batch-info")
    public Result<Map<String, Object>> getBatchProductInfo(@RequestParam("ids") List<Long> ids) {
        Map<String, Object> info = productService.getBatchProductInfo(ids);
        return Result.success(info);
    }

    // ==================== Banner 管理 ====================

    @GetMapping("/banners/page")
    public Result<Map<String, Object>> getBannerPage(@RequestParam int pageNum, @RequestParam int pageSize) {
        Page<NewProductBanner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NewProductBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewProductBanner::getDeleted, 0);
        wrapper.orderByDesc(NewProductBanner::getSort);
        Page<NewProductBanner> result = bannerMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return Result.success(data);
    }

    @PostMapping("/banners")
    public Result<Map<String, Object>> createBanner(@RequestBody Map<String, Object> request) {
        NewProductBanner banner = new NewProductBanner();
        banner.setTitle((String) request.get("title"));
        banner.setImageUrl((String) request.get("imageUrl"));
        banner.setLinkUrl((String) request.get("linkUrl"));
        banner.setSort(request.get("sort") != null ? ((Number) request.get("sort")).intValue() : 0);
        banner.setStatus(request.get("status") != null ? ((Number) request.get("status")).intValue() : 1);
        banner.setDeleted(0);
        bannerMapper.insert(banner);

        Map<String, Object> data = new HashMap<>();
        data.put("id", banner.getId());
        data.put("title", banner.getTitle());
        return Result.success(data);
    }

    @PutMapping("/banners/{id}")
    public Result<Map<String, Object>> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        NewProductBanner existing = bannerMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            return Result.error(404, "Banner不存在");
        }
        if (request.containsKey("title")) existing.setTitle((String) request.get("title"));
        if (request.containsKey("imageUrl")) existing.setImageUrl((String) request.get("imageUrl"));
        if (request.containsKey("linkUrl")) existing.setLinkUrl((String) request.get("linkUrl"));
        if (request.containsKey("sort")) existing.setSort(((Number) request.get("sort")).intValue());
        if (request.containsKey("status")) existing.setStatus(((Number) request.get("status")).intValue());
        bannerMapper.updateById(existing);

        Map<String, Object> data = new HashMap<>();
        data.put("id", existing.getId());
        return Result.success(data);
    }

    @DeleteMapping("/banners/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        NewProductBanner existing = bannerMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            return Result.error(404, "Banner不存在");
        }
        existing.setDeleted(1);
        bannerMapper.updateById(existing);
        return Result.success();
    }
}
