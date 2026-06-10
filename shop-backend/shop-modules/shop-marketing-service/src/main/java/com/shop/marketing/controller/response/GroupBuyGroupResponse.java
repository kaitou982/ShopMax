package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "拼团记录响应")
public class GroupBuyGroupResponse {

    private Long id;
    private Long activityId;
    private Long leaderId;
    private Integer currentCount;
    private Integer requiredCount;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private List<GroupBuyMemberResponse> members;
}
