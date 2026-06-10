package com.shop.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付网关 — APIv3 真实实现
 * <p>
 * 使用 Java 21 java.net.http.HttpClient 直接调用微信支付 APIv3 接口，
 * 支持 Native 扫码支付、JSAPI 支付、退款、回调验签。
 * <p>
 * 前置条件：
 * 1. 微信商户平台申请 APIv3 密钥 + 商户 API 证书(apiclient_key.pem)
 * 2. 配置 payment.wechat-pay 相关属性（merchantId / apiV3Key / privateKey / merchantSerialNumber / appId）
 * 3. 将 mock 设为 false
 */
@Slf4j
@Component
public class WechatPayGateway {

    private final PaymentGatewayProperties properties;
    private final HttpClient httpClient;

    private static final String API_BASE = "https://api.mch.weixin.qq.com";

    public WechatPayGateway(PaymentGatewayProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    // ──────────────────────────────────────────────
    // Native 支付（扫码） → 返回 code_url
    // ──────────────────────────────────────────────
    public Map<String, Object> nativePay(String outTradeNo, BigDecimal amount, String description) {
        var wp = properties.getWechatPay();
        if (!isConfigured()) {
            return mockNativePay(outTradeNo, amount, "微信支付未配置真实参数，当前为模拟模式");
        }

        int totalCents = amount.multiply(new BigDecimal("100")).intValue();
        String body = """
            {
                "appid": "%s",
                "mchid": "%s",
                "description": "%s",
                "out_trade_no": "%s",
                "notify_url": "%s",
                "amount": { "total": %d, "currency": "CNY" }
            }
            """.formatted(
                wp.getAppId(), wp.getMerchantId(), escapeJson(description), outTradeNo,
                properties.getNotifyBaseUrl() + "/api/v1/payments/callback/wechat",
                totalCents
        );

        try {
            String response = post("/v3/pay/transactions/native", body);
            // 简单解析返回的 code_url（生产环境建议用 Jackson 解析）
            String codeUrl = extractJsonField(response, "code_url");
            log.info("微信Native下单成功: outTradeNo={}, codeUrl={}", outTradeNo, codeUrl);
            return Map.of(
                    "codeUrl", codeUrl,
                    "outTradeNo", outTradeNo,
                    "mode", "live"
            );
        } catch (Exception e) {
            log.error("微信Native下单失败: outTradeNo={}, error={}", outTradeNo, e.getMessage(), e);
            throw new RuntimeException("微信支付下单失败: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────
    // JSAPI 支付（公众号/小程序）
    // ──────────────────────────────────────────────
    public Map<String, Object> jsapiPay(String openId, String outTradeNo, BigDecimal amount, String description) {
        var wp = properties.getWechatPay();
        if (!isConfigured()) {
            return mockJsapiPay(openId, outTradeNo, amount);
        }

        int totalCents = amount.multiply(new BigDecimal("100")).intValue();
        String body = """
            {
                "appid": "%s",
                "mchid": "%s",
                "description": "%s",
                "out_trade_no": "%s",
                "notify_url": "%s",
                "amount": { "total": %d, "currency": "CNY" },
                "payer": { "openid": "%s" }
            }
            """.formatted(
                wp.getAppId(), wp.getMerchantId(), escapeJson(description), outTradeNo,
                properties.getNotifyBaseUrl() + "/api/v1/payments/callback/wechat",
                totalCents, openId != null ? openId : ""
        );

        try {
            String response = post("/v3/pay/transactions/jsapi", body);
            String prepayId = extractJsonField(response, "prepay_id");
            String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
            long timestamp = Instant.now().getEpochSecond();

            // 构造小程序/公众号调起支付参数签名
            String signStr = wp.getAppId() + "\n" + timestamp + "\n" + nonceStr + "\nprepay_id=" + prepayId + "\n";
            String paySign = signWithPrivateKey(wp.getPrivateKey(), signStr);

            log.info("微信JSAPI下单成功: outTradeNo={}, prepayId={}", outTradeNo, prepayId);
            return Map.of(
                    "appId", wp.getAppId(),
                    "timeStamp", String.valueOf(timestamp),
                    "nonceStr", nonceStr,
                    "package", "prepay_id=" + prepayId,
                    "signType", "RSA",
                    "paySign", paySign,
                    "mode", "live"
            );
        } catch (Exception e) {
            log.error("微信JSAPI下单失败: outTradeNo={}, error={}", outTradeNo, e.getMessage(), e);
            throw new RuntimeException("微信支付下单失败: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────
    // 退款
    // ──────────────────────────────────────────────
    public Map<String, Object> refund(String outTradeNo, String transactionId,
                                       String outRefundNo, BigDecimal refundAmount,
                                       BigDecimal totalAmount, String refundReason) {
        if (!isConfigured()) {
            return mockRefund(outTradeNo, outRefundNo, refundAmount, refundReason);
        }

        int refundCents = refundAmount.multiply(new BigDecimal("100")).intValue();
        int totalCents = totalAmount.multiply(new BigDecimal("100")).intValue();

        String reason = (refundReason != null && !refundReason.isBlank()) ? refundReason : "用户退款";
        String body = """
            {
                "out_trade_no": "%s",
                "out_refund_no": "%s",
                "reason": "%s",
                "notify_url": "%s",
                "amount": { "refund": %d, "total": %d, "currency": "CNY" }
            }
            """.formatted(
                outTradeNo, outRefundNo, escapeJson(reason),
                properties.getNotifyBaseUrl() + "/api/v1/payments/callback/wechat/refund",
                refundCents, totalCents
        );

        try {
            String response = post("/v3/refund/domestic/refunds", body);
            String status = extractJsonField(response, "status");
            boolean success = "SUCCESS".equals(status) || "PROCESSING".equals(status);
            String gatewayRefundNo = extractJsonField(response, "refund_id");
            if (gatewayRefundNo == null || gatewayRefundNo.isEmpty()) {
                gatewayRefundNo = extractJsonField(response, "out_refund_no");
            }

            log.info("微信退款: outTradeNo={}, outRefundNo={}, status={}", outTradeNo, outRefundNo, status);
            return Map.of("success", success,
                    "gatewayRefundNo", gatewayRefundNo != null ? gatewayRefundNo : "",
                    "refundFee", refundAmount.toString(),
                    "status", status);
        } catch (Exception e) {
            log.error("微信退款异常: outTradeNo={}, outRefundNo={}, error={}", outTradeNo, outRefundNo, e.getMessage(), e);
            return Map.of("success", false,
                    "errorMsg", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 支付回调验签
    // ──────────────────────────────────────────────
    public boolean verifyNotify(String wechatpaySignature, String wechatpayTimestamp,
                                 String wechatpayNonce, String body) {
        var wp = properties.getWechatPay();
        if (wp.getApiV3Key() == null || wp.getApiV3Key().isBlank()) {
            log.warn("微信APIv3密钥未配置，跳过回调验签");
            return false;
        }
        try {
            String signStr = wechatpayTimestamp + "\n" + wechatpayNonce + "\n" + body + "\n";
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(wp.getApiV3Key().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            String computed = Base64.getEncoder().encodeToString(mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8)));
            return computed.equals(wechatpaySignature);
        } catch (Exception e) {
            log.error("微信回调验签异常: {}", e.getMessage(), e);
            return false;
        }
    }

    // ──────────────────────────────────────────────
    // 模拟环境（Mock，开发环境降级用）
    // ──────────────────────────────────────────────
    public String mockConfirm(String outTradeNo) {
        String transactionId = "WX_MOCK_" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("微信支付模拟确认: outTradeNo={}, transactionId={}", outTradeNo, transactionId);
        return transactionId;
    }

    public boolean isAvailable() {
        return isConfigured() || properties.getWechatPay().isMock();
    }

    public boolean isConfigured() {
        var wp = properties.getWechatPay();
        return !wp.isMock()
                && wp.getMerchantId() != null && !wp.getMerchantId().isBlank()
                && wp.getApiV3Key() != null && !wp.getApiV3Key().isBlank()
                && wp.getPrivateKey() != null && !wp.getPrivateKey().isBlank()
                && wp.getMerchantSerialNumber() != null && !wp.getMerchantSerialNumber().isBlank();
    }

    // ──────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────

    private Map<String, Object> mockNativePay(String outTradeNo, BigDecimal amount, String message) {
        String codeUrl = "https://pay.example.com/mock/wechat/qrcode?no=" + outTradeNo + "&amount=" + amount;
        log.info("微信支付模拟(扫码): outTradeNo={}, amount={}, 原因: {}", outTradeNo, amount, message);
        return Map.of(
                "codeUrl", codeUrl,
                "outTradeNo", outTradeNo,
                "mode", "mock",
                "message", "【模拟环境】" + message
        );
    }

    private Map<String, Object> mockJsapiPay(String openId, String outTradeNo, BigDecimal amount) {
        String prepayId = "wx_mock_" + UUID.randomUUID().toString().substring(0, 16);
        var wp = properties.getWechatPay();
        log.info("微信支付模拟(JSAPI): outTradeNo={}, amount={}, openId={}", outTradeNo, amount, openId);
        return Map.of(
                "appId", wp.getAppId() != null ? wp.getAppId() : "wx_mock_appid",
                "timeStamp", String.valueOf(System.currentTimeMillis() / 1000),
                "nonceStr", UUID.randomUUID().toString().substring(0, 16),
                "package", "prepay_id=" + prepayId,
                "signType", "RSA",
                "paySign", "MOCK_SIGN_" + UUID.randomUUID().toString().substring(0, 8),
                "mode", "mock",
                "message", "【模拟环境】微信支付未配置真实参数"
        );
    }

    private Map<String, Object> mockRefund(String outTradeNo, String outRefundNo,
                                            BigDecimal refundAmount, String refundReason) {
        log.info("微信支付模拟退款: outTradeNo={}, outRefundNo={}, amount={}, reason={}",
                outTradeNo, outRefundNo, refundAmount, refundReason);
        return Map.of("success", true,
                "gatewayRefundNo", "WX_REFUND_MOCK_" + UUID.randomUUID().toString().substring(0, 16),
                "refundFee", refundAmount.toString());
    }

    /** HTTP POST with WeChat Pay APIv3 signing */
    private String post(String path, String body) throws Exception {
        var wp = properties.getWechatPay();
        String url = API_BASE + path;
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        long timestamp = Instant.now().getEpochSecond();

        // 构造签名串
        String signStr = "POST\n" + path + "\n" + timestamp + "\n" + nonceStr + "\n" + body + "\n";
        String signature = signWithPrivateKey(wp.getPrivateKey(), signStr);

        // Authorization 头
        String authorization = "WECHATPAY2-SHA256-RSA2048 mchid=\"" + wp.getMerchantId()
                + "\",nonce_str=\"" + nonceStr
                + "\",timestamp=\"" + timestamp
                + "\",serial_no=\"" + wp.getMerchantSerialNumber()
                + "\",signature=\"" + signature + "\"";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        log.error("微信支付API返回非200: status={}, body={}", response.statusCode(), response.body());
        throw new RuntimeException("微信支付API错误: HTTP " + response.statusCode() + " " + response.body());
    }

    /** RSA-SHA256 签名（PKCS8 格式私钥） */
    private String signWithPrivateKey(String privateKeyPem, String data) throws Exception {
        if (privateKeyPem == null || privateKeyPem.isBlank()) {
            throw new RuntimeException("微信支付商户私钥未配置");
        }
        // 去除 PEM 头尾
        String keyContent = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    /** 简易 JSON 字段提取（避免额外依赖） */
    private String extractJsonField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) return "";
        int colonIdx = json.indexOf(":", keyIdx + key.length());
        if (colonIdx < 0) return "";
        int start = colonIdx + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }
        int end = json.indexOf('"', start);
        if (end < 0) {
            // 可能是数字或布尔值
            end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ' ') {
                end++;
            }
        }
        return json.substring(start, end);
    }

    /** JSON 字符串转义 */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
