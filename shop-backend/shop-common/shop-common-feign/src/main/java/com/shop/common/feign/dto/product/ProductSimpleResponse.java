package com.shop.common.feign.dto.product;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品简要信息（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class ProductSimpleResponse {

    private Long id;

    private String name;

    private BigDecimal salePrice;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer sales;

    private String mainImage;
}
