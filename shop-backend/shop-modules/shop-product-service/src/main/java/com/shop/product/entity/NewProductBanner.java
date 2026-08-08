package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新品首发Banner实体
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
@TableName("pms_new_product_banner")
public class NewProductBanner implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Banner标题
     */
    private String title;

    /**
     * Banner图片
     */
    private String imageUrl;

    /**
     * 关联商品ID（点击跳转商品详情）
     */
    private Long productId;

    /**
     * 外部链接（与product_id二选一）
     */
    private String linkUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 展示开始时间
     */
    private LocalDateTime startTime;

    /**
     * 展示结束时间
     */
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
