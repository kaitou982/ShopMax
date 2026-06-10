package com.shop.common.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 存储配置属性
 *
 * @author shop
 * @since 2026-05-29
 */
@Data
@ConfigurationProperties(prefix = "storage.minio")
public class StorageProperties {

    /** MinIO 服务端点 */
    private String endpoint = "http://localhost:9000";

    /** 访问密钥 */
    private String accessKey = "minioadmin";

    /** 秘密密钥 */
    private String secretKey = "minioadmin123";

    /** 存储桶名称 */
    private String bucket = "shopmax";

    /** 外部访问基础 URL */
    private String baseUrl = "http://localhost:9000/shopmax";

    /** 最大文件大小（字节），默认 10MB */
    private long maxFileSize = 10 * 1024 * 1024;
}
