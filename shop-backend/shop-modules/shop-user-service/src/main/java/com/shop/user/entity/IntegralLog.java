package com.shop.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ums_integral_log")
public class IntegralLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer changeAmount;
    private Integer afterAmount;
    private Integer type;
    private String bizId;
    private String remark;
    private LocalDateTime createTime;
}
