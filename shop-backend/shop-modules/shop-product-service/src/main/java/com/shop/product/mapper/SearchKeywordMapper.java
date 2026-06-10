package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.SearchKeyword;
import com.shop.product.controller.response.HotKeywordResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索关键词Mapper
 *
 * @author shop
 * @since 2026-05-31
 */
@Mapper
public interface SearchKeywordMapper extends BaseMapper<SearchKeyword> {

    @Select("""
            SELECT keyword, COUNT(*) AS count
            FROM cms_search_keyword
            WHERE search_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
              AND deleted = 0
            GROUP BY keyword
            ORDER BY count DESC
            LIMIT #{limit}
            """)
    List<HotKeywordResponse> selectHotKeywords(@Param("limit") int limit);
}
