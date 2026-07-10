package com.shop.product.controller;

import com.shop.common.web.Result;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品服务内部接口（供其他微服务通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

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
}
