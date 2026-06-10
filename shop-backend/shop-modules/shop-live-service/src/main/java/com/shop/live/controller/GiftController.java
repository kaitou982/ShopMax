package com.shop.live.controller;

import com.shop.common.web.Result;
import com.shop.live.entity.Gift;
import com.shop.live.service.GiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 礼物控制器
 */
@Tag(name = "礼物管理")
@RestController
@RequestMapping("/api/v1/live/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @Operation(summary = "获取所有礼物列表")
    @GetMapping
    public Result<List<Gift>> list() {
        return Result.success(giftService.listAll());
    }

    @Operation(summary = "获取用户虚拟币余额")
    @GetMapping("/balance")
    public Result<Integer> getBalance(@RequestAttribute("userId") Long userId) {
        return Result.success(giftService.getCoinBalance(userId));
    }
}
