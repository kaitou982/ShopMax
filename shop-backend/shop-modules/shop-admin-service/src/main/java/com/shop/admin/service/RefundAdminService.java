package com.shop.admin.service;

import com.shop.common.feign.client.InternalRefundClient;
import com.shop.common.feign.client.InternalOrderClient;
import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.feign.client.InternalProductClient;
import com.shop.common.enums.OrderStatus;
import com.shop.common.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款审核服务（已解耦：通过 Feign 调用 payment-service）
 *
 * @author shop
 * @since 2026-07-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundAdminService {

    private final InternalRefundClient internalRefundClient;
    private final InternalOrderClient internalOrderClient;
    private final InternalUserClient internalUserClient;
    private final InternalProductClient internalProductClient;

    /** 分页查询退款记录（通过 Feign） */
    public Map<String, Object> page(Integer pageNum, Integer pageSize, Integer status) {
        Result<Map<String, Object>> result = internalRefundClient.page(pageNum, pageSize, status);
        return result.getData();
    }

    /** 根据订单号查询退款记录 */
    public Object getByOrderNo(String orderNo) {
        return internalRefundClient.getByOrderNo(orderNo).getData();
    }

    /** 批准退款 */
    public Map<String, Object> approve(String refundNo) {
        return internalRefundClient.approve(refundNo).getData();
    }

    /** 拒绝退款 */
    public Map<String, Object> reject(String refundNo, String reason) {
        Map<String, String> body = new HashMap<>();
        body.put("reason", reason);
        return internalRefundClient.reject(refundNo, body).getData();
    }

    /** 手动标记退款成功 */
    public Map<String, Object> manualApprove(String refundNo, String remark) {
        Map<String, String> body = new HashMap<>();
        body.put("remark", remark);
        return internalRefundClient.manualApprove(refundNo, body).getData();
    }
}
