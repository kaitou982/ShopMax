package com.shop.customerservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FaqImportRequest {

    @NotEmpty(message = "导入数据不能为空")
    private List<FaqItem> items;

    @Data
    public static class FaqItem {
        private String category;
        private String question;
        private String answer;
        private Integer sortOrder;
    }
}
