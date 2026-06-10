package com.shop.customerservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.mimo")
public class AiConfig {

    private String apiKey;

    private String baseUrl = "https://api.xiaomimimo.com";

    private String model = "mimo-v2-flash";

    private Integer maxTokens = 2048;

    private Double temperature = 0.7;
}
