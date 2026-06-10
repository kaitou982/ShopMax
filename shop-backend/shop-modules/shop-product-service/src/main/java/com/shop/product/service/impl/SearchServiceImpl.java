package com.shop.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.redis.RedisUtil;
import com.shop.product.controller.response.HotKeywordResponse;
import com.shop.product.controller.response.SuggestResponse;
import com.shop.product.entity.Product;
import com.shop.product.entity.SearchKeyword;
import com.shop.product.mapper.ProductMapper;
import com.shop.product.mapper.SearchKeywordMapper;
import com.shop.product.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 *
 * @author shop
 * @since 2026-05-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final String CACHE_HOT_KEYWORDS = "search:hot";
    private static final String CACHE_SUGGEST_PREFIX = "search:suggest:";
    private static final String DEDUP_PREFIX = "search:dedup:";

    private final SearchKeywordMapper searchKeywordMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    @Override
    @Async
    public void recordKeyword(String keyword, Long userId) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }

        // 防刷：同一用户 1 分钟内同词不重复记录
        String dedupKey = DEDUP_PREFIX + (userId != null ? userId : "anon") + ":" + keyword.trim();
        Boolean isNew = redisUtil.setIfAbsent(dedupKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isNew)) {
            return;
        }

        try {
            SearchKeyword record = new SearchKeyword();
            record.setKeyword(keyword.trim());
            record.setUserId(userId);
            searchKeywordMapper.insert(record);
        } catch (Exception e) {
            log.warn("记录搜索关键词失败: keyword={}", keyword, e);
        }
    }

    @Override
    public List<HotKeywordResponse> getHotKeywords(int limit) {
        try {
            List<HotKeywordResponse> cached = redisUtil.get(CACHE_HOT_KEYWORDS);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取热搜缓存失败", e);
        }

        List<HotKeywordResponse> result = searchKeywordMapper.selectHotKeywords(limit);
        try {
            redisUtil.set(CACHE_HOT_KEYWORDS, result, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入热搜缓存失败", e);
        }
        return result;
    }

    @Override
    public SuggestResponse getSuggestions(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            SuggestResponse response = new SuggestResponse();
            response.setProducts(List.of());
            response.setHotWords(getHotKeywords(3).stream()
                    .map(HotKeywordResponse::getKeyword)
                    .collect(Collectors.toList()));
            return response;
        }

        String cacheKey = CACHE_SUGGEST_PREFIX + keyword.trim();
        try {
            SuggestResponse cached = redisUtil.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取建议缓存失败", e);
        }

        // 商品名匹配
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getName, keyword.trim());
        wrapper.eq(Product::getStatus, 1);
        wrapper.eq(Product::getDeleted, 0);
        wrapper.select(Product::getName);
        wrapper.last("LIMIT " + Math.min(limit, 10));
        List<String> productNames = productMapper.selectList(wrapper).stream()
                .map(Product::getName)
                .distinct()
                .collect(Collectors.toList());

        // 热搜词
        List<String> hotWords = getHotKeywords(3).stream()
                .map(HotKeywordResponse::getKeyword)
                .collect(Collectors.toList());

        SuggestResponse response = new SuggestResponse();
        response.setProducts(productNames);
        response.setHotWords(hotWords);

        try {
            redisUtil.set(cacheKey, response, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入建议缓存失败", e);
        }
        return response;
    }
}
