package com.shop.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.enums.OrderStatus;
import com.shop.common.enums.PayMethod;
import com.shop.common.enums.PaymentStatus;
import com.shop.common.enums.RefundStatus;
import com.shop.common.exception.BusinessException;
import com.shop.common.feign.client.InternalOrderClient;
import com.shop.common.feign.client.InternalProductClient;
import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.web.Result;
import com.shop.payment.entity.Payment;
import com.shop.payment.entity.RefundRecord;
import com.shop.payment.gateway.AlipayGateway;
import com.shop.payment.gateway.WechatPayGateway;
import com.shop.payment.mapper.PaymentMapper;
import com.shop.payment.mapper.RefundRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 退款审批服务（内部使用，归属 payment-service）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntraRefundService {

    private final RefundRecordMapper refundRecordMapper;
    private final PaymentMapper paymentMapper;
    private final AlipayGateway alipayGateway;
    private final WechatPayGateway wechatPayGateway;
    private final InternalOrderClient internalOrderClient;
    private final InternalUserClient internalUserClient;
    private final InternalProductClient internalProductClient;

    public Map<String, Object> pageAsMap(Integer pageNum, Integer pageSize, Integer status) {
        Page<RefundRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        if (status != null) {
            w.eq(RefundRecord::getStatus, status);
        }
        w.orderByDesc(RefundRecord::getCreateTime);
        Page<RefundRecord> result = refundRecordMapper.selectPage(page, w);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("pages", result.getPages());
        return map;
    }

    public RefundRecord getByOrderNo(String orderNo) {
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        w.eq(RefundRecord::getOrderNo, orderNo);
        w.orderByDesc(RefundRecord::getCreateTime);
        w.last("LIMIT 1");
        return refundRecordMapper.selectOne(w);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> approve(String refundNo) {
        RefundRecord record = getRefundRecord(refundNo);
        if (record.getStatus() != RefundStatus.PROCESSING.getCode()) {
            throw new BusinessException("退款记录状态不是处理中，无法批准");
        }

        PayMethod payMethod = PayMethod.fromCode(record.getPayMethod());

        if (payMethod == PayMethod.BALANCE) {
            executeBalanceRefundFromRecord(record);
            finalizeApproveSuccessWithoutPayment(record, "BALANCE_REFUND");
            log.info("余额退款审核通过: refundNo={}, amount={}", refundNo, record.getRefundAmount());
            return Map.of("refundNo", refundNo, "status", "approved", "refundAmount", record.getRefundAmount());
        }

        Payment p = getPaymentByNo(record.getPaymentNo());

        if (payMethod == PayMethod.ALIPAY) {
            if (!alipayGateway.isAvailable()) {
                throw new BusinessException("支付宝网关不可用");
            }
            Map<String, Object> result = alipayGateway.refund(
                    record.getPaymentNo(), refundNo, record.getRefundAmount(), record.getRefundReason());
            if (result.get("success") instanceof Boolean ok && ok) {
                String gwRefundNo = (String) result.getOrDefault("gatewayRefundNo", "");
                finalizeApproveSuccess(record, p, gwRefundNo);
            } else {
                String errMsg = (String) result.getOrDefault("errorMsg", "支付宝退款失败");
                finalizeApproveFailed(record, errMsg);
                throw new BusinessException("支付宝退款失败: " + errMsg);
            }
        } else if (payMethod == PayMethod.WECHAT) {
            Map<String, Object> result = wechatPayGateway.refund(
                    record.getPaymentNo(), p.getTransactionId(), refundNo,
                    record.getRefundAmount(), p.getAmount(), record.getRefundReason());
            if (result.get("success") instanceof Boolean ok && ok) {
                String gwRefundNo = (String) result.getOrDefault("gatewayRefundNo", "");
                finalizeApproveSuccess(record, p, gwRefundNo);
            } else {
                String errMsg = (String) result.getOrDefault("errorMsg", "微信退款失败");
                finalizeApproveFailed(record, errMsg);
                throw new BusinessException("微信退款失败: " + errMsg);
            }
        } else {
            throw new BusinessException("不支持的支付方式");
        }

        log.info("退款审核通过: refundNo={}, paymentNo={}, amount={}",
                refundNo, record.getPaymentNo(), record.getRefundAmount());
        return Map.of("refundNo", refundNo, "status", "approved",
                "refundAmount", record.getRefundAmount());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reject(String refundNo, String rejectReason) {
        RefundRecord record = getRefundRecord(refundNo);
        if (record.getStatus() != RefundStatus.PROCESSING.getCode()) {
            throw new BusinessException("退款记录状态不是处理中，无法拒绝");
        }

        record.setStatus(RefundStatus.FAILED.getCode());
        record.setFailReason(rejectReason != null ? rejectReason : "管理员拒绝退款");
        refundRecordMapper.updateById(record);

        Map<String, Object> statusReq = new HashMap<>();
        statusReq.put("orderNo", record.getOrderNo());
        statusReq.put("status", OrderStatus.PENDING_SHIP.getCode());
        internalOrderClient.updateOrderStatusByOrderNo(statusReq);

        log.info("退款审核拒绝: refundNo={}, reason={}", refundNo, rejectReason);
        return Map.of("refundNo", refundNo, "status", "rejected", "reason", rejectReason);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> manualApprove(String refundNo, String remark) {
        RefundRecord record = getRefundRecord(refundNo);
        if (record.getStatus() != RefundStatus.PROCESSING.getCode()
                && record.getStatus() != RefundStatus.FAILED.getCode()) {
            throw new BusinessException("退款记录状态不是处理中或失败，无法手动标记");
        }

        PayMethod payMethod = PayMethod.fromCode(record.getPayMethod());
        if (payMethod == PayMethod.BALANCE) {
            executeBalanceRefundFromRecord(record);
        }

        record.setStatus(RefundStatus.SUCCESS.getCode());
        record.setGatewayRefundNo("MANUAL_" + System.currentTimeMillis());
        record.setFailReason(remark);
        refundRecordMapper.updateById(record);

        if (record.getPaymentNo() != null) {
            try {
                Payment p = getPaymentByNo(record.getPaymentNo());
                BigDecimal totalRefunded = (p.getRefundAmount() != null ? p.getRefundAmount() : BigDecimal.ZERO)
                        .add(record.getRefundAmount());
                p.setRefundAmount(totalRefunded);
                p.setRefundTime(LocalDateTime.now());
                p.setRefundReason(record.getRefundReason());
                p.setStatus(totalRefunded.compareTo(p.getAmount()) >= 0
                        ? PaymentStatus.REFUNDED.getCode() : PaymentStatus.REFUNDING.getCode());
                paymentMapper.updateById(p);
            } catch (Exception e) {
                log.warn("更新支付记录失败: paymentNo={}, error={}", record.getPaymentNo(), e.getMessage());
            }
        }

        Map<String, Object> statusReq = new HashMap<>();
        statusReq.put("orderNo", record.getOrderNo());
        statusReq.put("status", OrderStatus.REFUNDED.getCode());
        internalOrderClient.updateOrderStatusByOrderNo(statusReq);

        restoreStock(record.getOrderNo());

        log.info("手动标记退款成功: refundNo={}, amount={}", refundNo, record.getRefundAmount());
        return Map.of("refundNo", refundNo, "status", "approved", "refundAmount", record.getRefundAmount());
    }

    private void executeBalanceRefundFromRecord(RefundRecord record) {
        Map<String, Object> request = new HashMap<>();
        request.put("amount", record.getRefundAmount());
        request.put("description", record.getRefundReason());
        request.put("bizId", record.getOrderNo());
        internalUserClient.refundBalance(record.getUserId(), request);
    }

    private void finalizeApproveSuccess(RefundRecord record, Payment p, String gatewayRefundNo) {
        record.setStatus(RefundStatus.SUCCESS.getCode());
        record.setGatewayRefundNo(gatewayRefundNo);
        refundRecordMapper.updateById(record);

        BigDecimal totalRefunded = (p.getRefundAmount() != null ? p.getRefundAmount() : BigDecimal.ZERO)
                .add(record.getRefundAmount());
        p.setRefundAmount(totalRefunded);
        p.setRefundTime(LocalDateTime.now());
        p.setRefundReason(record.getRefundReason());
        p.setStatus(totalRefunded.compareTo(p.getAmount()) >= 0
                ? PaymentStatus.REFUNDED.getCode() : PaymentStatus.REFUNDING.getCode());
        paymentMapper.updateById(p);

        updateOrderAndRestoreStock(record, OrderStatus.REFUNDED.getCode());
    }

    private void finalizeApproveSuccessWithoutPayment(RefundRecord record, String gatewayRefundNo) {
        record.setStatus(RefundStatus.SUCCESS.getCode());
        record.setGatewayRefundNo(gatewayRefundNo);
        refundRecordMapper.updateById(record);
        updateOrderAndRestoreStock(record, OrderStatus.REFUNDED.getCode());
    }

    private void finalizeApproveFailed(RefundRecord record, String failReason) {
        record.setStatus(RefundStatus.FAILED.getCode());
        record.setFailReason(failReason);
        refundRecordMapper.updateById(record);
        updateOrderAndRestoreStock(record, OrderStatus.PENDING_SHIP.getCode());
    }

    private void updateOrderAndRestoreStock(RefundRecord record, int orderStatus) {
        Map<String, Object> statusReq = new HashMap<>();
        statusReq.put("orderNo", record.getOrderNo());
        statusReq.put("status", orderStatus);
        internalOrderClient.updateOrderStatusByOrderNo(statusReq);

        restoreStock(record.getOrderNo());
    }

    private void restoreStock(String orderNo) {
        try {
            Result<Map<String, Object>> orderResult = internalOrderClient.getOrderInfoByOrderNo(orderNo);
            if (orderResult.getCode() != 200 || orderResult.getData() == null) {
                log.warn("查询订单失败: orderNo={}", orderNo);
                return;
            }
            Long orderId = ((Number) orderResult.getData().get("id")).longValue();

            Result<List<Map<String, Object>>> itemsResult = internalOrderClient.getOrderItems(orderId);
            if (itemsResult.getCode() != 200 || itemsResult.getData() == null) {
                log.warn("查询订单商品失败: orderId={}", orderId);
                return;
            }

            for (Map<String, Object> item : itemsResult.getData()) {
                Long productId = ((Number) item.get("product_id")).longValue();
                int quantity = ((Number) item.get("quantity")).intValue();
                Map<String, Object> restoreReq = new HashMap<>();
                restoreReq.put("quantity", quantity);
                internalProductClient.restoreStock(productId, restoreReq);
            }
        } catch (Exception e) {
            log.warn("恢复库存失败: orderNo={}, error={}", orderNo, e.getMessage());
        }
    }

    private RefundRecord getRefundRecord(String refundNo) {
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        w.eq(RefundRecord::getRefundNo, refundNo);
        RefundRecord record = refundRecordMapper.selectOne(w);
        if (record == null) throw new BusinessException("退款记录不存在: " + refundNo);
        return record;
    }

    private Payment getPaymentByNo(String paymentNo) {
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getPaymentNo, paymentNo);
        Payment p = paymentMapper.selectOne(w);
        if (p == null) throw new BusinessException("支付记录不存在: " + paymentNo);
        return p;
    }
}
