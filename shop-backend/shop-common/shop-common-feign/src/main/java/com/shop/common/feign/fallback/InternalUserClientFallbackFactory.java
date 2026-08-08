package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InternalUserClientFallbackFactory implements FallbackFactory<InternalUserClient> {
    @Override
    public InternalUserClient create(Throwable cause) {
        log.error("用户服务内部调用失败: {}", cause.getMessage(), cause);
        return new InternalUserClient() {
            @Override
            public Result<Integer> getMemberLevel(Long id) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Void> deductIntegral(Long id, Map<String, Object> request) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Void> addIntegral(Long id, Map<String, Object> request) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Void> deductBalance(Long id, Map<String, Object> request) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Void> addBalance(Long id, Map<String, Object> request) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Void> addGrowthValue(Long id, Map<String, Object> request) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Void> refundBalance(Long id, Map<String, Object> request) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getRegisterStats() {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Integer> getIntegral(Long id) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getUserBasicInfo(Long id) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<List<Long>> getFollowingUserIds(Long id) {
                return Result.error(503, "用户服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getBatchBasicInfo(List<Long> ids) {
                return Result.error(503, "用户服务暂时不可用");
            }
        };
    }
}
