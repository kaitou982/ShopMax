package com.shop.marketing.controller;

import com.shop.common.web.Result;
import com.shop.marketing.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 优惠券内部接口（供其他微服务通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/coupons")
@RequiredArgsConstructor
public class InternalCouponController {

    private final CouponService couponService;

    @PostMapping("/use")
    public Result<Void> useCoupon(@RequestBody Map<String, Object> request) {
        Long id = ((Number) request.get("id")).longValue();
        Long userId = ((Number) request.get("userId")).longValue();
        Long orderId = ((Number) request.get("orderId")).longValue();
        String orderNo = (String) request.get("orderNo");
        couponService.useCoupon(id, userId, orderId, orderNo);
        return Result.success();
    }

    @GetMapping("/detail")
    public Result<Map<String, Object>> getCouponDetail(@RequestParam Long id, @RequestParam Long userId) {
        Map<String, Object> detail = couponService.getCouponDetail(id, userId);
        return Result.success(detail);
    }

    @PostMapping("/restore")
    public Result<Void> restoreCoupon(@RequestBody Map<String, Object> request) {
        Long id = ((Number) request.get("id")).longValue();
        Long userId = ((Number) request.get("userId")).longValue();
        couponService.restoreCoupon(id, userId);
        return Result.success();
    }
}
