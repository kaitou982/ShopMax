package com.shop.community.controller.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteResponse {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String title;
    private String content;
    private String coverUrl;
    private Integer status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private List<String> images;
    private List<ProductItem> products;
    private Boolean isLiked;
    private Boolean isFavorited;
    private LocalDateTime createTime;

    @Data
    public static class ProductItem {
        private Long id;
        private String name;
        private String mainImage;
        private java.math.BigDecimal salePrice;
    }
}
