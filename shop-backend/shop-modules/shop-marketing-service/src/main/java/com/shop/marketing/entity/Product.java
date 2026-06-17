package com.shop.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品实体（用于跨表查询）
 *
 * @author shop
 * @since 2026-06-15
 */
@Data
@TableName("pms_product")
public class Product {

    @TableId
    private Long id;

    private String name;

    private String mainImage;

    private BigDecimal salePrice;

    private Integer sales;
}
