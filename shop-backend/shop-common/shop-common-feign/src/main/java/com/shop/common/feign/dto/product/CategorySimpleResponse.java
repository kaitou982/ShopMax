package com.shop.common.feign.dto.product;

import lombok.Data;

/**
 * 分类简要信息（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class CategorySimpleResponse {

    private Long id;

    private String name;
}
