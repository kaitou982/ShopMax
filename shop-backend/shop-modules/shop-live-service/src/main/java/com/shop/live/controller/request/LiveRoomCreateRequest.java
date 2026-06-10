package com.shop.live.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建直播间请求")
public class LiveRoomCreateRequest {

    @NotNull(message = "主播ID不能为空")
    @Schema(description = "主播ID")
    private Long anchorId;

    @NotBlank(message = "直播标题不能为空")
    @Schema(description = "直播标题")
    private String title;

    @Schema(description = "直播封面")
    private String cover;

    @Schema(description = "直播公告")
    private String notice;

    @NotNull(message = "直播分类不能为空")
    @Schema(description = "分类: 1-推荐 2-穿搭 3-美妆 4-美食 5-家居 6-数码 7-母婴")
    private Integer type;

    @NotNull(message = "预告开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预告开始时间")
    private LocalDateTime startTime;

    @Schema(description = "推流地址")
    private String pushUrl;

    @Schema(description = "拉流地址")
    private String pullUrl;
}
