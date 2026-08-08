package com.shop.product.service;

import com.shop.product.controller.response.HotKeywordResponse;
import com.shop.product.controller.response.SuggestResponse;
import com.shop.product.elasticsearch.ProductDocument;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务接口
 *
 * @author shop
 * @since 2026-05-31
 */
public interface SearchService {

    /**
     * 记录搜索关键词（含防刷去重）
     */
    void recordKeyword(String keyword, Long userId);

    /**
     * 获取热门搜索关键词
     */
    List<HotKeywordResponse> getHotKeywords(int limit);

    /**
     * 获取搜索建议（商品名 + 热搜词）
     */
    SuggestResponse getSuggestions(String keyword, int limit);

    /**
     * 全文搜索商品（使用 Elasticsearch）
     */
    Map<String, Object> searchProducts(String keyword, int pageNum, int pageSize);
}
