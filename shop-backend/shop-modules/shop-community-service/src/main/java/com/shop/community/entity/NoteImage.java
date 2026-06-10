package com.shop.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("cms_note_image")
public class NoteImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;
    private String imageUrl;
    private Integer sortOrder;
    private Integer width;
    private Integer height;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
