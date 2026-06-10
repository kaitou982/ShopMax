package com.shop.user.controller;

import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Operation(summary = "上传头像")
    @PostMapping("/users/me/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestAttribute("userId") Long userId,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.badRequest("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.badRequest("文件大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.badRequest("仅支持图片格式");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "avatar/" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        Path dir = Paths.get(uploadPath, "avatar");
        Files.createDirectories(dir);
        Path target = Paths.get(uploadPath, filename);
        file.transferTo(target.toFile());

        String url = "/uploads/" + filename;
        log.info("头像上传成功: userId={}, url={}", userId, url);

        return Result.success(Map.of("url", url));
    }
}
