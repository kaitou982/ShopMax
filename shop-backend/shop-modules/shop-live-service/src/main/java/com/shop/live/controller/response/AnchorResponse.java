package com.shop.live.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "主播响应")
public class AnchorResponse {

    private Long id;
    private Long userId;
    private String realName;
    private String phone;
    private String nickname;
    private String avatar;
    private String cover;
    private String introduction;
    private Integer status;
    private String rejectReason;
    private Integer level;
    private Integer fansCount;
    private Integer totalLiveCount;
    private Long totalDuration;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
