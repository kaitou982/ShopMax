package com.shop.product.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品 Elasticsearch 仓库
 *
 * @author shop
 * @since 2026-06-23
 */
@Repository
public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {

    /**
     * 根据分类ID查询商品
     */
    List<ProductDocument> findByCategoryIdAndStatus(String categoryId, Integer status);

    /**
     * 根据品牌ID查询商品
     */
    List<ProductDocument> findByBrandIdAndStatus(String brandId, Integer status);

    /**
     * 查询新品
     */
    List<ProductDocument> findByIsNewTrueAndStatusOrderBySortAsc(Integer status);

    /**
     * 查询热销商品
     */
    List<ProductDocument> findByIsHotTrueAndStatusOrderBySalesDesc(Integer status);
}
