package com.shop.file.controller;

import com.shop.common.storage.service.StorageService;
import com.shop.common.storage.util.FileValidationUtil;
import com.shop.common.web.Result;
import com.shop.file.controller.response.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * @author shop
 * @since 2026-05-29
 */
@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final StorageService storageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> VALID_DEFAULT_TYPES = Set.of(
            "product", "avatar", "brand", "category", "cover", "community"
    );

    @Operation(summary = "通用图片上传")
    @PostMapping("/upload")
    public Result<FileUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                             @RequestParam(defaultValue = "common") String type) {
        validateFile(file);
        String objectName = generateObjectName(type, file.getOriginalFilename());
        String url = storageService.upload(objectName, file);
        FileUploadResponse response = FileUploadResponse.builder()
                .url(url)
                .objectName(objectName)
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
        log.info("文件上传成功: type={}, objectName={}, url={}", type, objectName, url);
        return Result.success(response);
    }

    @Operation(summary = "批量图片上传")
    @PostMapping("/upload/batch")
    public Result<List<FileUploadResponse>> uploadBatch(@RequestParam("files") List<MultipartFile> files,
                                                         @RequestParam(defaultValue = "common") String type) {
        List<FileUploadResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            String objectName = generateObjectName(type, file.getOriginalFilename());
            String url = storageService.upload(objectName, file);
            results.add(FileUploadResponse.builder()
                    .url(url)
                    .objectName(objectName)
                    .size(file.getSize())
                    .contentType(file.getContentType())
                    .build());
        }
        log.info("批量上传完成: type={}, count={}", type, results.size());
        return Result.success(results);
    }

    @Operation(summary = "获取默认占位图")
    @GetMapping("/default/{type}")
    public void getDefaultImage(@PathVariable String type, HttpServletResponse response) throws IOException {
        if (!VALID_DEFAULT_TYPES.contains(type)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "未知的默认图类型: " + type);
            return;
        }
        String objectName = "defaults/" + type + ".png";
        String url = storageService.getAccessUrl(objectName);
        response.sendRedirect(url);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
        if (!FileValidationUtil.isAllowedImage(file)) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/GIF/WebP/SVG/BMP 图片格式");
        }
    }

    private String generateObjectName(String type, String originalFilename) {
        String ext = FileValidationUtil.getExtension(originalFilename);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return type + "/" + date + "/" + uuid + ext;
    }
}
