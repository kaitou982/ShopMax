package com.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.admin.entity.Banner;
import com.shop.admin.mapper.BannerMapper;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公开Banner")
@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class PublicBannerController {

    private final BannerMapper bannerMapper;

    @Operation(summary = "获取启用中的Banner列表")
    @GetMapping(value = {"", "/", "/active"})
    public Result<List<Banner>> activeList() {
        LambdaQueryWrapper<Banner> w = new LambdaQueryWrapper<>();
        w.eq(Banner::getStatus, 1).orderByAsc(Banner::getSort);
        return Result.success(bannerMapper.selectList(w));
    }
}
