package com.shop.live.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "主播审核请求")
public class AnchorAuditRequest {

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态: 1-通过 2-拒绝")
    private Integer status;

    @Schema(description = "拒绝原因(拒绝时必填)")
    private String rejectReason;
}
