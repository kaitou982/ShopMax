package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalProductClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class InternalProductClientFallbackFactory implements FallbackFactory<InternalProductClient> {
    @Override
    public InternalProductClient create(Throwable cause) {
        log.error("商品服务内部调用失败: {}", cause.getMessage(), cause);
        return new InternalProductClient() {
            @Override
            public Result<Void> deductStock(Long id, Map<String, Object> request) {
                return Result.error(503, "商品服务暂时不可用");
            }
            @Override
            public Result<Void> restoreStock(Long id, Map<String, Object> request) {
                return Result.error(503, "商品服务暂时不可用");
            }
            @Override
            public Result<Void> addSales(Long id, Map<String, Object> request) {
                return Result.error(503, "商品服务暂时不可用");
            }
        };
    }
}
