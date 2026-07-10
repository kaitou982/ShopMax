package com.shop.product.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品搜索服务
 *
 * @author shop
 * @since 2026-06-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 全文搜索商品
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    public Page<ProductDocument> search(String keyword, int pageNum, int pageSize) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .fields("name", "categoryName", "brandName", "description")
                        .query(keyword)
                        .fuzziness("AUTO")
                ))
                .withPageable(PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "sort", "sales")))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        List<ProductDocument> products = hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return Page.empty();
    }

    /**
     * 按分类搜索
     */
    public List<ProductDocument> searchByCategory(String categoryId, int pageNum, int pageSize) {
        return productRepository.findByCategoryIdAndStatus(categoryId, 1);
    }

    /**
     * 按价格区间搜索
     */
    public List<ProductDocument> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int pageNum, int pageSize) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.range(r -> r
                        .field("price")
                        .gte(co.elastic.clients.json.JsonData.of(minPrice.doubleValue()))
                        .lte(co.elastic.clients.json.JsonData.of(maxPrice.doubleValue()))
                ))
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    /**
     * 获取新品推荐
     */
    public List<ProductDocument> getNewProducts(int limit) {
        return productRepository.findByIsNewTrueAndStatusOrderBySortAsc(1)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取热销商品
     */
    public List<ProductDocument> getHotProducts(int limit) {
        return productRepository.findByIsHotTrueAndStatusOrderBySalesDesc(1)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 保存商品到 ES
     */
    public void save(ProductDocument product) {
        productRepository.save(product);
        log.debug("商品已同步到ES: id={}", product.getId());
    }

    /**
     * 批量保存商品到 ES
     */
    public void saveAll(List<ProductDocument> products) {
        productRepository.saveAll(products);
        log.info("批量同步商品到ES: count={}", products.size());
    }

    /**
     * 从 ES 删除商品
     */
    public void delete(Long productId) {
        productRepository.deleteById(productId);
        log.debug("商品已从ES删除: id={}", productId);
    }
}
