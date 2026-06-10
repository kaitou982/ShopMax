package com.shop.user.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.user.controller.request.RechargeRequest;
import com.shop.user.entity.BalanceLog;
import com.shop.user.entity.IntegralLog;
import com.shop.user.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "钱包管理")
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "余额充值")
    @PostMapping("/recharge")
    public Result<Map<String, Object>> recharge(@RequestAttribute("userId") Long userId,
                                                 @Valid @RequestBody RechargeRequest request) {
        walletService.changeBalance(userId, request.getAmount(), 1, null, request.getPayChannel(), "余额充值");
        return Result.success(Map.of("message", "充值成功"));
    }

    @Operation(summary = "积分流水")
    @GetMapping("/integral-logs")
    public Result<PageResult<IntegralLog>> integralLogs(@RequestAttribute("userId") Long userId,
                                                         @RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(walletService.integralLogs(userId, pageNum, pageSize));
    }

    @Operation(summary = "余额流水")
    @GetMapping("/balance-logs")
    public Result<PageResult<BalanceLog>> balanceLogs(@RequestAttribute("userId") Long userId,
                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(walletService.balanceLogs(userId, pageNum, pageSize));
    }
}
