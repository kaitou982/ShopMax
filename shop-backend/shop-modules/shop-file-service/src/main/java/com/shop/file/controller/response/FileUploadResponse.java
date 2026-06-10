package com.shop.file.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 *
 * @author shop
 * @since 2026-05-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadResponse {

    @Schema(description = "文件访问 URL")
    private String url;

    @Schema(description = "对象名称")
    private String objectName;

    @Schema(description = "文件大小（字节）")
    private long size;

    @Schema(description = "文件类型")
    private String contentType;
}
