package com.shop.live.controller;

import com.shop.common.web.Result;
import com.shop.live.controller.request.LiveProductAddRequest;
import com.shop.live.controller.request.LiveProductUpdateRequest;
import com.shop.live.controller.response.LiveProductResponse;
import com.shop.live.service.LiveProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "直播商品管理")
@RestController
@RequestMapping("/api/v1/live/products")
@RequiredArgsConstructor
public class LiveProductController {

    private final LiveProductService liveProductService;

    @Operation(summary = "直播间上架商品")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LiveProductResponse> addProduct(@Valid @RequestBody LiveProductAddRequest request) {
        return Result.success(liveProductService.addProduct(request));
    }

    @Operation(summary = "下架商品")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> removeProduct(@PathVariable Long id) {
        liveProductService.removeProduct(id);
        return Result.success();
    }

    @Operation(summary = "更新商品信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LiveProductResponse> updateProduct(@PathVariable Long id,
                                                      @Valid @RequestBody LiveProductUpdateRequest request) {
        return Result.success(liveProductService.updateProduct(id, request));
    }

    @Operation(summary = "直播间商品列表")
    @GetMapping("/room/{roomId}")
    public Result<List<LiveProductResponse>> getRoomProducts(@PathVariable Long roomId) {
        return Result.success(liveProductService.getRoomProducts(roomId));
    }

    @Operation(summary = "标记为讲解中")
    @PutMapping("/{id}/explain")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> setExplaining(@PathVariable Long id) {
        liveProductService.setExplaining(id);
        return Result.success();
    }

    @Operation(summary = "取消讲解")
    @PutMapping("/{id}/unexplain")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> unexplain(@PathVariable Long id) {
        liveProductService.unexplain(id);
        return Result.success();
    }
}
