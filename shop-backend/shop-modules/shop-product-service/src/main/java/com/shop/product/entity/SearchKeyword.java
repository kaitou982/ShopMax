package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索关键词记录实体
 *
 * @author shop
 * @since 2026-05-31
 */
@Data
@TableName("cms_search_keyword")
public class SearchKeyword implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String keyword;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime searchTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
