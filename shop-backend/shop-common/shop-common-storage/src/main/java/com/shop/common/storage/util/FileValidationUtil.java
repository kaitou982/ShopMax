package com.shop.common.storage.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 文件校验工具类
 *
 * @author shop
 * @since 2026-05-29
 */
public final class FileValidationUtil {

    private FileValidationUtil() {
    }

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml", "image/bmp"
    );

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp"
    );

    public static boolean isAllowedImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType);
    }

    public static boolean isAllowedImageExtension(String filename) {
        if (filename == null) return false;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return false;
        return ALLOWED_IMAGE_EXTENSIONS.contains(filename.substring(dot).toLowerCase());
    }

    public static String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? ".jpg" : filename.substring(dot).toLowerCase();
    }
}
