package com.shop.product.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Brand;
import com.shop.product.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌控制器
 *
 * @author shop
 * @since 2026-04-22
 */
@Tag(name = "品牌管理")
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @Operation(summary = "创建品牌")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Brand> create(@RequestBody Brand brand) {
        return Result.success(brandService.create(brand));
    }

    @Operation(summary = "更新品牌")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Brand> update(@PathVariable Long id, @RequestBody Brand brand) {
        return Result.success(brandService.update(id, brand));
    }

    @Operation(summary = "删除品牌")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取品牌详情")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/{id}")
    public Result<Brand> getById(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }

    @Operation(summary = "分页查询品牌")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<PageResult<Brand>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(brandService.page(pageNum, pageSize));
    }

    @Operation(summary = "获取所有启用品牌")
    @GetMapping("/all")
    public Result<List<Brand>> listAll() {
        return Result.success(brandService.listAll());
    }
}
