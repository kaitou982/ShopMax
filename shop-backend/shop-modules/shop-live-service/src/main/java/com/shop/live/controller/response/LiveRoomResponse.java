package com.shop.live.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "直播间响应")
public class LiveRoomResponse {

    private Long id;
    private Long anchorId;
    private String title;
    private String cover;
    private String notice;
    private Integer type;
    private LocalDateTime startTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime endTime;
    private String pushUrl;
    private String pullUrl;
    private Integer status;
    private Integer onlineCount;
    private Integer totalViewCount;
    private Integer peakOnlineCount;
    private Integer likeCount;
    private Integer giftCount;
    private Long duration;
    private Integer replayDuration;
    private String replayUrl;

    /** 来自 Anchor 的冗余字段 */
    private String anchorNickname;
    private String anchorAvatar;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
