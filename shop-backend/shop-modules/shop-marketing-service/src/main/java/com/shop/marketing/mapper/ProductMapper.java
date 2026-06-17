package com.shop.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.marketing.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper（用于跨表查询）
 *
 * @author shop
 * @since 2026-06-15
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
