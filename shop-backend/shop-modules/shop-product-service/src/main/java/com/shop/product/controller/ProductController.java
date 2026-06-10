package com.shop.product.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.product.controller.request.ReviewCreateRequest;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductReview;
import com.shop.product.service.ProductReviewService;
import com.shop.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 *
 * @author shop
 * @since 2026-04-22
 */
@Tag(name = "商品管理")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductReviewService productReviewService;

    @Operation(summary = "创建商品")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Product> create(@RequestBody Product product) {
        return Result.success(productService.create(product));
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.update(id, product));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "分页查询商品")
    @GetMapping
    public Result<PageResult<Product>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortBy) {
        return Result.success(productService.page(pageNum, pageSize, categoryId, keyword, status, sortBy));
    }

    @Operation(summary = "商品上架")
    @PutMapping("/{id}/on-shelf")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Void> onShelf(@PathVariable Long id) {
        productService.onShelf(id);
        return Result.success();
    }

    @Operation(summary = "商品下架")
    @PutMapping("/{id}/off-shelf")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(id);
        return Result.success();
    }

    @Operation(summary = "获取推荐商品")
    @GetMapping("/recommend")
    public Result<List<Product>> listRecommend(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.listRecommend(limit));
    }

    @Operation(summary = "获取新品")
    @GetMapping("/new")
    public Result<List<Product>> listNew(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.listNew(limit));
    }

    // ==================== 商品评价 ====================

    @Operation(summary = "创建商品评价")
    @PostMapping("/reviews")
    public Result<ProductReview> createReview(@RequestAttribute("userId") Long userId,
                                               @Valid @RequestBody ReviewCreateRequest request) {
        return Result.success(productReviewService.createReview(userId, request));
    }

    @Operation(summary = "商品评价列表")
    @GetMapping("/{productId}/reviews")
    public Result<PageResult<ProductReview>> productReviews(@PathVariable Long productId,
                                                              @RequestParam(defaultValue = "1") int pageNum,
                                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productReviewService.getProductReviews(productId, pageNum, pageSize));
    }

    @Operation(summary = "商品评分统计")
    @GetMapping("/{productId}/reviews/stats")
    public Result<Map<String, Object>> reviewStats(@PathVariable Long productId) {
        return Result.success(productReviewService.getReviewStats(productId));
    }

    @Operation(summary = "用户评价列表")
    @GetMapping("/reviews/my")
    public Result<PageResult<ProductReview>> myReviews(@RequestAttribute("userId") Long userId,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productReviewService.getUserReviews(userId, pageNum, pageSize));
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/reviews/{id}")
    public Result<Void> deleteReview(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        productReviewService.deleteReview(id, userId);
        return Result.success();
    }
}
