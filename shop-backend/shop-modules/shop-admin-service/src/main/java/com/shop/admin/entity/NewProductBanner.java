package com.shop.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新品首发Banner实体（Admin端）
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

    private String title;

    private String imageUrl;

    private Long productId;

    private String linkUrl;

    private Integer sort;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
