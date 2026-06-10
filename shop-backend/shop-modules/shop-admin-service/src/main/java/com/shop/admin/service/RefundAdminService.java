package com.shop.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.enums.OrderStatus;
import com.shop.common.enums.PayMethod;
import com.shop.common.enums.PaymentStatus;
import com.shop.common.enums.RefundStatus;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.payment.entity.Payment;
import com.shop.payment.entity.RefundRecord;
import com.shop.payment.gateway.AlipayGateway;
import com.shop.payment.gateway.WechatPayGateway;
import com.shop.payment.mapper.PaymentMapper;
import com.shop.payment.mapper.RefundRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 退款审核服务
 *
 * @author shop
 * @since 2026-06-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundAdminService {

    private final RefundRecordMapper refundRecordMapper;
    private final PaymentMapper paymentMapper;
    private final AlipayGateway alipayGateway;
    private final WechatPayGateway wechatPayGateway;
    private final JdbcTemplate jdbcTemplate;

    /** 分页查询退款记录 */
    public PageResult<RefundRecord> page(Integer pageNum, Integer pageSize, Integer status) {
        Page<RefundRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        if (status != null) {
            w.eq(RefundRecord::getStatus, status);
        }
        w.orderByDesc(RefundRecord::getCreateTime);
        Page<RefundRecord> result = refundRecordMapper.selectPage(page, w);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    /** 根据订单号查询退款记录（取最新一条） */
    public RefundRecord getByOrderNo(String orderNo) {
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        w.eq(RefundRecord::getOrderNo, orderNo);
        w.orderByDesc(RefundRecord::getCreateTime);
        w.last("LIMIT 1");
        return refundRecordMapper.selectOne(w);
    }

    /** 批准退款：触发支付网关退款 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> approve(String refundNo) {
        RefundRecord record = getRefundRecord(refundNo);
        if (record.getStatus() != RefundStatus.PROCESSING.getCode()) {
            throw new BusinessException("退款记录状态不是处理中，无法批准");
        }

        PayMethod payMethod = PayMethod.fromCode(record.getPayMethod());

        // 余额退款：无 Payment 记录，直接从 RefundRecord 取信息
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
                finalizeApproveFailed(record, p, errMsg);
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
                finalizeApproveFailed(record, p, errMsg);
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

    /** 拒绝退款 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reject(String refundNo, String rejectReason) {
        RefundRecord record = getRefundRecord(refundNo);
        if (record.getStatus() != RefundStatus.PROCESSING.getCode()) {
            throw new BusinessException("退款记录状态不是处理中，无法拒绝");
        }

        record.setStatus(RefundStatus.FAILED.getCode());
        record.setFailReason(rejectReason != null ? rejectReason : "管理员拒绝退款");
        refundRecordMapper.updateById(record);

        // 恢复订单状态
        jdbcTemplate.update("UPDATE oms_order SET status = ? WHERE order_no = ?",
                OrderStatus.PENDING_SHIP.getCode(), record.getOrderNo());

        log.info("退款审核拒绝: refundNo={}, reason={}", refundNo, rejectReason);
        return Map.of("refundNo", refundNo, "status", "rejected", "reason", rejectReason);
    }

    /**
     * 手动标记退款成功（用于旧订单支付网关不可用的情况）
     * 适用场景：旧订单在支付宝/微信中的交易记录已不存在，无法通过网关退款
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> manualApprove(String refundNo, String remark) {
        RefundRecord record = getRefundRecord(refundNo);
        if (record.getStatus() != RefundStatus.PROCESSING.getCode()
                && record.getStatus() != RefundStatus.FAILED.getCode()) {
            throw new BusinessException("退款记录状态不是处理中或失败，无法手动标记");
        }

        PayMethod payMethod = PayMethod.fromCode(record.getPayMethod());

        // 余额退款：执行余额退还
        if (payMethod == PayMethod.BALANCE) {
            executeBalanceRefundFromRecord(record);
        }

        // 更新退款记录状态
        record.setStatus(RefundStatus.SUCCESS.getCode());
        record.setGatewayRefundNo("MANUAL_" + System.currentTimeMillis());
        record.setFailReason(remark);
        refundRecordMapper.updateById(record);

        // 更新支付记录（如果存在）
        if (record.getPaymentNo() != null) {
            try {
                Payment p = getPaymentByNo(record.getPaymentNo());
                BigDecimal totalRefunded = (p.getRefundAmount() != null ? p.getRefundAmount() : BigDecimal.ZERO)
                        .add(record.getRefundAmount());
                p.setRefundAmount(totalRefunded);
                p.setRefundTime(LocalDateTime.now());
                p.setRefundReason(record.getRefundReason());
                if (totalRefunded.compareTo(p.getAmount()) >= 0) {
                    p.setStatus(PaymentStatus.REFUNDED.getCode());
                } else {
                    p.setStatus(PaymentStatus.REFUNDING.getCode());
                }
                paymentMapper.updateById(p);
            } catch (Exception e) {
                log.warn("更新支付记录失败: paymentNo={}, error={}", record.getPaymentNo(), e.getMessage());
            }
        }

        // 更新订单状态
        jdbcTemplate.update("UPDATE oms_order SET status = ? WHERE order_no = ?",
                OrderStatus.REFUNDED.getCode(), record.getOrderNo());

        // 恢复商品库存
        restoreStock(record.getOrderNo());

        log.info("手动标记退款成功: refundNo={}, amount={}, remark={}", refundNo, record.getRefundAmount(), remark);
        return Map.of("refundNo", refundNo, "status", "approved", "refundAmount", record.getRefundAmount());
    }

    private void executeBalanceRefund(Payment p, BigDecimal refundAmount, String reason) {
        jdbcTemplate.update("UPDATE ums_user SET balance = balance + ? WHERE id = ?",
                refundAmount, p.getUserId());
        jdbcTemplate.update(
                "INSERT INTO ums_balance_log(user_id, change_amount, after_amount, type, biz_id, remark, create_time) "
                        + "SELECT ?, ?, balance, 3, ?, ?, NOW() FROM ums_user WHERE id = ?",
                p.getUserId(), refundAmount, p.getOrderNo(), reason, p.getUserId());
    }

    /** 余额退款：从 RefundRecord 直接取信息，不依赖 Payment */
    private void executeBalanceRefundFromRecord(RefundRecord record) {
        jdbcTemplate.update("UPDATE ums_user SET balance = balance + ? WHERE id = ?",
                record.getRefundAmount(), record.getUserId());
        jdbcTemplate.update(
                "INSERT INTO ums_balance_log(user_id, change_amount, after_amount, type, biz_id, remark, create_time) "
                        + "SELECT ?, ?, balance, 3, ?, ?, NOW() FROM ums_user WHERE id = ?",
                record.getUserId(), record.getRefundAmount(), record.getOrderNo(), record.getRefundReason(), record.getUserId());
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
        if (totalRefunded.compareTo(p.getAmount()) >= 0) {
            p.setStatus(PaymentStatus.REFUNDED.getCode());
        } else {
            p.setStatus(PaymentStatus.REFUNDING.getCode());
        }
        paymentMapper.updateById(p);

        jdbcTemplate.update("UPDATE oms_order SET status = ? WHERE order_no = ?",
                OrderStatus.REFUNDED.getCode(), record.getOrderNo());

        // 恢复商品库存
        restoreStock(record.getOrderNo());

        log.info("退款执行成功: refundNo={}, gatewayRefundNo={}, amount={}",
                record.getRefundNo(), gatewayRefundNo, record.getRefundAmount());
    }

    /** 退款审批成功（无 Payment 记录，余额退款专用） */
    private void finalizeApproveSuccessWithoutPayment(RefundRecord record, String gatewayRefundNo) {
        record.setStatus(RefundStatus.SUCCESS.getCode());
        record.setGatewayRefundNo(gatewayRefundNo);
        refundRecordMapper.updateById(record);

        jdbcTemplate.update("UPDATE oms_order SET status = ? WHERE order_no = ?",
                OrderStatus.REFUNDED.getCode(), record.getOrderNo());

        restoreStock(record.getOrderNo());

        log.info("余额退款执行成功: refundNo={}, amount={}",
                record.getRefundNo(), record.getRefundAmount());
    }

    private void finalizeApproveFailed(RefundRecord record, Payment p, String failReason) {
        record.setStatus(RefundStatus.FAILED.getCode());
        record.setFailReason(failReason);
        refundRecordMapper.updateById(record);

        jdbcTemplate.update("UPDATE oms_order SET status = ? WHERE order_no = ?",
                OrderStatus.PENDING_SHIP.getCode(), record.getOrderNo());

        log.warn("退款执行失败: refundNo={}, reason={}", record.getRefundNo(), failReason);
    }

    private RefundRecord getRefundRecord(String refundNo) {
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        w.eq(RefundRecord::getRefundNo, refundNo);
        RefundRecord record = refundRecordMapper.selectOne(w);
        if (record == null) throw new BusinessException("退款记录不存在: " + refundNo);
        return record;
    }

    /** 根据订单号恢复库存 */
    private void restoreStock(String orderNo) {
        try {
            Map<String, Object> order = jdbcTemplate.queryForMap(
                    "SELECT id FROM oms_order WHERE order_no = ? AND deleted = 0", orderNo);
            Long orderId = ((Number) order.get("id")).longValue();
            List<Map<String, Object>> items = jdbcTemplate.queryForList(
                    "SELECT product_id, quantity FROM oms_order_item WHERE order_id = ? AND deleted = 0", orderId);
            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("product_id")).longValue();
                int quantity = ((Number) item.get("quantity")).intValue();
                jdbcTemplate.update(
                        "UPDATE pms_product SET stock = stock + ? WHERE id = ? AND deleted = 0",
                        quantity, productId);
            }
        } catch (Exception e) {
            log.warn("恢复库存失败: orderNo={}, error={}", orderNo, e.getMessage());
        }
    }

    private Payment getPaymentByNo(String paymentNo) {
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getPaymentNo, paymentNo);
        Payment p = paymentMapper.selectOne(w);
        if (p == null) throw new BusinessException("支付记录不存在: " + paymentNo);
        return p;
    }
}
