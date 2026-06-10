package com.shop.marketing.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.marketing.controller.request.CouponCreateRequest;
import com.shop.marketing.controller.request.CouponReceiveRequest;
import com.shop.marketing.controller.request.CouponUpdateRequest;
import com.shop.marketing.controller.response.CouponReceiveResponse;
import com.shop.marketing.controller.response.CouponResponse;
import com.shop.marketing.service.CouponReceiveService;
import com.shop.marketing.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "优惠券管理")
@RestController
@RequestMapping("/api/v1/marketing/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponReceiveService couponReceiveService;
    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    @Operation(summary = "创建优惠券")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CouponResponse> create(@Valid @RequestBody CouponCreateRequest request) {
        return Result.success(couponService.create(request));
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CouponResponse> update(@PathVariable Long id,
                                         @Valid @RequestBody CouponUpdateRequest request) {
        return Result.success(couponService.update(id, request));
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{id}")
    public Result<CouponResponse> getById(@PathVariable Long id) {
        return Result.success(couponService.getById(id));
    }

    @Operation(summary = "分页查询优惠券")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<CouponResponse>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(couponService.page(pageNum, pageSize));
    }

    @Operation(summary = "可用优惠券列表")
    @GetMapping("/list")
    public Result<PageResult<CouponResponse>> listAvailable() {
        return Result.success(couponService.listAvailable());
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/receive")
    public Result<Void> receive(@RequestAttribute("userId") Long userId,
                                 @Valid @RequestBody CouponReceiveRequest request) {
        couponService.receive(request.getCouponId(), userId);
        return Result.success();
    }

    @Operation(summary = "我的优惠券")
    @GetMapping("/my")
    public Result<PageResult<CouponReceiveResponse>> myCoupons(@RequestAttribute("userId") Long userId,
                                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                                @RequestParam(required = false) Integer status) {
        return Result.success(couponReceiveService.pageByUserId(userId, pageNum, pageSize, status));
    }

    @Operation(summary = "积分兑换优惠券")
    @PostMapping("/exchange")
    public Result<Void> exchange(@RequestAttribute("userId") Long userId,
                                  @RequestBody CouponReceiveRequest request) {
        couponService.exchangeWithIntegral(request.getCouponId(), userId);
        return Result.success();
    }

    @Operation(summary = "管理员发放优惠券给用户")
    @PostMapping("/{couponId}/grant")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> grantToUsers(@PathVariable Long couponId,
                                                     @RequestBody Map<String, List<Long>> body) {
        List<Long> userIds = body.get("userIds");
        int success = 0, failed = 0;
        for (Long userId : userIds) {
            try {
                couponService.grantToUser(couponId, userId);
                success++;
            } catch (Exception e) {
                log.warn("发放优惠券失败: couponId={}, userId={}, error={}", couponId, userId, e.getMessage());
                failed++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("total", userIds.size());
        return Result.success(result);
    }

    @Operation(summary = "优惠券领取记录")
    @GetMapping("/{couponId}/records")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<CouponReceiveResponse>> records(@PathVariable Long couponId,
                                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                                              @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(couponReceiveService.pageByCouponId(couponId, pageNum, pageSize));
    }

    @Operation(summary = "优惠券统计")
    @GetMapping("/{couponId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> stats(@PathVariable Long couponId) {
        return Result.success(couponService.getStats(couponId));
    }

    @Operation(summary = "优惠券领取趋势")
    @GetMapping("/{couponId}/trend")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> trend(@PathVariable Long couponId) {
        return Result.success(couponService.getTrend(couponId));
    }
}
