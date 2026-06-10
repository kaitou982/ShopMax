package com.shop.user.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户收货地址响应
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "用户收货地址响应")
public class UserAddressResponse {

    @Schema(description = "地址ID")
    private Long addressId;

    @Schema(description = "收货人姓名")
    private String receiverName;

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

    @Schema(description = "完整地址")
    private String fullAddress;

    @Schema(description = "邮编")
    private String postalCode;

    @Schema(description = "是否默认: 0-否 1-是")
    private Integer isDefault;

    @Schema(description = "标签: 家/公司/学校等")
    private String label;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
