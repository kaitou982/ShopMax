package com.shop.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.user.controller.request.UserAddressCreateRequest;
import com.shop.user.controller.request.UserAddressUpdateRequest;
import com.shop.user.controller.response.UserAddressResponse;
import com.shop.user.entity.UserAddress;

import java.util.List;

/**
 * 用户地址服务接口
 *
 * @author shop
 * @since 2026-04-15
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 创建地址
     */
    UserAddressResponse createAddress(Long userId, UserAddressCreateRequest request);

    /**
     * 更新地址
     */
    UserAddressResponse updateAddress(Long userId, Long addressId, UserAddressUpdateRequest request);

    /**
     * 删除地址
     */
    void deleteAddress(Long userId, Long addressId);

    /**
     * 获取地址详情
     */
    UserAddressResponse getAddressDetail(Long userId, Long addressId);

    /**
     * 获取用户地址列表
     */
    List<UserAddressResponse> getUserAddressList(Long userId);

    /**
     * 获取用户默认地址
     */
    UserAddressResponse getDefaultAddress(Long userId);

    /**
     * 设置默认地址
     */
    void setDefaultAddress(Long userId, Long addressId);
}
