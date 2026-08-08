package com.shop.common.feign.fallback;

import com.shop.common.feign.client.InternalRefundClient;
import com.shop.common.feign.dto.payment.RefundRecordDTO;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class InternalRefundClientFallbackFactory implements FallbackFactory<InternalRefundClient> {
    @Override
    public InternalRefundClient create(Throwable cause) {
        log.error("退款服务调用失败: {}", cause.getMessage(), cause);
        return new InternalRefundClient() {
            @Override
            public Result<Map<String, Object>> page(Integer pageNum, Integer pageSize, Integer status) {
                return Result.error(503, "退款服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<RefundRecordDTO> getByOrderNo(String orderNo) {
                return Result.error(503, "退款服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Map<String, Object>> approve(String refundNo) {
                return Result.error(503, "退款服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Map<String, Object>> reject(String refundNo, Map<String, String> body) {
                return Result.error(503, "退款服务暂时不可用，请稍后再试");
            }

            @Override
            public Result<Map<String, Object>> manualApprove(String refundNo, Map<String, String> body) {
                return Result.error(503, "退款服务暂时不可用，请稍后再试");
            }
        };
    }
}
