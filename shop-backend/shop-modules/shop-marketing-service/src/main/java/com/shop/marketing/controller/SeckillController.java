package com.shop.marketing.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.marketing.controller.request.SeckillOrderRequest;
import com.shop.marketing.controller.request.SeckillProductCreateRequest;
import com.shop.marketing.controller.request.SeckillSessionCreateRequest;
import com.shop.marketing.controller.response.SeckillProductResponse;
import com.shop.marketing.controller.response.SeckillSessionResponse;
import com.shop.marketing.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "秒杀管理")
@RestController
@RequestMapping("/api/v1/marketing/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "创建秒杀场次")
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SeckillSessionResponse> createSession(@Valid @RequestBody SeckillSessionCreateRequest request) {
        return Result.success(seckillService.createSession(request));
    }

    @Operation(summary = "秒杀场次列表")
    @GetMapping("/sessions")
    public Result<PageResult<SeckillSessionResponse>> pageSessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(seckillService.pageSessions(pageNum, pageSize));
    }

    @Operation(summary = "进行中的场次")
    @GetMapping("/sessions/active")
    public Result<List<SeckillSessionResponse>> listActiveSessions() {
        return Result.success(seckillService.listActiveSessions());
    }

    @Operation(summary = "添加秒杀商品")
    @PostMapping("/sessions/{sessionId}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SeckillProductResponse> addProduct(@PathVariable Long sessionId,
                                                      @Valid @RequestBody SeckillProductCreateRequest request) {
        return Result.success(seckillService.addProduct(sessionId, request));
    }

    @Operation(summary = "秒杀商品列表")
    @GetMapping("/products")
    public Result<List<SeckillProductResponse>> getProducts(@RequestParam Long sessionId) {
        return Result.success(seckillService.getSessionProducts(sessionId));
    }

    @Operation(summary = "加载库存到Redis")
    @PostMapping("/sessions/{sessionId}/load-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> loadStock(@PathVariable Long sessionId) {
        seckillService.loadStockToRedis(sessionId);
        return Result.success();
    }

    @Operation(summary = "执行秒杀")
    @PostMapping("/execute")
    public Result<Void> executeSeckill(@RequestAttribute("userId") Long userId,
                                        @Valid @RequestBody SeckillOrderRequest request) {
        seckillService.executeSeckill(request.getSessionId(), request.getProductId(), userId);
        return Result.success();
    }
}
