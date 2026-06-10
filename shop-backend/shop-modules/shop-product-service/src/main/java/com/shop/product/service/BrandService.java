package com.shop.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Brand;

import java.util.List;

/**
 * 品牌服务接口
 *
 * @author shop
 * @since 2026-04-22
 */
public interface BrandService extends IService<Brand> {

    /**
     * 创建品牌
     */
    Brand create(Brand brand);

    /**
     * 更新品牌
     */
    Brand update(Long id, Brand brand);

    /**
     * 删除品牌
     */
    void delete(Long id);

    /**
     * 根据ID获取品牌
     */
    Brand getById(Long id);

    /**
     * 分页查询品牌
     */
    PageResult<Brand> page(Integer pageNum, Integer pageSize);

    /**
     * 获取所有启用品牌
     */
    List<Brand> listAll();
}
