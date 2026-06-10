package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("pms_product_review")
public class ProductReview implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private Long productId;
    private Integer rating;
    private String content;
    private String images;
    private String replyContent;
    private LocalDateTime replyTime;
    private Integer isAnonymous;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
