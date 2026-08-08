package com.shop.community.service;

import com.shop.common.feign.client.InternalProductClient;
import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 社区服务跨服务数据访问 —— 通过 Feign 调用 user-service / product-service，
 * 替代原来直查其他数据库表的 UserInfoMapper。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityExternalService {

    private final InternalUserClient internalUserClient;
    private final InternalProductClient internalProductClient;

    public record UserInfo(Long id, String nickname, String avatar) {}

    public record ProductInfo(Long id, String name, String mainImage, java.math.BigDecimal salePrice) {}

    /**
     * 批量获取用户昵称和头像
     */
    public Map<Long, UserInfo> batchGetUserInfo(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        try {
            Result<Map<String, Object>> result = internalUserClient.getBatchBasicInfo(userIds);
            if (result == null || result.getData() == null) {
                return Map.of();
            }
            Map<Long, UserInfo> map = new HashMap<>();
            result.getData().forEach((key, value) -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> info = (Map<String, Object>) value;
                map.put(Long.valueOf(key), new UserInfo(
                        Long.valueOf(key),
                        (String) info.get("nickname"),
                        (String) info.get("avatar")
                ));
            });
            return map;
        } catch (Exception e) {
            log.warn("批量获取用户信息失败: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 批量获取商品基本信息
     */
    public Map<Long, ProductInfo> batchGetProductInfo(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        try {
            Result<Map<String, Object>> result = internalProductClient.getBatchProductInfo(productIds);
            if (result == null || result.getData() == null) {
                return Map.of();
            }
            Map<Long, ProductInfo> map = new HashMap<>();
            result.getData().forEach((key, value) -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> info = (Map<String, Object>) value;
                java.math.BigDecimal price = java.math.BigDecimal.ZERO;
                Object priceObj = info.get("salePrice");
                if (priceObj instanceof Number n) {
                    price = java.math.BigDecimal.valueOf(n.doubleValue());
                }
                map.put(Long.valueOf(key), new ProductInfo(
                        Long.valueOf(key),
                        (String) info.get("name"),
                        (String) info.get("mainImage"),
                        price
                ));
            });
            return map;
        } catch (Exception e) {
            log.warn("批量获取商品信息失败: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 获取用户关注的用户 ID 列表（暂未实现，user-service 返回空列表）
     */
    public List<Long> getFollowingUserIds(Long userId) {
        try {
            Result<List<Long>> result = internalUserClient.getFollowingUserIds(userId);
            return result != null && result.getData() != null ? result.getData() : List.of();
        } catch (Exception e) {
            log.warn("获取关注列表失败: {}", e.getMessage());
            return List.of();
        }
    }
}
