package com.shop.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cms_note")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String content;
    private String coverUrl;
    private Integer contentType;
    private String videoUrl;
    private Integer videoDuration;
    private Integer status;
    private String rejectReason;
    private LocalDateTime auditTime;
    private Long auditUserId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer shareCount;
    private Integer isRecommended;
    private Integer isTop;
    private Integer isEssence;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String locationName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
