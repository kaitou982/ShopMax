package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "拼团成员响应")
public class GroupBuyMemberResponse {

    private Long id;
    private Long groupId;
    private Long userId;
    private Long orderId;
    private String orderNo;
    private Integer isLeader;
    private LocalDateTime joinTime;
}
