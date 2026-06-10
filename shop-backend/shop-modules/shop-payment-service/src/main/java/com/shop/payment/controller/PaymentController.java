package com.shop.payment.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.payment.entity.Payment;
import com.shop.payment.entity.RefundRecord;
import com.shop.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "支付管理")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "创建支付（支付宝返回HTML/微信返回二维码/余额即时扣款）")
    @PostMapping
    public Result<Map<String, Object>> create(@RequestAttribute("userId") Long userId,
                                               @RequestBody Map<String, Object> body,
                                               jakarta.servlet.http.HttpServletRequest request) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        int payMethod = Integer.parseInt(body.getOrDefault("payMethod", "1").toString());
        String scene = (String) body.getOrDefault("scene", "web");
        String frontendOrigin = request.getHeader("Origin");
        if (frontendOrigin == null || frontendOrigin.isBlank()) {
            frontendOrigin = request.getHeader("Referer");
            if (frontendOrigin != null && !frontendOrigin.isBlank()) {
                // 提取 Referer 的基础 URL（去掉路径部分）
                try {
                    java.net.URI uri = new java.net.URI(frontendOrigin);
                    frontendOrigin = uri.getScheme() + "://" + uri.getHost()
                            + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
                } catch (Exception ignored) {
                    frontendOrigin = null;
                }
            }
        }
        return Result.success(paymentService.createPayment(userId, orderId, payMethod, scene, frontendOrigin));
    }

    @Operation(summary = "模拟确认支付（开发环境专用）")
    @PostMapping("/{paymentNo}/mock-confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> mockConfirm(@PathVariable String paymentNo) {
        return Result.success(paymentService.mockConfirm(paymentNo));
    }

    @Operation(summary = "查询支付状态（轮询确认用）")
    @GetMapping("/{paymentNo}/status")
    public Result<Map<String, Object>> queryStatus(@PathVariable String paymentNo,
                                                    @RequestAttribute("userId") Long userId) {
        return Result.success(paymentService.queryPaymentStatus(paymentNo));
    }

    @Operation(summary = "支付宝异步通知回调（公开）")
    @PostMapping("/callback/alipay")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        return paymentService.handleAlipayNotify(params);
    }

    @Operation(summary = "微信支付回调（公开）")
    @PostMapping("/callback/wechat")
    public Map<String, Object> wechatCallback(@RequestBody Map<String, Object> body,
                                               HttpServletRequest request) {
        log.info("微信支付回调: {}", body);
        String signature = request.getHeader("Wechatpay-Signature");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        return paymentService.handleWechatCallback(body, signature, timestamp, nonce);
    }

    @Operation(summary = "统一回调入口（公开，兼容旧版）")
    @PostMapping("/callback")
    public Result<String> callback(@RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        String signature = request.getHeader("Wechatpay-Signature");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        paymentService.handleWechatCallback(body, signature, timestamp, nonce);
        return Result.success("ok");
    }

    @Operation(summary = "微信退款回调（公开）")
    @PostMapping("/callback/wechat/refund")
    public Map<String, Object> wechatRefundCallback(@RequestBody Map<String, Object> body,
                                                     HttpServletRequest request) {
        log.info("微信退款回调: {}", body);
        String signature = request.getHeader("Wechatpay-Signature");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        return paymentService.handleWechatRefundCallback(body, signature, timestamp, nonce);
    }

    @Operation(summary = "支付宝支付表单页（GET，直接返回HTML）")
    @GetMapping(value = "/{paymentNo}/alipay-form", produces = "text/html;charset=UTF-8")
    public String alipayFormPage(@PathVariable String paymentNo,
                                  @RequestParam(required = false) String returnUrl) {
        return paymentService.getAlipayForm(paymentNo, returnUrl);
    }

    @Operation(summary = "申请退款（支持全额/部分退款）")
    @PostMapping("/{paymentNo}/refund")
    public Result<Map<String, Object>> refund(@PathVariable String paymentNo,
                                               @RequestAttribute("userId") Long userId,
                                               @RequestBody Map<String, Object> body) {
        String reason = (String) body.getOrDefault("reason", "用户申请退款");
        BigDecimal refundAmount = null;
        if (body.get("refundAmount") != null) {
            refundAmount = new BigDecimal(body.get("refundAmount").toString());
        }
        return Result.success(paymentService.refund(paymentNo, userId, reason, refundAmount));
    }

    @Operation(summary = "我的支付记录")
    @GetMapping("/my")
    public Result<PageResult<Payment>> myPayments(@RequestAttribute("userId") Long userId,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(paymentService.myPayments(userId, pageNum, pageSize));
    }

    @Operation(summary = "查询退款记录（按退款单号）")
    @GetMapping("/refunds/{refundNo}")
    public Result<RefundRecord> getRefundRecord(@PathVariable String refundNo) {
        return Result.success(paymentService.getRefundRecord(refundNo));
    }

    @Operation(summary = "查询支付单的所有退款记录")
    @GetMapping("/{paymentNo}/refunds")
    public Result<List<RefundRecord>> listRefundRecords(@PathVariable String paymentNo) {
        return Result.success(paymentService.listRefundRecords(paymentNo));
    }

    @Operation(summary = "同步支付宝退款状态")
    @PostMapping("/refunds/{refundNo}/sync")
    public Result<Map<String, Object>> syncRefundStatus(@PathVariable String refundNo) {
        return Result.success(paymentService.syncRefundStatus(refundNo));
    }
}
