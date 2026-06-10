package com.shop.community.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NoteCreateRequest {

    @Size(max = 128, message = "标题最多128字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private List<ImageItem> images;

    private List<Long> productIds;

    private Integer status;

    @Data
    public static class ImageItem {
        private String imageUrl;
        private Integer sortOrder;
    }
}
