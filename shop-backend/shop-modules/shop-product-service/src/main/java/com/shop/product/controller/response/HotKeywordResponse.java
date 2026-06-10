package com.shop.product.controller.response;

import lombok.Data;

/**
 * 热搜关键词响应
 *
 * @author shop
 * @since 2026-05-31
 */
@Data
public class HotKeywordResponse {

    private String keyword;

    private Long count;
}
