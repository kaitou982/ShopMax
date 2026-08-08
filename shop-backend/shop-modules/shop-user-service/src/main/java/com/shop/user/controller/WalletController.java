package com.shop.user.controller;

import com.shop.common.enums.MemberLevelConstants;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.user.controller.request.RechargeRequest;
import com.shop.user.entity.BalanceLog;
import com.shop.user.entity.IntegralLog;
import com.shop.user.entity.User;
import com.shop.user.mapper.UserMapper;
import com.shop.user.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "钱包管理")
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserMapper userMapper;

    @Operation(summary = "会员信息聚合")
    @GetMapping("/member-info")
    public Result<Map<String, Object>> memberInfo(@RequestAttribute("userId") Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        int level = user.getMemberLevel() != null ? user.getMemberLevel() : 1;
        level = Math.min(level, MemberLevelConstants.MAX_LEVEL);
        int growth = user.getGrowthValue() != null ? user.getGrowthValue() : 0;

        int nextLevelGrowth = MemberLevelConstants.getNextLevelGrowth(level);
        String nextLevelName = level < MemberLevelConstants.MAX_LEVEL
                ? MemberLevelConstants.getLevelName(level + 1)
                : MemberLevelConstants.getLevelName(MemberLevelConstants.MAX_LEVEL);

        List<Map<String, Object>> benefits = new ArrayList<>();
        for (int i = 1; i <= MemberLevelConstants.MAX_LEVEL; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("level", i);
            item.put("name", MemberLevelConstants.LEVEL_NAMES[i]);
            item.put("discount", MemberLevelConstants.LEVEL_DISCOUNT_LABELS[i]);
            item.put("threshold", MemberLevelConstants.LEVEL_THRESHOLDS[i - 1]);
            benefits.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memberLevel", level);
        result.put("memberLevelName", MemberLevelConstants.getLevelName(level));
        result.put("integral", user.getIntegral() != null ? user.getIntegral() : 0);
        result.put("balance", user.getBalance() != null ? user.getBalance() : 0);
        result.put("growthValue", growth);
        result.put("nextLevelGrowth", nextLevelGrowth);
        result.put("nextLevelName", nextLevelName);
        result.put("levelBenefits", benefits);

        return Result.success(result);
    }

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
