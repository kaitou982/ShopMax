package com.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.admin.entity.NewProductBanner;
import com.shop.admin.mapper.NewProductBannerMapper;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 新品Banner管理控制器（Admin端）
 *
 * @author shop
 * @since 2026-06-17
 */
@Tag(name = "新品Banner管理")
@RestController
@RequestMapping("/api/v1/admin/new-product-banners")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NewProductBannerAdminController {

    private final NewProductBannerMapper bannerMapper;

    @Operation(summary = "Banner分页列表")
    @GetMapping
    public Result<PageResult<NewProductBanner>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<NewProductBanner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NewProductBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewProductBanner::getDeleted, 0);
        wrapper.orderByDesc(NewProductBanner::getSort);
        Page<NewProductBanner> result = bannerMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getPages()));
    }

    @Operation(summary = "新增Banner")
    @PostMapping
    public Result<NewProductBanner> create(@RequestBody NewProductBanner banner) {
        // 清除敏感字段，防止客户端注入
        banner.setId(null);
        banner.setCreateTime(null);
        banner.setUpdateTime(null);
        banner.setDeleted(0);
        if (banner.getTitle() == null || banner.getTitle().isBlank()) {
            return Result.badRequest("标题不能为空");
        }
        if (banner.getImageUrl() == null || banner.getImageUrl().isBlank()) {
            return Result.badRequest("图片URL不能为空");
        }
        bannerMapper.insert(banner);
        return Result.success(banner);
    }

    @Operation(summary = "编辑Banner")
    @PutMapping("/{id}")
    public Result<NewProductBanner> update(@PathVariable Long id, @RequestBody NewProductBanner banner) {
        NewProductBanner existing = bannerMapper.selectById(id);
        if (existing == null) {
            return Result.badRequest("Banner不存在");
        }
        banner.setId(id);
        bannerMapper.updateById(banner);
        return Result.success(banner);
    }

    @Operation(summary = "删除Banner")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        NewProductBanner existing = bannerMapper.selectById(id);
        if (existing == null) {
            return Result.badRequest("Banner不存在");
        }
        bannerMapper.deleteById(id);
        return Result.success();
    }
}
