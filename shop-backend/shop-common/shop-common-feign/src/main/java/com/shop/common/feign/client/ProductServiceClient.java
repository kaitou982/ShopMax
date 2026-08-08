package com.shop.common.feign.client;

import com.shop.common.feign.dto.product.CategorySimpleResponse;
import com.shop.common.feign.dto.product.ProductSimpleResponse;
import com.shop.common.feign.fallback.ProductServiceClientFallbackFactory;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品服务 FeignClient（供 shop-customer-service 调用）
 *
 * @author shop
 * @since 2026-06-17
 */
@FeignClient(name = "shop-product-service", contextId = "productServiceClient",
             fallbackFactory = ProductServiceClientFallbackFactory.class)
public interface ProductServiceClient {

    /**
     * 搜索商品（按关键词、分类、价格区间）
     */
    @GetMapping("/internal/products/search")
    Result<List<ProductSimpleResponse>> searchProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize);

    /**
     * 按名称模糊搜索分类
     */
    @GetMapping("/internal/categories/search")
    Result<List<CategorySimpleResponse>> searchCategories(
            @RequestParam("name") String name);
}
