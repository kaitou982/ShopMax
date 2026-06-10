package com.shop.user.controller;

import com.shop.common.web.Result;
import com.shop.user.controller.request.UserAddressCreateRequest;
import com.shop.user.controller.request.UserAddressUpdateRequest;
import com.shop.user.controller.response.UserAddressResponse;
import com.shop.user.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收货地址控制器
 *
 * @author shop
 * @since 2026-04-15
 */
@Tag(name = "用户收货地址管理")
@RestController
@RequestMapping("/api/v1/users/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @Operation(summary = "创建收货地址")
    @PostMapping
    public Result<UserAddressResponse> createAddress(@RequestAttribute("userId") Long userId,
                                                      @Valid @RequestBody UserAddressCreateRequest request) {
        return Result.success(userAddressService.createAddress(userId, request));
    }

    @Operation(summary = "更新收货地址")
    @PutMapping("/{id}")
    public Result<UserAddressResponse> updateAddress(@RequestAttribute("userId") Long userId,
                                                      @PathVariable("id") Long addressId,
                                                      @Valid @RequestBody UserAddressUpdateRequest request) {
        return Result.success(userAddressService.updateAddress(userId, addressId, request));
    }

    @Operation(summary = "删除收货地址")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@RequestAttribute("userId") Long userId,
                                       @PathVariable("id") Long addressId) {
        userAddressService.deleteAddress(userId, addressId);
        return Result.success();
    }

    @Operation(summary = "获取收货地址详情")
    @GetMapping("/{id}")
    public Result<UserAddressResponse> getAddressDetail(@RequestAttribute("userId") Long userId,
                                                        @PathVariable("id") Long addressId) {
        return Result.success(userAddressService.getAddressDetail(userId, addressId));
    }

    @Operation(summary = "获取用户收货地址列表")
    @GetMapping
    public Result<List<UserAddressResponse>> getUserAddressList(@RequestAttribute("userId") Long userId) {
        return Result.success(userAddressService.getUserAddressList(userId));
    }

    @Operation(summary = "获取用户默认收货地址")
    @GetMapping("/default")
    public Result<UserAddressResponse> getDefaultAddress(@RequestAttribute("userId") Long userId) {
        return Result.success(userAddressService.getDefaultAddress(userId));
    }

    @Operation(summary = "设置默认收货地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@RequestAttribute("userId") Long userId,
                                          @PathVariable("id") Long addressId) {
        userAddressService.setDefaultAddress(userId, addressId);
        return Result.success();
    }
}
