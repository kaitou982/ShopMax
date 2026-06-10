package com.shop.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Category;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author shop
 * @since 2026-04-22
 */
public interface CategoryService extends IService<Category> {

    /**
     * 创建分类
     */
    Category create(Category category);

    /**
     * 更新分类
     */
    Category update(Long id, Category category);

    /**
     * 删除分类
     */
    void delete(Long id);

    /**
     * 根据ID获取分类
     */
    Category getById(Long id);

    /**
     * 分页查询分类
     */
    PageResult<Category> page(Integer pageNum, Integer pageSize, Long parentId);

    /**
     * 获取所有启用的一级分类
     */
    List<Category> listFirstLevel();

    /**
     * 获取子分类列表
     */
    List<Category> listChildren(Long parentId);

    /**
     * 获取分类树
     */
    List<Category> getCategoryTree();
}
