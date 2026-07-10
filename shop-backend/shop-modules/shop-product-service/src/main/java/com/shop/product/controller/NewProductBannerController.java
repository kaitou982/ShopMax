package com.shop.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.web.Result;
import com.shop.product.entity.NewProductBanner;
import com.shop.product.mapper.NewProductBannerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新品首发Banner控制器（C端）
 *
 * @author shop
 * @since 2026-06-17
 */
@Tag(name = "新品Banner")
@RestController
@RequestMapping("/api/v1/products/new-product-banners")
@RequiredArgsConstructor
public class NewProductBannerController {

    private final NewProductBannerMapper bannerMapper;

    @Operation(summary = "获取新品Banner列表")
    @GetMapping
    public Result<List<NewProductBanner>> list() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<NewProductBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewProductBanner::getStatus, 1);
        wrapper.eq(NewProductBanner::getDeleted, 0);
        // 时间过滤
        wrapper.and(w -> w
                .isNull(NewProductBanner::getStartTime)
                .or().le(NewProductBanner::getStartTime, now)
        );
        wrapper.and(w -> w
                .isNull(NewProductBanner::getEndTime)
                .or().ge(NewProductBanner::getEndTime, now)
        );
        wrapper.orderByDesc(NewProductBanner::getSort);
        return Result.success(bannerMapper.selectList(wrapper));
    }
}
