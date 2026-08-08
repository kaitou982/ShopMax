package com.shop.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 *
 * @author shop
 * @since 2026-04-22
 */
public interface ProductService extends IService<Product> {

    /**
     * 创建商品
     */
    Product create(Product product);

    /**
     * 更新商品
     */
    Product update(Long id, Product product);

    /**
     * 删除商品
     */
    void delete(Long id);

    /**
     * 根据ID获取商品
     */
    Product getById(Long id);

    /**
     * 分页查询商品
     */
    PageResult<Product> page(Integer pageNum, Integer pageSize, Long categoryId, String keyword, Integer status, String sortBy);

    /**
     * 上架商品
     */
    void onShelf(Long id);

    /**
     * 下架商品
     */
    void offShelf(Long id);

    /**
     * 获取推荐商品
     */
    List<Product> listRecommend(Integer limit);

    /**
     * 获取新品
     */
    List<Product> listNew(Integer limit);

    /**
     * 新品分页查询（C端，带时间过滤）
     */
    PageResult<Product> pageNew(Integer pageNum, Integer pageSize, Long categoryId, String sortBy);

    /**
     * 扣减库存（内部接口）
     */
    void deductStock(Long productId, Integer quantity);

    /**
     * 恢复库存（内部接口）
     */
    void restoreStock(Long productId, Integer quantity);

    /**
     * 增加销量（内部接口）
     */
    void addSales(Long productId, Integer quantity);

    /**
     * 批量恢复库存（内部接口）
     */
    void batchRestoreStock(List<Map<String, Object>> items);

    /**
     * 新品分页查询（内部接口）
     */
    Map<String, Object> getNewProductPage(int pageNum, int pageSize, Long categoryId);

    /**
     * 新品统计（内部接口）
     */
    Map<String, Object> getNewProductStats();

    /**
     * 批量获取商品基本信息（内部接口）
     */
    Map<String, Object> getBatchProductInfo(List<Long> ids);

    /**
     * 批量标记新品（内部接口）
     */
    void batchMarkNew(List<Long> ids);

    /**
     * 批量取消新品（内部接口）
     */
    void batchUnmarkNew(List<Long> ids);

    /**
     * 更新新品设置（内部接口）
     */
    void updateNewProductSettings(Long id, Integer sort, String startTime, String endTime);
}
