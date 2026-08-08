package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalCouponClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class InternalCouponClientFallbackFactory implements FallbackFactory<InternalCouponClient> {
    @Override
    public InternalCouponClient create(Throwable cause) {
        log.error("营销服务内部调用失败: {}", cause.getMessage(), cause);
        return new InternalCouponClient() {
            @Override
            public Result<Void> useCoupon(Map<String, Object> request) {
                return Result.error(503, "营销服务暂时不可用");
            }
            @Override
            public Result<Map<String, Object>> getCouponDetail(Long id, Long userId) {
                return Result.error(503, "营销服务暂时不可用");
            }
            @Override
            public Result<Void> restoreCoupon(Map<String, Object> request) {
                return Result.error(503, "营销服务暂时不可用");
            }
        };
    }
}
