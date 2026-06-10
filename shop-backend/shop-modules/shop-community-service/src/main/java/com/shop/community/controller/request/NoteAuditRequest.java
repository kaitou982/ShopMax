package com.shop.community.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteAuditRequest {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    private String rejectReason;
}
