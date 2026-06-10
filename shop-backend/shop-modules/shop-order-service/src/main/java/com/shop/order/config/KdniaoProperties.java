package com.shop.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 快递100配置属性
 *
 * @author shop
 * @since 2026-06-07
 */
@Data
@Component
@ConfigurationProperties(prefix = "kdniao")
public class KdniaoProperties {

    /** 用户ID */
    private String customerId;

    /** 授权密钥 */
    private String appKey;

    /** 查询接口URL */
    private String queryUrl = "https://poll.kuaidi100.com/poll/query.do";

    /** 单号识别接口URL */
    private String autoUrl = "https://poll.kuaidi100.com/poll/auto";

    /** 实时查询接口URL */
    private String realtimeUrl = "https://poll.kuaidi100.com/poll/realtimeservice.go";
}
