package com.shop.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 通知类型: 1退款申请 2入驻审核 3内容审核 4库存预警 */
    private Integer type;

    private String title;
    private String content;

    /** 关联业务ID */
    private Long refId;

    /** 关联业务类型 */
    private String refType;

    /** 是否已读: 0未读 1已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
