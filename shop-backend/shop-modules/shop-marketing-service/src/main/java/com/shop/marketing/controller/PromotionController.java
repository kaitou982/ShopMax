package com.shop.marketing.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.marketing.controller.request.PromotionCreateRequest;
import com.shop.marketing.controller.request.PromotionUpdateRequest;
import com.shop.marketing.controller.response.PromotionResponse;
import com.shop.marketing.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "促销活动管理")
@RestController
@RequestMapping("/api/v1/marketing/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @Operation(summary = "创建促销活动")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PromotionResponse> create(@Valid @RequestBody PromotionCreateRequest request) {
        return Result.success(promotionService.create(request));
    }

    @Operation(summary = "更新促销活动")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PromotionResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody PromotionUpdateRequest request) {
        return Result.success(promotionService.update(id, request));
    }

    @Operation(summary = "删除促销活动")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取促销活动详情")
    @GetMapping("/{id}")
    public Result<PromotionResponse> getById(@PathVariable Long id) {
        return Result.success(promotionService.getById(id));
    }

    @Operation(summary = "分页查询促销活动")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<PromotionResponse>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                                       @RequestParam(required = false) Integer status) {
        return Result.success(promotionService.page(pageNum, pageSize, status));
    }

    @Operation(summary = "进行中的促销活动列表")
    @GetMapping("/list")
    public Result<List<PromotionResponse>> listActive() {
        return Result.success(promotionService.listActive());
    }

    @Operation(summary = "启用促销活动")
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> enable(@PathVariable Long id) {
        promotionService.enable(id);
        return Result.success();
    }

    @Operation(summary = "停用促销活动")
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> disable(@PathVariable Long id) {
        promotionService.disable(id);
        return Result.success();
    }
}
