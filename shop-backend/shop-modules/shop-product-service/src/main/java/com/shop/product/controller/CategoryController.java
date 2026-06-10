package com.shop.product.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Category;
import com.shop.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author shop
 * @since 2026-04-22
 */
@Tag(name = "商品分类管理")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "创建分类")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> create(@RequestBody Category category) {
        return Result.success(categoryService.create(category));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return Result.success(categoryService.update(id, category));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取分类详情")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @Operation(summary = "分页查询分类")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public Result<PageResult<Category>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long parentId) {
        return Result.success(categoryService.page(pageNum, pageSize, parentId));
    }

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<Category>> getTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "获取子分类列表")
    @GetMapping("/{parentId}/children")
    public Result<List<Category>> getChildren(@PathVariable Long parentId) {
        return Result.success(categoryService.listChildren(parentId));
    }
}
