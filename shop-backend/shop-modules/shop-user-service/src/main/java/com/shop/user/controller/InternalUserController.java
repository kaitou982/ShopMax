package com.shop.user.controller;

import com.shop.common.web.Result;
import com.shop.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户服务内部接口（供其他微服务通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{id}/member-level")
    public Result<Integer> getMemberLevel(@PathVariable Long id) {
        Integer level = userService.getMemberLevel(id);
        return Result.success(level);
    }

    @PostMapping("/{id}/deduct-integral")
    public Result<Void> deductIntegral(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer amount = (Integer) request.get("amount");
        String description = (String) request.get("description");
        userService.deductIntegral(id, amount, description);
        return Result.success();
    }

    @PostMapping("/{id}/add-integral")
    public Result<Void> addIntegral(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer amount = (Integer) request.get("amount");
        String description = (String) request.get("description");
        userService.addIntegral(id, amount, description);
        return Result.success();
    }

    @PostMapping("/{id}/deduct-balance")
    public Result<Void> deductBalance(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String description = (String) request.get("description");
        userService.deductBalance(id, amount, description);
        return Result.success();
    }

    @PostMapping("/{id}/add-balance")
    public Result<Void> addBalance(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String description = (String) request.get("description");
        userService.addBalance(id, amount, description);
        return Result.success();
    }

    @PostMapping("/{id}/add-growth")
    public Result<Void> addGrowthValue(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer amount = (Integer) request.get("amount");
        userService.addGrowthValue(id, amount);
        return Result.success();
    }

    @PostMapping("/{id}/refund-balance")
    public Result<Void> refundBalance(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String description = (String) request.get("description");
        String bizId = (String) request.get("bizId");
        userService.refundBalance(id, amount, description, bizId);
        return Result.success();
    }

    @GetMapping("/register-stats")
    public Result<Map<String, Object>> getRegisterStats() {
        Map<String, Object> stats = userService.getRegisterStats();
        return Result.success(stats);
    }

    @GetMapping("/{id}/integral")
    public Result<Integer> getIntegral(@PathVariable Long id) {
        Integer integral = userService.getIntegral(id);
        return Result.success(integral);
    }

    @GetMapping("/{id}/basic-info")
    public Result<Map<String, Object>> getUserBasicInfo(@PathVariable Long id) {
        Map<String, Object> info = userService.getUserBasicInfo(id);
        return Result.success(info);
    }

    @GetMapping("/batch-basic-info")
    public Result<Map<String, Object>> getBatchBasicInfo(@RequestParam("ids") List<Long> ids) {
        Map<String, Object> info = userService.getBatchBasicInfo(ids);
        return Result.success(info);
    }

    @GetMapping("/{id}/following")
    public Result<List<Long>> getFollowingUserIds(@PathVariable Long id) {
        List<Long> ids = userService.getFollowingUserIds(id);
        return Result.success(ids);
    }
}
