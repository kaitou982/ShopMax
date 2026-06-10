package com.shop.community.controller.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NoteUpdateRequest {

    @Size(max = 128, message = "标题最多128字")
    private String title;

    private String content;

    private List<NoteCreateRequest.ImageItem> images;

    private List<Long> productIds;

    private Integer status;
}
