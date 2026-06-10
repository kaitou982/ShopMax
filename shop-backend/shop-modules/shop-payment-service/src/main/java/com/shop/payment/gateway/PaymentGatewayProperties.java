package com.shop.payment.gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentGatewayProperties {

    private Alipay alipay = new Alipay();
    private WechatPay wechatPay = new WechatPay();
    /** 支付回调公网地址前缀（开发时用 cpolar） */
    private String notifyBaseUrl = "http://localhost:8080";
    /** 前端地址（支付宝支付完成后跳转回来） */
    private String frontendBaseUrl = "http://localhost:5173";

    @Data
    public static class Alipay {
        /** 沙箱APPID */
        private String appId;
        /** 应用私钥(PKCS8格式) */
        private String privateKey;
        /** 支付宝公钥 */
        private String alipayPublicKey;
        /** 沙箱网关（2024年后新版地址） */
        private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
        /** 签名类型 RSA2 */
        private String signType = "RSA2";
        /** 是否启用沙箱模式 */
        private boolean sandbox = true;
    }

    @Data
    public static class WechatPay {
        /** 是否启用模拟模式（开发环境用） */
        private boolean mock = true;
        /** 商户号 */
        private String merchantId;
        /** APIv3密钥 */
        private String apiV3Key;
        /** 商户证书序列号 */
        private String merchantSerialNumber;
        /** 商户私钥 */
        private String privateKey;
        /** 应用APPID */
        private String appId;
    }
}
