package com.shop.community.controller.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {

    private Long id;
    private Long noteId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long parentId;
    private Long replyToUserId;
    private String replyToUserNickname;
    private String content;
    private Integer likeCount;
    private List<CommentResponse> children;
    private LocalDateTime createTime;
}
