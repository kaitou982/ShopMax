package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalProductClientFallbackFactory;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "shop-product-service", path = "/internal/products",
             fallbackFactory = InternalProductClientFallbackFactory.class)
public interface InternalProductClient {

    @PostMapping("/{id}/deduct-stock")
    Result<Void> deductStock(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/restore-stock")
    Result<Void> restoreStock(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/{id}/add-sales")
    Result<Void> addSales(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);
}
