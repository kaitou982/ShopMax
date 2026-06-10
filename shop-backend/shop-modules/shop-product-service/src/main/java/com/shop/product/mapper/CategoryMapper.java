package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分类Mapper
 *
 * @author shop
 * @since 2026-04-22
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据父ID查询分类列表
     */
    @Select("SELECT * FROM pms_category WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort")
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有启用的一级分类
     */
    @Select("SELECT * FROM pms_category WHERE level = 1 AND status = 1 AND deleted = 0 ORDER BY sort")
    List<Category> selectFirstLevelCategories();
}
