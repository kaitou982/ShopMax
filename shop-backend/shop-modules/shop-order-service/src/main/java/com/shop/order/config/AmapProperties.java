package com.shop.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图配置属性
 *
 * @author shop
 * @since 2026-06-07
 */
@Data
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapProperties {

    /** Web服务API Key */
    private String webKey;

    /** 地理编码接口URL */
    private String geocodeUrl = "https://restapi.amap.com/v3/geocode/geo";
}
