package com.shop.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ums_balance_log")
public class BalanceLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private BigDecimal changeAmount;
    private BigDecimal afterAmount;
    private Integer type;
    private String bizId;
    private String payChannel;
    private String remark;
    private LocalDateTime createTime;
}
