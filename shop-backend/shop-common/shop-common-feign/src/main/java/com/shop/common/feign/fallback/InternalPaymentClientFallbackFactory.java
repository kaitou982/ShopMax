package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalPaymentClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InternalPaymentClientFallbackFactory implements FallbackFactory<InternalPaymentClient> {
    @Override
    public InternalPaymentClient create(Throwable cause) {
        log.error("支付服务内部调用失败: {}", cause.getMessage(), cause);
        return new InternalPaymentClient() {
            @Override
            public Result<String> getPaymentNoByOrderId(Long orderId) {
                return Result.error(503, "支付服务暂时不可用");
            }
        };
    }
}
