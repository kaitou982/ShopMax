package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
        };
    }
}
