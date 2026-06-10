package com.shop.user.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.user.controller.request.*;
import com.shop.user.controller.response.*;
import com.shop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 *
 * @author shop
 * @since 2026-04-15
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUserInfo(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getCurrentUserInfo(userId));
    }

    @Operation(summary = "更新当前用户信息")
    @PutMapping("/me")
    public Result<UserInfoResponse> updateCurrentUserInfo(@RequestAttribute("userId") Long userId,
                                                          @Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.updateUserInfo(userId, request));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    public Result<Void> changePassword(@RequestAttribute("userId") Long userId,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return Result.success();
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserInfoResponse>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     UserQueryRequest request) {
        return Result.success(userService.pageUsers(pageNum, pageSize, request));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserInfoResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserInfoResponse> update(@PathVariable Long id,
                                           @Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.update(id, request));
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "列表查询用户")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserInfoResponse>> list() {
        return Result.success(userService.listUsers());
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserInfoResponse> create(@Valid @RequestBody UserRegisterRequest request) {
        // 管理员创建用户，复用注册逻辑但返回用户信息
        UserRegisterResponse registerResponse = userService.register(request);
        // 查询并返回完整用户信息
        return Result.success(userService.getUserById(registerResponse.getUserId()));
    }

    @Operation(summary = "分页查询待审核店家")
    @GetMapping("/store-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserInfoResponse>> pageStoreApplications(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.pageStoreApplications(pageNum, pageSize));
    }

    @Operation(summary = "审核店家入驻")
    @PutMapping("/{id}/store-audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditStore(@PathVariable Long id,
                                   @Valid @RequestBody StoreAuditRequest request) {
        userService.auditStore(id, request);
        return Result.success();
    }
}