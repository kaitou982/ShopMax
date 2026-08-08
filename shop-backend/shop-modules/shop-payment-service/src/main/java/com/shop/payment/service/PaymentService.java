package com.shop.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.enums.OrderStatus;
import com.shop.common.enums.PayMethod;
import com.shop.common.enums.PaymentStatus;
import com.shop.common.enums.RefundStatus;
import com.shop.common.exception.BusinessException;
import com.shop.common.feign.client.InternalOrderClient;
import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.payment.entity.Payment;
import com.shop.payment.entity.RefundRecord;
import com.shop.payment.gateway.AlipayGateway;
import com.shop.payment.gateway.PaymentGatewayProperties;
import com.shop.payment.gateway.WechatPayGateway;
import com.shop.payment.mapper.PaymentMapper;
import com.shop.payment.mapper.RefundRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    private final AlipayGateway alipayGateway;
    private final WechatPayGateway wechatPayGateway;
    private final PaymentGatewayProperties properties;
    private final RefundRecordMapper refundRecordMapper;

    @Autowired(required = false)
    private InternalUserClient internalUserClient;

    @Autowired(required = false)
    private InternalOrderClient internalOrderClient;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 创建支付 → 返回支付入口信息 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(Long userId, Long orderId, Integer payMethod, String scene, String frontendOrigin) {
        Map<String, Object> order = queryOrder(orderId, userId);
        if (order == null || order.isEmpty()) throw new BusinessException("订单不存在或不属于当前用户");

        int orderStatus = ((Number) order.get("status")).intValue();
        if (orderStatus != OrderStatus.PENDING_PAY.getCode()) throw new BusinessException("订单状态不正确");

        BigDecimal payAmount = (BigDecimal) order.get("pay_amount");
        String orderNo = (String) order.get("order_no");

        // 防重复支付：检查是否已有未支付或已支付的支付单
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getOrderId, orderId).in(Payment::getStatus, PaymentStatus.PENDING.getCode(), PaymentStatus.SUCCESS.getCode());
        long existingCount = baseMapper.selectCount(w);
        if (existingCount > 0) {
            List<Payment> existing = baseMapper.selectList(w);
            Payment first = existing.get(0);
            if (first.getStatus().equals(PaymentStatus.SUCCESS.getCode())) {
                throw new BusinessException("该订单已支付");
            }
            // 存在未支付的支付单，直接复用
            log.info("复用已有支付单: paymentNo={}", first.getPaymentNo());
            return buildPaymentResponse(first, scene, frontendOrigin);
        }

        String paymentNo = "PAY" + LocalDateTime.now().format(DT) + String.format("%04d", new Random().nextInt(10000));
        Payment p = new Payment();
        p.setPaymentNo(paymentNo);
        p.setOrderId(orderId);
        p.setOrderNo(orderNo);
        p.setUserId(userId);
        p.setAmount(payAmount);
        p.setPayMethod(payMethod);
        p.setStatus(PaymentStatus.PENDING.getCode());
        baseMapper.insert(p);

        log.info("创建支付单: paymentNo={}, orderNo={}, amount={}, method={}, scene={}", paymentNo, orderNo, payAmount, payMethod, scene);

        // 余额支付：即时扣款
        if (payMethod.equals(PayMethod.BALANCE.getCode())) {
            return executeBalancePay(paymentNo);
        }

        String subject = "ShopMax订单-" + orderNo;
        String frontendBase = (frontendOrigin != null && !frontendOrigin.isBlank())
                ? frontendOrigin : properties.getFrontendBaseUrl();
        String returnUrl = frontendBase + "/order/" + orderId;

        // 支付宝
        if (payMethod.equals(PayMethod.ALIPAY.getCode())) {
            if (!alipayGateway.isAvailable()) {
                return simulatePay(paymentNo, "alipay");
            }
            // 扫码支付：调用当面付预下单获取二维码链接
            if ("qrcode".equals(scene)) {
                Map<String, Object> qrResult = alipayGateway.preCreate(paymentNo, payAmount, subject);
                return Map.of("paymentNo", paymentNo, "amount", payAmount,
                        "method", "alipay", "scene", "qrcode",
                        "qrCodeUrl", qrResult.get("qrCode"));
            }
            // 页面跳转支付（默认）
            String formUrl = "/api/v1/payments/" + paymentNo + "/alipay-form?returnUrl="
                    + java.net.URLEncoder.encode(returnUrl, java.nio.charset.StandardCharsets.UTF_8);
            return Map.of("paymentNo", paymentNo, "amount", payAmount,
                    "method", "alipay", "scene", "page", "formUrl", formUrl);
        }

        // 微信支付
        if (payMethod.equals(PayMethod.WECHAT.getCode())) {
            if (!wechatPayGateway.isAvailable()) {
                return simulatePay(paymentNo, "wechat");
            }
            if ("mobile".equals(scene)) {
                return wechatPayGateway.jsapiPay(null, paymentNo, payAmount, subject);
            }
            Map<String, Object> result = wechatPayGateway.nativePay(paymentNo, payAmount, subject);
            result.put("paymentNo", paymentNo);
            result.put("method", "wechat");
            return result;
        }

        throw new BusinessException("不支持的支付方式");
    }

    /** 余额支付 */
    private Map<String, Object> executeBalancePay(String paymentNo) {
        Payment p = getByPaymentNo(paymentNo);
        if (internalUserClient != null) {
            Result<Void> balanceResult = internalUserClient.deductBalance(p.getUserId(),
                    Map.of("amount", p.getAmount(), "description", "余额支付订单"));
            if (balanceResult == null || balanceResult.getCode() != 200) {
                throw new BusinessException("余额不足");
            }
        }
        completePayment(p, "BALANCE_" + paymentNo);
        return Map.of("paymentNo", paymentNo, "status", "success", "method", "balance");
    }

    /** 模拟支付（支付宝/微信未配置时降级） */
    private Map<String, Object> simulatePay(String paymentNo, String method) {
        return Map.of("paymentNo", paymentNo, "method", method, "status", "pending", "mode", "simulate",
                "message", "【模拟环境】" + method + "未配置，请调用 POST /api/v1/payments/" + paymentNo + "/mock-confirm 确认支付");
    }

    /** 获取支付宝支付表单 HTML（GET 接口直接返回页面） */
    public String getAlipayForm(String paymentNo, String returnUrlParam) {
        Payment p = getByPaymentNo(paymentNo);
        if (p.getStatus() != PaymentStatus.PENDING.getCode()) throw new BusinessException("支付单状态不正确");
        if (p.getPayMethod() != PayMethod.ALIPAY.getCode()) throw new BusinessException("非支付宝支付");

        if (!alipayGateway.isAvailable()) {
            throw new BusinessException("支付宝未配置，请联系管理员或选择余额支付");
        }

        String returnUrl = (returnUrlParam != null && !returnUrlParam.isBlank())
                ? returnUrlParam : properties.getFrontendBaseUrl() + "/order/" + p.getOrderId();
        String subject = "ShopMax订单-" + p.getOrderNo();
        try {
            return alipayGateway.pagePay(paymentNo, p.getAmount(), subject, returnUrl);
        } catch (Exception e) {
            log.error("支付宝表单生成失败: paymentNo={}, error={}", paymentNo, e.getMessage(), e);
            throw new BusinessException("支付宝支付网关异常，请稍后重试或选择余额支付: " + e.getMessage());
        }
    }

    /** 查询支付状态（轮询确认用，主动调用支付宝 tradeQuery） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> queryPaymentStatus(String paymentNo) {
        Payment p = getByPaymentNo(paymentNo);

        // 已支付直接返回成功，不重复调用支付宝 API
        if (p.getStatus().equals(PaymentStatus.SUCCESS.getCode())) {
            return Map.of("paymentNo", paymentNo, "status", "success",
                    "tradeStatus", "TRADE_SUCCESS", "transactionId", p.getTransactionId());
        }

        // 未支付且非支付宝，无法轮询确认
        if (p.getPayMethod() != PayMethod.ALIPAY.getCode()) {
            return Map.of("paymentNo", paymentNo, "status", "pending", "tradeStatus", "WAIT_BUYER_PAY");
        }

        // 调用支付宝 tradeQuery API 查询交易状态
        Map<String, Object> queryResult = alipayGateway.tradeQuery(paymentNo);
        String tradeStatus = (String) queryResult.get("tradeStatus");

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            String tradeNo = (String) queryResult.get("tradeNo");
            completePayment(p, tradeNo);
            log.info("轮询确认支付成功: paymentNo={}, tradeNo={}", paymentNo, tradeNo);
            return Map.of("paymentNo", paymentNo, "status", "success",
                    "tradeStatus", tradeStatus, "transactionId", tradeNo);
        }

        return Map.of("paymentNo", paymentNo, "status", "pending",
                "tradeStatus", tradeStatus != null ? tradeStatus : "WAIT_BUYER_PAY");
    }

    /** 模拟确认支付（开发环境用） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockConfirm(String paymentNo) {
        Payment p = getByPaymentNo(paymentNo);
        if (p.getStatus() != PaymentStatus.PENDING.getCode()) throw new BusinessException("支付单状态不正确");
        String txnId;
        if (p.getPayMethod().equals(PayMethod.ALIPAY.getCode())) {
            txnId = "ALI_MOCK_" + LocalDateTime.now().format(DT);
        } else {
            txnId = wechatPayGateway.mockConfirm(paymentNo);
        }
        completePayment(p, txnId);
        return Map.of("paymentNo", paymentNo, "status", "success", "transactionId", txnId);
    }

    /** 支付宝异步通知回调 */
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        log.info("收到支付宝回调: {}", params);
        if (!alipayGateway.verifyNotify(params)) {
            log.warn("支付宝回调验签失败");
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            try {
                Payment p = getByPaymentNo(outTradeNo);
                if (p.getStatus().equals(PaymentStatus.PENDING.getCode())) {
                    completePayment(p, tradeNo);
                }
                return "success";
            } catch (Exception e) {
                log.error("处理支付宝回调异常: {}", e.getMessage(), e);
            }
        }
        return "success";
    }

    /** 微信支付回调（模拟/真实） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handleWechatCallback(Map<String, Object> body,
                                                     String signature, String timestamp, String nonce) {
        // 真实模式下验签，mock 模式跳过
        if (wechatPayGateway.isAvailable()) {
            if (signature == null || timestamp == null || nonce == null) {
                log.warn("微信回调缺少验签头");
                return Map.of("code", "FAIL", "message", "缺少验签参数");
            }
            String bodyStr = body.toString();
            if (!wechatPayGateway.verifyNotify(signature, timestamp, nonce, bodyStr)) {
                log.warn("微信回调验签失败");
                return Map.of("code", "FAIL", "message", "验签失败");
            }
        }

        String outTradeNo = (String) body.getOrDefault("outTradeNo", body.get("out_trade_no"));
        String transactionId = (String) body.getOrDefault("transactionId",
                body.getOrDefault("transaction_id", "WX_CALLBACK_" + outTradeNo));

        Payment p = getByPaymentNo(outTradeNo);
        if (p.getStatus().equals(PaymentStatus.PENDING.getCode())) {
            completePayment(p, transactionId);
        }
        return Map.of("code", "SUCCESS", "message", "ok");
    }

    /** 微信退款回调（模拟/真实） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handleWechatRefundCallback(Map<String, Object> body,
                                                           String signature, String timestamp, String nonce) {
        // 真实模式下验签，mock 模式跳过
        if (wechatPayGateway.isAvailable()) {
            if (signature == null || timestamp == null || nonce == null) {
                log.warn("微信退款回调缺少验签头");
                return Map.of("code", "FAIL", "message", "缺少验签参数");
            }
            String bodyStr = body.toString();
            if (!wechatPayGateway.verifyNotify(signature, timestamp, nonce, bodyStr)) {
                log.warn("微信退款回调验签失败");
                return Map.of("code", "FAIL", "message", "验签失败");
            }
        }

        // 解析退款通知中的退款单号
        @SuppressWarnings("unchecked")
        Map<String, Object> resource = (Map<String, Object>) body.get("resource");
        String outRefundNo = resource != null ? (String) resource.get("out_refund_no") : null;
        if (outRefundNo == null) {
            log.warn("微信退款回调缺少退款单号");
            return Map.of("code", "FAIL", "message", "缺少退款单号");
        }

        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        w.eq(RefundRecord::getRefundNo, outRefundNo);
        RefundRecord record = refundRecordMapper.selectOne(w);
        if (record == null) {
            log.warn("退款记录不存在: refundNo={}", outRefundNo);
            return Map.of("code", "FAIL", "message", "退款记录不存在");
        }

        // 更新退款记录状态为成功
        record.setStatus(2);
        String successTime = resource != null ? (String) resource.get("success_time") : null;
        if (successTime != null) {
            record.setGatewayRefundNo((String) resource.get("refund_id"));
        }
        refundRecordMapper.updateById(record);

        log.info("微信退款回调处理成功: refundNo={}", outRefundNo);
        return Map.of("code", "SUCCESS", "message", "ok");
    }

    /** 退款（支持全额/部分退款） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> refund(String paymentNo, Long userId, String reason, BigDecimal refundAmount) {
        Payment p = getByPaymentNo(paymentNo);
        if (!p.getUserId().equals(userId)) throw new BusinessException("无权操作");
        if (!p.getStatus().equals(PaymentStatus.SUCCESS.getCode())) throw new BusinessException("仅已支付的支付单可退款");

        // 确定退款金额：null = 全额退款
        BigDecimal actualRefundAmount = (refundAmount != null) ? refundAmount : p.getAmount();
        BigDecimal alreadyRefunded = p.getRefundAmount() != null ? p.getRefundAmount() : BigDecimal.ZERO;
        if (alreadyRefunded.add(actualRefundAmount).compareTo(p.getAmount()) > 0) {
            throw new BusinessException("退款金额超过可退金额");
        }

        // 生成幂等退款单号
        String refundNo = "RF" + LocalDateTime.now().format(DT) + String.format("%04d", new Random().nextInt(10000));

        // 创建退款记录（状态：处理中）
        RefundRecord record = new RefundRecord();
        record.setRefundNo(refundNo);
        record.setPaymentNo(p.getPaymentNo());
        record.setOrderNo(p.getOrderNo());
        record.setUserId(userId);
        record.setRefundAmount(actualRefundAmount);
        record.setRefundReason(reason);
        record.setStatus(RefundStatus.PROCESSING.getCode());
        record.setPayMethod(p.getPayMethod());
        refundRecordMapper.insert(record);

        PayMethod payMethod = PayMethod.fromCode(p.getPayMethod());

        if (payMethod == PayMethod.BALANCE) {
            executeBalanceRefund(p, actualRefundAmount, reason);
            finalizeRefundSuccess(record, p, actualRefundAmount, reason, "BALANCE_REFUND");
        } else if (payMethod == PayMethod.ALIPAY) {
            if (!alipayGateway.isAvailable()) {
                throw new BusinessException("支付宝网关不可用");
            }
            Map<String, Object> result = alipayGateway.refund(paymentNo, refundNo, actualRefundAmount, reason);
            if (result.get("success") instanceof Boolean ok && ok) {
                String gatewayRefundNo = (String) result.getOrDefault("gatewayRefundNo", "");
                finalizeRefundSuccess(record, p, actualRefundAmount, reason, gatewayRefundNo);
            } else {
                finalizeRefundFailed(record, p, (String) result.getOrDefault("errorMsg", "支付宝退款失败"));
                throw new BusinessException("支付宝退款失败: " + result.getOrDefault("errorMsg", "未知错误"));
            }
        } else if (payMethod == PayMethod.WECHAT) {
            Map<String, Object> result = wechatPayGateway.refund(
                    paymentNo, p.getTransactionId(), refundNo,
                    actualRefundAmount, p.getAmount(), reason);
            if (result.get("success") instanceof Boolean ok && ok) {
                String gatewayRefundNo = (String) result.getOrDefault("gatewayRefundNo", "");
                finalizeRefundSuccess(record, p, actualRefundAmount, reason, gatewayRefundNo);
            } else {
                finalizeRefundFailed(record, p, (String) result.getOrDefault("errorMsg", "微信退款失败"));
                throw new BusinessException("微信退款失败: " + result.getOrDefault("errorMsg", "未知错误"));
            }
        } else {
            throw new BusinessException("不支持的支付方式");
        }

        return Map.of("paymentNo", paymentNo, "refundNo", refundNo,
                "status", RefundStatus.fromCode(record.getStatus()).getDesc(),
                "refundAmount", actualRefundAmount);
    }

    /** 余额退款 */
    private void executeBalanceRefund(Payment p, BigDecimal refundAmount, String reason) {
        if (internalUserClient != null) {
            try {
                internalUserClient.addBalance(p.getUserId(),
                        Map.of("amount", refundAmount, "description", reason));
            } catch (Exception e) {
                log.error("余额退款失败: userId={}, amount={}, error={}", p.getUserId(), refundAmount, e.getMessage(), e);
                throw new BusinessException("余额退款失败: " + e.getMessage());
            }
        }
    }

    /** 退款成功 */
    private void finalizeRefundSuccess(RefundRecord record, Payment p,
                                        BigDecimal refundAmount, String reason, String gatewayRefundNo) {
        record.setStatus(RefundStatus.SUCCESS.getCode());
        record.setGatewayRefundNo(gatewayRefundNo);
        refundRecordMapper.updateById(record);

        BigDecimal totalRefunded = (p.getRefundAmount() != null ? p.getRefundAmount() : BigDecimal.ZERO)
                .add(refundAmount);
        p.setRefundAmount(totalRefunded);
        p.setRefundTime(LocalDateTime.now());
        p.setRefundReason(reason);
        if (totalRefunded.compareTo(p.getAmount()) >= 0) {
            p.setStatus(PaymentStatus.REFUNDED.getCode());
        } else {
            p.setStatus(PaymentStatus.REFUNDING.getCode());
        }
        baseMapper.updateById(p);

        if (internalOrderClient != null) {
            try {
                internalOrderClient.updateOrderStatus(p.getOrderId(),
                        Map.of("status", OrderStatus.REFUNDED.getCode()));
            } catch (Exception e) {
                log.warn("更新订单状态失败: orderId={}, error={}", p.getOrderId(), e.getMessage());
            }
        }

        log.info("退款成功: paymentNo={}, refundNo={}, amount={}, totalRefunded={}",
                p.getPaymentNo(), record.getRefundNo(), refundAmount, totalRefunded);
    }

    /** 退款失败 */
    private void finalizeRefundFailed(RefundRecord record, Payment p, String failReason) {
        record.setStatus(RefundStatus.FAILED.getCode());
        record.setFailReason(failReason);
        refundRecordMapper.updateById(record);

        p.setStatus(PaymentStatus.SUCCESS.getCode());
        baseMapper.updateById(p);

        if (internalOrderClient != null) {
            try {
                internalOrderClient.updateOrderStatus(p.getOrderId(),
                        Map.of("status", OrderStatus.PENDING_SHIP.getCode()));
            } catch (Exception e) {
                log.warn("更新订单状态失败: orderId={}, error={}", p.getOrderId(), e.getMessage());
            }
        }

        log.warn("退款失败: paymentNo={}, refundNo={}, reason={}",
                p.getPaymentNo(), record.getRefundNo(), failReason);
    }

    /** 查询退款记录 */
    public RefundRecord getRefundRecord(String refundNo) {
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        w.eq(RefundRecord::getRefundNo, refundNo);
        RefundRecord record = refundRecordMapper.selectOne(w);
        if (record == null) throw new BusinessException("退款记录不存在: " + refundNo);
        return record;
    }

    /** 查询支付单的所有退款记录 */
    public List<RefundRecord> listRefundRecords(String paymentNo) {
        LambdaQueryWrapper<RefundRecord> w = new LambdaQueryWrapper<>();
        w.eq(RefundRecord::getPaymentNo, paymentNo).orderByDesc(RefundRecord::getCreateTime);
        return refundRecordMapper.selectList(w);
    }

    /** 主动同步支付宝退款状态 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncRefundStatus(String refundNo) {
        RefundRecord record = getRefundRecord(refundNo);
        if (record.getPayMethod() != PayMethod.ALIPAY.getCode()) {
            return Map.of("refundNo", refundNo,
                    "status", RefundStatus.fromCode(record.getStatus()).getDesc(),
                    "message", "仅支付宝退款支持主动查询");
        }
        Map<String, Object> result = alipayGateway.refundQuery(record.getPaymentNo(), refundNo);
        if (result.get("success") instanceof Boolean ok && ok) {
            String status = (String) result.get("refundStatus");
            if ("REFUND_SUCCESS".equals(status)) {
                record.setStatus(RefundStatus.SUCCESS.getCode());
                refundRecordMapper.updateById(record);
                log.info("退款状态同步成功: refundNo={}, status=SUCCESS", refundNo);
            } else if ("REFUND_FAIL".equals(status)) {
                record.setStatus(RefundStatus.FAILED.getCode());
                record.setFailReason("支付宝返回退款失败");
                refundRecordMapper.updateById(record);
                log.warn("退款状态同步: refundNo={}, status=FAIL", refundNo);
            }
            return Map.of("refundNo", refundNo,
                    "localStatus", RefundStatus.fromCode(record.getStatus()).getDesc(),
                    "gatewayStatus", status,
                    "refundAmount", result.getOrDefault("refundAmount", "0.00"));
        }
        return Map.of("refundNo", refundNo, "querySuccess", false,
                "errorMsg", result.getOrDefault("errorMsg", "查询失败"));
    }

    public PageResult<Payment> myPayments(Long userId, int pageNum, int pageSize) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getUserId, userId).orderByDesc(Payment::getCreateTime);
        return PageResult.of(baseMapper.selectPage(page, w).getRecords(), page.getTotal(), page.getPages());
    }

    private void completePayment(Payment p, String transactionId) {
        p.setStatus(PaymentStatus.SUCCESS.getCode());
        p.setTransactionId(transactionId);
        p.setPayTime(LocalDateTime.now());
        p.setCallbackTime(LocalDateTime.now());
        baseMapper.updateById(p);
        if (internalOrderClient != null) {
            try {
                internalOrderClient.updateOrderStatus(p.getOrderId(),
                        Map.of("status", OrderStatus.PENDING_SHIP.getCode(),
                                "payType", p.getPayMethod()));
            } catch (Exception e) {
                log.warn("更新订单状态失败: orderId={}, error={}", p.getOrderId(), e.getMessage());
            }
        }
        log.info("支付完成: paymentNo={}, orderNo={}, amount={}, txnId={}",
                p.getPaymentNo(), p.getOrderNo(), p.getAmount(), transactionId);
    }

    private Map<String, Object> buildPaymentResponse(Payment p, String scene, String frontendOrigin) {
        if (p.getPayMethod().equals(PayMethod.ALIPAY.getCode())) {
            if (!alipayGateway.isAvailable()) {
                return simulatePay(p.getPaymentNo(), "alipay");
            }
            if ("qrcode".equals(scene)) {
                String subject = "ShopMax订单-" + p.getOrderNo();
                Map<String, Object> qrResult = alipayGateway.preCreate(p.getPaymentNo(), p.getAmount(), subject);
                return Map.of("paymentNo", p.getPaymentNo(), "amount", p.getAmount(),
                        "method", "alipay", "scene", "qrcode",
                        "qrCodeUrl", qrResult.get("qrCode"));
            }
            String frontendBase = (frontendOrigin != null && !frontendOrigin.isBlank())
                    ? frontendOrigin : properties.getFrontendBaseUrl();
            String returnUrl = frontendBase + "/order/" + p.getOrderId();
            String formUrl = "/api/v1/payments/" + p.getPaymentNo() + "/alipay-form?returnUrl="
                    + java.net.URLEncoder.encode(returnUrl, java.nio.charset.StandardCharsets.UTF_8);
            return Map.of("paymentNo", p.getPaymentNo(), "amount", p.getAmount(),
                    "method", "alipay", "scene", "page", "formUrl", formUrl);
        }
        if (p.getPayMethod().equals(PayMethod.WECHAT.getCode())) {
            if (!wechatPayGateway.isAvailable()) {
                return simulatePay(p.getPaymentNo(), "wechat");
            }
            return Map.of("paymentNo", p.getPaymentNo(), "amount", p.getAmount(),
                    "method", "wechat", "codeUrl", "/api/v1/payments/" + p.getPaymentNo() + "/qrcode");
        }
        throw new BusinessException("不支持的支付方式");
    }

    private Payment getByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getPaymentNo, paymentNo);
        Payment p = baseMapper.selectOne(w);
        if (p == null) throw new BusinessException("支付单不存在: " + paymentNo);
        return p;
    }

    /**
     * 根据订单ID获取支付单号（内部接口）
     */
    public String getPaymentNoByOrderId(Long orderId) {
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getOrderId, orderId)
         .eq(Payment::getStatus, PaymentStatus.SUCCESS.getCode())
         .last("LIMIT 1");
        Payment p = baseMapper.selectOne(w);
        return p != null ? p.getPaymentNo() : null;
    }

    private Map<String, Object> queryOrder(Long orderId, Long userId) {
        try {
            if (internalOrderClient == null) {
                return null;
            }
            Result<Map<String, Object>> result = internalOrderClient.getOrderInfo(orderId);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                return null;
            }
            Map<String, Object> orderInfo = result.getData();
            // 验证订单属于当前用户
            Object userIdObj = orderInfo.get("user_id");
            if (userIdObj == null) {
                return null;
            }
            Long orderUserId;
            if (userIdObj instanceof Long uid) {
                orderUserId = uid;
            } else {
                orderUserId = Long.valueOf(userIdObj.toString());
            }
            if (!orderUserId.equals(userId)) {
                return null;
            }
            return orderInfo;
        } catch (Exception e) {
            log.warn("查询订单信息失败: orderId={}, error={}", orderId, e.getMessage());
            return null;
        }
    }

    /**
     * 根据订单号更新支付状态（内部接口）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatusByOrderNo(String orderNo, Integer status) {
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getOrderNo, orderNo);
        w.eq(Payment::getStatus, PaymentStatus.SUCCESS.getCode());
        Payment p = baseMapper.selectOne(w);
        if (p != null) {
            p.setStatus(status);
            baseMapper.updateById(p);
            log.info("根据订单号更新支付状态: orderNo={}, status={}", orderNo, status);
        }
    }
}
