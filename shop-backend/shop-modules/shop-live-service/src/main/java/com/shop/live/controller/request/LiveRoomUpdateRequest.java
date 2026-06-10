package com.shop.live.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "更新直播间请求")
public class LiveRoomUpdateRequest {

    @Schema(description = "直播标题")
    private String title;

    @Schema(description = "直播封面")
    private String cover;

    @Schema(description = "直播公告")
    private String notice;

    @Schema(description = "分类: 1-推荐 2-穿搭 3-美妆 4-美食 5-家居 6-数码 7-母婴")
    private Integer type;

    @Schema(description = "预告开始时间")
    private LocalDateTime startTime;

    @Schema(description = "推流地址")
    private String pushUrl;

    @Schema(description = "拉流地址")
    private String pullUrl;
}
