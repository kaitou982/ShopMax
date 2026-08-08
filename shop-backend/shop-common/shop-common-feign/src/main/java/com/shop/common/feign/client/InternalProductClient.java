package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalProductClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "shop-product-service", contextId = "internalProductClient",
             path = "/internal/products", fallbackFactory = InternalProductClientFallbackFactory.class)
public interface InternalProductClient {

    @PostMapping("/{id}/deduct-stock")
    Result<Void> deductStock(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/restore-stock")
    Result<Void> restoreStock(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/add-sales")
    Result<Void> addSales(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/batch-restore-stock")
    Result<Void> batchRestoreStock(@RequestBody List<Map<String, Object>> items);

    @GetMapping("/new-products/page")
    Result<Map<String, Object>> getNewProductPage(@RequestParam("pageNum") int pageNum,
                                                   @RequestParam("pageSize") int pageSize,
                                                   @RequestParam(value = "categoryId", required = false) Long categoryId);

    @GetMapping("/batch-info")
    Result<Map<String, Object>> getBatchProductInfo(@RequestParam("ids") List<Long> ids);

    @GetMapping("/new-products/stats")
    Result<Map<String, Object>> getNewProductStats();

    @PostMapping("/batch-mark-new")
    Result<Void> batchMarkNew(@RequestBody Map<String, Object> request);

    @PostMapping("/batch-unmark-new")
    Result<Void> batchUnmarkNew(@RequestBody Map<String, Object> request);

    @PutMapping("/{id}/new-product-settings")
    Result<Void> updateNewProductSettings(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @GetMapping("/banners/page")
    Result<Map<String, Object>> getBannerPage(@RequestParam("pageNum") int pageNum,
                                               @RequestParam("pageSize") int pageSize);

    @PostMapping("/banners")
    Result<Map<String, Object>> createBanner(@RequestBody Map<String, Object> request);

    @PutMapping("/banners/{id}")
    Result<Map<String, Object>> updateBanner(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @DeleteMapping("/banners/{id}")
    Result<Void> deleteBanner(@PathVariable("id") Long id);
}
