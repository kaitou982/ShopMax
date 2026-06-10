package com.shop.user.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 店家入驻申请请求
 *
 * @author shop
 * @since 2026-05-21
 */
@Data
@Schema(description = "店家入驻申请请求")
public class StoreApplyRequest {

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 128, message = "店铺名称最长128个字符")
    @Schema(description = "店铺名称", required = true)
    private String storeName;

    @Schema(description = "店铺Logo")
    private String storeLogo;

    @Size(max = 1000, message = "店铺简介最长1000个字符")
    @Schema(description = "店铺简介")
    private String storeDescription;
}
