package com.shop.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.feign.dto.product.CategorySimpleResponse;
import com.shop.common.feign.dto.product.ProductSimpleResponse;
import com.shop.common.web.Result;
import com.shop.product.entity.Category;
import com.shop.product.entity.Product;
import com.shop.product.mapper.CategoryMapper;
import com.shop.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务内部接口（供其他微服务通过 Feign 调用，不经过 Gateway 路由）
 *
 * @author shop
 * @since 2026-06-17
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class ProductInternalController {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @GetMapping("/products/search")
    public Result<List<ProductSimpleResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "5") Integer pageSize) {

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getDeleted, 0);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getName, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (maxPrice != null && maxPrice > 0) {
            wrapper.le(Product::getSalePrice, BigDecimal.valueOf(maxPrice));
        }

        if ("sales".equals(sortBy)) {
            wrapper.orderByDesc(Product::getSales);
        } else {
            wrapper.orderByDesc(Product::getSales);
        }
        wrapper.last("LIMIT " + pageSize);

        List<ProductSimpleResponse> result = productMapper.selectList(wrapper).stream()
                .map(p -> {
                    ProductSimpleResponse r = new ProductSimpleResponse();
                    r.setId(p.getId());
                    r.setName(p.getName());
                    r.setSalePrice(p.getSalePrice());
                    r.setOriginalPrice(p.getOriginalPrice());
                    r.setStock(p.getStock());
                    r.setSales(p.getSales());
                    r.setMainImage(p.getMainImage());
                    return r;
                })
                .toList();

        return Result.success(result);
    }

    @GetMapping("/categories/search")
    public Result<List<CategorySimpleResponse>> searchCategories(@RequestParam String name) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .eq(Category::getDeleted, 0)
               .like(Category::getName, name);

        List<CategorySimpleResponse> result = categoryMapper.selectList(wrapper).stream()
                .map(c -> {
                    CategorySimpleResponse r = new CategorySimpleResponse();
                    r.setId(c.getId());
                    r.setName(c.getName());
                    return r;
                })
                .toList();

        return Result.success(result);
    }
}
