package com.shop.product.controller.response;

import lombok.Data;

import java.util.List;

/**
 * 搜索建议响应
 *
 * @author shop
 * @since 2026-05-31
 */
@Data
public class SuggestResponse {

    /**
     * 商品名称建议
     */
    private List<String> products;

    /**
     * 热搜词建议
     */
    private List<String> hotWords;
}
