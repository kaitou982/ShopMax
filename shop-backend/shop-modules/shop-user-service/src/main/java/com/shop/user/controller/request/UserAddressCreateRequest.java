package com.shop.user.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建收货地址请求
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "创建收货地址请求")
public class UserAddressCreateRequest {

    @NotBlank(message = "收货人姓名不能为空")
    @Schema(description = "收货人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverName;

    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "收货人手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    @Schema(description = "省份", requiredMode = Schema.RequiredMode.REQUIRED)
    private String province;

    @Schema(description = "省份编码")
    private String provinceCode;

    @NotBlank(message = "城市不能为空")
    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Schema(description = "城市编码")
    private String cityCode;

    @NotBlank(message = "区/县不能为空")
    @Schema(description = "区/县", requiredMode = Schema.RequiredMode.REQUIRED)
    private String district;

    @Schema(description = "区/县编码")
    private String districtCode;

    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detailAddress;

    @Schema(description = "邮编")
    private String postalCode;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;

    @Schema(description = "标签: 家/公司/学校等")
    private String label;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
