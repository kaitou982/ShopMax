package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SPU实体
 *
 * @author shop
 * @since 2026-04-22
 */
@Data
@TableName("pms_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品副标题
     */
    private String subtitle;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品主图
     */
    private String mainImage;

    /**
     * 商品副图，逗号分隔
     */
    private String subImages;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 销售价
     */
    private BigDecimal salePrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 状态: 0-下架 1-上架 2-删除
     */
    private Integer status;

    /**
     * 是否推荐: 0-否 1-是
     */
    private Integer isRecommend;

    /**
     * 是否新品: 0-否 1-是
     */
    private Integer isNew;

    /**
     * 新品排序权重（数值越大越靠前）
     */
    private Integer newProductSort;

    /**
     * 新品上架时间（为空则永久展示）
     */
    private LocalDateTime newProductStartTime;

    /**
     * 新品下架时间（为空则不自动过期）
     */
    private LocalDateTime newProductEndTime;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建者用户ID（店家用户关联）
     */
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
