package com.shop.common.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 统一文件存储服务接口
 *
 * @author shop
 * @since 2026-05-29
 */
public interface StorageService {

    /**
     * 上传文件
     * @param objectName 对象名称（含路径前缀）
     * @param file 上传文件
     * @return 文件访问 URL
     */
    String upload(String objectName, MultipartFile file);

    /**
     * 通过流上传文件
     * @param objectName 对象名称
     * @param stream 输入流
     * @param contentType 文件类型
     * @param size 文件大小
     * @return 文件访问 URL
     */
    String upload(String objectName, InputStream stream, String contentType, long size);

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    void delete(String objectName);

    /**
     * 生成文件访问 URL
     * @param objectName 对象名称
     * @return 完整访问 URL
     */
    String getAccessUrl(String objectName);

    /**
     * 判断文件是否存在
     * @param objectName 对象名称
     * @return true 表示存在
     */
    boolean exists(String objectName);
}
