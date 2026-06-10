package com.shop.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Product;

import java.util.List;

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
}
