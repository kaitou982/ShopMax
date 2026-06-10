package com.shop.user.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新收货地址请求
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "更新收货地址请求")
public class UserAddressUpdateRequest {

    @Schema(description = "收货人姓名")
    private String receiverName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "收货人手机号")
    private String receiverPhone;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "省份编码")
    private String provinceCode;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "城市编码")
    private String cityCode;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "区/县编码")
    private String districtCode;

    @Schema(description = "详细地址")
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
