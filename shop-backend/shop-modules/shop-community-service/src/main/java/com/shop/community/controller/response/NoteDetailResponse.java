package com.shop.community.controller.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDetailResponse {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Boolean isFollowing;
    private String title;
    private String content;
    private String coverUrl;
    private Integer status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer shareCount;
    private Integer viewCount;
    private List<NoteImageItem> images;
    private List<NoteResponse.ProductItem> products;
    private Boolean isLiked;
    private Boolean isFavorited;
    private String locationName;
    private List<String> topics;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Data
    public static class NoteImageItem {
        private Long id;
        private String imageUrl;
        private Integer sortOrder;
    }
}
