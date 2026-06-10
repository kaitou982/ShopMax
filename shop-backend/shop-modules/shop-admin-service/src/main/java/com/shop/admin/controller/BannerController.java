package com.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.admin.entity.Banner;
import com.shop.admin.mapper.BannerMapper;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Banner管理")
@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerMapper bannerMapper;

    @Operation(summary = "Banner列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Banner>> list() {
        LambdaQueryWrapper<Banner> w = new LambdaQueryWrapper<>();
        w.orderByAsc(Banner::getSort);
        return Result.success(bannerMapper.selectList(w));
    }

    @Operation(summary = "创建Banner")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Banner> create(@RequestBody Banner banner) {
        bannerMapper.insert(banner);
        return Result.success(banner);
    }

    @Operation(summary = "更新Banner")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerMapper.updateById(banner);
        return Result.success(banner);
    }

    @Operation(summary = "删除Banner")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "前台Banner列表(公开)")
    @GetMapping("/active")
    public Result<List<Banner>> activeList() {
        LambdaQueryWrapper<Banner> w = new LambdaQueryWrapper<>();
        w.eq(Banner::getStatus, 1).orderByAsc(Banner::getSort);
        return Result.success(bannerMapper.selectList(w));
    }
}
