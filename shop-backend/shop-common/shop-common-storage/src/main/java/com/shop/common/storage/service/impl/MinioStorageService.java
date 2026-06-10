package com.shop.common.storage.service.impl;

import com.shop.common.exception.BusinessException;
import com.shop.common.storage.config.StorageProperties;
import com.shop.common.storage.service.StorageService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO 文件存储服务实现
 *
 * @author shop
 * @since 2026-05-29
 */
@Slf4j
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final StorageProperties properties;

    @Override
    public String upload(String objectName, MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            return upload(objectName, stream, file.getContentType(), file.getSize());
        } catch (Exception e) {
            log.error("文件上传失败: objectName={}, error={}", objectName, e.getMessage(), e);
            throw new BusinessException("文件上传失败", e);
        }
    }

    @Override
    public String upload(String objectName, InputStream stream, String contentType, long size) {
        try {
            ensureBucketExists();

            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .stream(stream, size, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();

            minioClient.putObject(args);
            String url = getAccessUrl(objectName);
            log.info("文件上传成功: objectName={}, url={}", objectName, url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败: objectName={}, error={}", objectName, e.getMessage(), e);
            throw new BusinessException("文件上传失败", e);
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
            log.info("文件删除成功: objectName={}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: objectName={}, error={}", objectName, e.getMessage(), e);
            throw new BusinessException("文件删除失败", e);
        }
    }

    @Override
    public String getAccessUrl(String objectName) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl + objectName;
        }
        return baseUrl + "/" + objectName;
    }

    @Override
    public boolean exists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
                log.info("创建存储桶: {}", properties.getBucket());
            }
        } catch (Exception e) {
            log.error("存储桶初始化失败: {}", e.getMessage(), e);
            throw new BusinessException("存储桶初始化失败", e);
        }
    }
}
