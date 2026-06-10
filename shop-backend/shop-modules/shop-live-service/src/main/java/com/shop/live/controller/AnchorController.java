package com.shop.live.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.live.controller.request.AnchorApplyRequest;
import com.shop.live.controller.request.AnchorAuditRequest;
import com.shop.live.controller.response.AnchorResponse;
import com.shop.live.service.AnchorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "主播管理")
@RestController
@RequestMapping("/api/v1/live/anchors")
@RequiredArgsConstructor
public class AnchorController {

    private final AnchorService anchorService;

    @Operation(summary = "申请成为主播")
    @PostMapping("/apply")
    public Result<AnchorResponse> apply(@RequestParam Long userId,
                                        @Valid @RequestBody AnchorApplyRequest request) {
        return Result.success(anchorService.apply(userId, request));
    }

    @Operation(summary = "审核主播")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AnchorResponse> audit(@PathVariable Long id,
                                        @Valid @RequestBody AnchorAuditRequest request) {
        return Result.success(anchorService.audit(id, request));
    }

    @Operation(summary = "获取主播详情")
    @GetMapping("/{id}")
    public Result<AnchorResponse> getById(@PathVariable Long id) {
        return Result.success(anchorService.getById(id));
    }

    @Operation(summary = "分页查询主播")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<AnchorResponse>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) Integer status) {
        return Result.success(anchorService.page(pageNum, pageSize, status));
    }

    @Operation(summary = "获取我的主播信息")
    @GetMapping("/my")
    public Result<AnchorResponse> getByUserId(@RequestParam Long userId) {
        return Result.success(anchorService.getByUserId(userId));
    }
}
