package com.shop.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.user.controller.request.UserAddressCreateRequest;
import com.shop.user.controller.request.UserAddressUpdateRequest;
import com.shop.user.controller.response.UserAddressResponse;
import com.shop.user.entity.UserAddress;
import com.shop.user.mapper.UserAddressMapper;
import com.shop.user.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户地址服务实现
 *
 * @author shop
 * @since 2026-04-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    private final UserAddressMapper userAddressMapper;

    // 最大地址数量
    private static final int MAX_ADDRESS_COUNT = 20;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddressResponse createAddress(Long userId, UserAddressCreateRequest request) {
        // 检查地址数量限制
        int count = userAddressMapper.countByUserId(userId);
        if (count >= MAX_ADDRESS_COUNT) {
            throw new BusinessException("收货地址数量已达到上限");
        }

        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(request, address);
        address.setUserId(userId);

        // 如果是第一个地址或设置为默认，更新其他地址为非默认
        if (count == 0 || Boolean.TRUE.equals(request.getIsDefault())) {
            if (count > 0) {
                userAddressMapper.cancelDefaultByUserId(userId);
            }
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        baseMapper.insert(address);

        log.info("创建收货地址成功: userId={}, addressId={}", userId, address.getId());

        return convertToResponse(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddressResponse updateAddress(Long userId, Long addressId, UserAddressUpdateRequest request) {
        UserAddress address = getAddressEntity(userId, addressId);

        // 更新字段
        if (StringUtils.hasText(request.getReceiverName())) {
            address.setReceiverName(request.getReceiverName());
        }
        if (StringUtils.hasText(request.getReceiverPhone())) {
            address.setReceiverPhone(request.getReceiverPhone());
        }
        if (StringUtils.hasText(request.getProvince())) {
            address.setProvince(request.getProvince());
        }
        if (StringUtils.hasText(request.getProvinceCode())) {
            address.setProvinceCode(request.getProvinceCode());
        }
        if (StringUtils.hasText(request.getCity())) {
            address.setCity(request.getCity());
        }
        if (StringUtils.hasText(request.getCityCode())) {
            address.setCityCode(request.getCityCode());
        }
        if (StringUtils.hasText(request.getDistrict())) {
            address.setDistrict(request.getDistrict());
        }
        if (StringUtils.hasText(request.getDistrictCode())) {
            address.setDistrictCode(request.getDistrictCode());
        }
        if (StringUtils.hasText(request.getDetailAddress())) {
            address.setDetailAddress(request.getDetailAddress());
        }
        if (StringUtils.hasText(request.getPostalCode())) {
            address.setPostalCode(request.getPostalCode());
        }
        if (StringUtils.hasText(request.getLabel())) {
            address.setLabel(request.getLabel());
        }
        if (request.getLongitude() != null) {
            address.setLongitude(request.getLongitude());
        }
        if (request.getLatitude() != null) {
            address.setLatitude(request.getLatitude());
        }

        // 处理默认地址
        if (request.getIsDefault() != null) {
            if (Boolean.TRUE.equals(request.getIsDefault()) && address.getIsDefault() == 0) {
                userAddressMapper.cancelDefaultByUserId(userId);
                address.setIsDefault(1);
            } else if (Boolean.FALSE.equals(request.getIsDefault())) {
                address.setIsDefault(0);
            }
        }

        baseMapper.updateById(address);

        log.info("更新收货地址成功: userId={}, addressId={}", userId, addressId);

        return convertToResponse(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = getAddressEntity(userId, addressId);
        baseMapper.deleteById(address.getId());

        log.info("删除收货地址成功: userId={}, addressId={}", userId, addressId);
    }

    @Override
    public UserAddressResponse getAddressDetail(Long userId, Long addressId) {
        UserAddress address = getAddressEntity(userId, addressId);
        return convertToResponse(address);
    }

    @Override
    public List<UserAddressResponse> getUserAddressList(Long userId) {
        List<UserAddress> list = userAddressMapper.selectByUserId(userId);
        return list.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressResponse getDefaultAddress(Long userId) {
        UserAddress address = userAddressMapper.selectDefaultByUserId(userId);
        if (address == null) {
            return null;
        }
        return convertToResponse(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = getAddressEntity(userId, addressId);

        if (address.getIsDefault() == 1) {
            return; // 已经是默认地址
        }

        userAddressMapper.cancelDefaultByUserId(userId);
        address.setIsDefault(1);
        baseMapper.updateById(address);

        log.info("设置默认地址成功: userId={}, addressId={}", userId, addressId);
    }

    private UserAddress getAddressEntity(Long userId, Long addressId) {
        UserAddress address = baseMapper.selectById(addressId);
        if (address == null || address.getDeleted() == 1) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该地址");
        }
        return address;
    }

    private UserAddressResponse convertToResponse(UserAddress address) {
        UserAddressResponse response = new UserAddressResponse();
        BeanUtils.copyProperties(address, response);
        response.setAddressId(address.getId());
        return response;
    }
}
