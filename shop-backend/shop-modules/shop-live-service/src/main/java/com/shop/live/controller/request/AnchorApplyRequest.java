package com.shop.live.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "主播申请请求")
public class AnchorApplyRequest {

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话")
    private String phone;

    @NotBlank(message = "主播昵称不能为空")
    @Schema(description = "主播昵称")
    private String nickname;

    @Schema(description = "主播头像")
    private String avatar;

    @Schema(description = "直播间封面")
    private String cover;

    @Schema(description = "主播简介")
    private String introduction;
}
