package com.shop.payment.gateway;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝沙箱网关 — 支持电脑网页支付 / 手机网页支付 / 退款 / 回调验签
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayGateway {

    private final PaymentGatewayProperties properties;
    private final ObjectMapper objectMapper;
    private AlipayClient alipayClient;

    @PostConstruct
    public void init() {
        var alipay = properties.getAlipay();
        if (alipay.getAppId() == null || alipay.getAppId().isBlank()) {
            log.warn("支付宝未配置APPID，沙箱网关不可用");
            return;
        }
        try {
            this.alipayClient = DefaultAlipayClient.builder(
                    alipay.getGatewayUrl(),
                    alipay.getAppId(),
                    alipay.getPrivateKey()
            ).format("json")
             .charset("UTF-8")
             .alipayPublicKey(alipay.getAlipayPublicKey())
             .signType(alipay.getSignType())
             .connectTimeout(15000)
             .readTimeout(30000)
             .build();
            log.info("支付宝网关初始化完成: appId={}, gateway={}, sandbox={}",
                    alipay.getAppId(), alipay.getGatewayUrl(), alipay.isSandbox());
        } catch (Exception e) {
            log.error("支付宝网关初始化失败: {}", e.getMessage(), e);
            this.alipayClient = null;
        }
    }

    /** 电脑网页支付 → 返回HTML表单 */
    public String pagePay(String outTradeNo, BigDecimal amount, String subject, String returnUrl) {
        checkReady();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(properties.getNotifyBaseUrl() + "/api/v1/payments/callback/alipay");
        request.setReturnUrl(returnUrl);
        request.setBizContent(bizJson(Map.of(
                "out_trade_no", outTradeNo,
                "total_amount", amount.toString(),
                "subject", subject,
                "product_code", "FAST_INSTANT_TRADE_PAY"
        )));

        try {
            AlipayTradePagePayResponse resp = alipayClient.pageExecute(request);
            if (resp.isSuccess()) {
                return resp.getBody(); // HTML表单
            }
            log.error("支付宝下单失败: code={}, msg={}", resp.getCode(), resp.getMsg());
            throw new BusinessException("支付宝下单失败: " + resp.getSubMsg());
        } catch (AlipayApiException e) {
            String detail = e.getErrMsg() != null ? e.getErrMsg() : "";
            if (e.getCause() != null) detail = e.getCause().toString();
            log.error("支付宝API异常: errMsg={}, errCode={}, cause={}", e.getErrMsg(), e.getErrCode(), e.getCause(), e);
            throw new BusinessException("支付宝页面支付异常: " + detail);
        }
    }

    /** 手机网页支付 → 返回HTML/支付链接 */
    public String wapPay(String outTradeNo, BigDecimal amount, String subject, String returnUrl) {
        checkReady();
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(properties.getNotifyBaseUrl() + "/api/v1/payments/callback/alipay");
        request.setReturnUrl(returnUrl);
        request.setBizContent(bizJson(Map.of(
                "out_trade_no", outTradeNo,
                "total_amount", amount.toString(),
                "subject", subject,
                "product_code", "QUICK_WAP_WAY"
        )));

        try {
            AlipayTradeWapPayResponse resp = alipayClient.pageExecute(request);
            if (resp.isSuccess()) {
                return resp.getBody();
            }
            throw new BusinessException("支付宝下单失败: " + resp.getSubMsg());
        } catch (AlipayApiException e) {
            String detail = e.getErrMsg() != null ? e.getErrMsg() : "";
            if (e.getCause() != null) detail = e.getCause().toString();
            log.error("支付宝WAP API异常: errMsg={}, errCode={}, cause={}", e.getErrMsg(), e.getErrCode(), e.getCause(), e);
            throw new BusinessException("支付宝手机支付异常: " + detail);
        }
    }

    /** 退款（支持幂等 out_request_no 和退款原因） */
    public Map<String, Object> refund(String outTradeNo, String outRequestNo,
                                       BigDecimal refundAmount, String refundReason) {
        checkReady();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        Map<String, String> bizContent = new java.util.HashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("refund_amount", refundAmount.toString());
        bizContent.put("out_request_no", outRequestNo);
        if (refundReason != null && !refundReason.isBlank()) {
            bizContent.put("refund_reason", refundReason);
        }
        request.setBizContent(bizJson(bizContent));

        try {
            AlipayTradeRefundResponse resp = alipayClient.execute(request);
            if (resp.isSuccess()) {
                log.info("支付宝退款成功: outTradeNo={}, outRequestNo={}, refundFee={}",
                        outTradeNo, outRequestNo, resp.getRefundFee());
                return Map.of("success", true,
                        "refundFee", resp.getRefundFee() != null ? resp.getRefundFee() : refundAmount.toString(),
                        "gatewayRefundNo", resp.getTradeNo() != null ? resp.getTradeNo() : "");
            }
            log.warn("支付宝退款失败: outTradeNo={}, outRequestNo={}, code={}, msg={}",
                    outTradeNo, outRequestNo, resp.getCode(), resp.getSubMsg());
            return Map.of("success", false,
                    "errorCode", resp.getCode() != null ? resp.getCode() : "",
                    "errorMsg", resp.getSubMsg() != null ? resp.getSubMsg() : resp.getMsg());
        } catch (AlipayApiException e) {
            log.error("支付宝退款异常: outTradeNo={}, errMsg={}", outTradeNo, e.getErrMsg(), e);
            return Map.of("success", false,
                    "errorMsg", e.getErrMsg() != null ? e.getErrMsg() : e.toString());
        }
    }

    /** 回调验签 */
    public boolean verifyNotify(Map<String, String> params) {
        var alipay = properties.getAlipay();
        try {
            return AlipaySignature.rsaCheckV1(params, alipay.getAlipayPublicKey(), "UTF-8", alipay.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付宝验签失败: {}", e.getErrMsg());
            return false;
        }
    }

    /** 当面付扫码支付 → 返回二维码链接 */
    public Map<String, Object> preCreate(String outTradeNo, BigDecimal amount, String subject) {
        checkReady();
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(properties.getNotifyBaseUrl() + "/api/v1/payments/callback/alipay");
        request.setBizContent(bizJson(Map.of(
                "out_trade_no", outTradeNo,
                "total_amount", amount.toString(),
                "subject", subject
        )));

        try {
            AlipayTradePrecreateResponse resp = alipayClient.execute(request);
            if (resp.isSuccess()) {
                log.info("支付宝扫码预下单成功: outTradeNo={}, qrCode={}", outTradeNo, resp.getQrCode());
                return Map.of("success", true, "qrCode", resp.getQrCode());
            }
            log.error("支付宝扫码预下单失败: code={}, msg={}", resp.getCode(), resp.getMsg());
            throw new BusinessException("支付宝扫码下单失败: " + resp.getSubMsg());
        } catch (AlipayApiException e) {
            String detail = e.getErrMsg() != null ? e.getErrMsg() : "";
            if (e.getCause() != null) detail = e.getCause().toString();
            log.error("支付宝扫码API异常: errMsg={}, errCode={}, cause={}", e.getErrMsg(), e.getErrCode(), e.getCause(), e);
            throw new BusinessException("支付宝扫码支付异常: " + detail);
        }
    }

    /** 主动查询支付宝交易状态（轮询确认用） */
    public Map<String, Object> tradeQuery(String outTradeNo) {
        checkReady();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(bizJson(Map.of("out_trade_no", outTradeNo)));

        try {
            AlipayTradeQueryResponse resp = alipayClient.execute(request);
            if (resp.isSuccess()) {
                log.info("支付宝交易查询成功: outTradeNo={}, tradeStatus={}, tradeNo={}",
                        outTradeNo, resp.getTradeStatus(), resp.getTradeNo());
                return Map.of("tradeStatus", resp.getTradeStatus(),
                        "tradeNo", resp.getTradeNo() != null ? resp.getTradeNo() : "");
            }
            log.warn("支付宝交易查询失败: outTradeNo={}, code={}, msg={}", outTradeNo, resp.getCode(), resp.getMsg());
            return Map.of("tradeStatus", "UNKNOWN");
        } catch (AlipayApiException e) {
            log.error("支付宝查询异常: outTradeNo={}, errMsg={}", outTradeNo, e.getErrMsg(), e);
            return Map.of("tradeStatus", "ERROR");
        }
    }

    /** 查询支付宝退款状态 */
    public Map<String, Object> refundQuery(String outTradeNo, String outRequestNo) {
        checkReady();
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        Map<String, String> bizContent = new java.util.HashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("out_request_no", outRequestNo);
        request.setBizContent(bizJson(bizContent));

        try {
            AlipayTradeFastpayRefundQueryResponse resp = alipayClient.execute(request);
            if (resp.isSuccess()) {
                log.info("退款查询成功: outTradeNo={}, outRequestNo={}, refundStatus={}",
                        outTradeNo, outRequestNo, resp.getRefundStatus());
                return Map.of("success", true,
                        "refundStatus", resp.getRefundStatus() != null ? resp.getRefundStatus() : "UNKNOWN",
                        "refundAmount", resp.getRefundAmount() != null ? resp.getRefundAmount() : "0.00",
                        "outRequestNo", resp.getOutRequestNo() != null ? resp.getOutRequestNo() : outRequestNo);
            }
            log.warn("退款查询失败: outTradeNo={}, outRequestNo={}, code={}, msg={}",
                    outTradeNo, outRequestNo, resp.getCode(), resp.getSubMsg());
            return Map.of("success", false,
                    "errorMsg", resp.getSubMsg() != null ? resp.getSubMsg() : resp.getMsg());
        } catch (AlipayApiException e) {
            log.error("退款查询异常: outTradeNo={}, outRequestNo={}, errMsg={}",
                    outTradeNo, outRequestNo, e.getErrMsg(), e);
            return Map.of("success", false,
                    "errorMsg", e.getErrMsg() != null ? e.getErrMsg() : e.toString());
        }
    }

    public boolean isAvailable() {
        return alipayClient != null;
    }

    private String bizJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new BusinessException("支付参数序列化失败");
        }
    }

    private void checkReady() {
        if (alipayClient == null) throw new BusinessException("支付宝网关未初始化，请配置APPID等参数");
    }
}
