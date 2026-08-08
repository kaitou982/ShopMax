package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU实体
 *
 * @author shop
 * @since 2026-04-22
 */
@Data
@TableName("pms_product_sku")
public class ProductSku implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * SPU ID
     */
    private Long productId;

    /**
     * SKU标题
     */
    private String title;

    /**
     * SKU图片
     */
    private String image;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * SKU规格属性，JSON格式 {"颜色":"红色","尺寸":"XL"}
     */
    private String specs;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
