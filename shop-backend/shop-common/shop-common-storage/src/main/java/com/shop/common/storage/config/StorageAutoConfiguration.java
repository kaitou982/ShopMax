package com.shop.common.storage.config;

import com.shop.common.storage.service.StorageService;
import com.shop.common.storage.service.impl.MinioStorageService;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储模块自动配置
 *
 * @author shop
 * @since 2026-05-29
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true")
public class StorageAutoConfiguration {

    private final StorageProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public StorageService storageService(MinioClient minioClient) {
        return new MinioStorageService(minioClient, properties);
    }
}
