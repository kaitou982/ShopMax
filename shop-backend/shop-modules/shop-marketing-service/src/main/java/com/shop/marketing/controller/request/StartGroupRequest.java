package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发起拼团请求")
public class StartGroupRequest {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "拼团活动ID")
    private Long activityId;
}
