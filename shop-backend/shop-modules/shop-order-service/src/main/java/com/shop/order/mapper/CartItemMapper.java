package com.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.order.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 购物车Mapper
 *
 * @author shop
 * @since 2026-04-22
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * 查询用户购物车列表
     */
    @Select("SELECT * FROM oms_cart_item WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户选中的购物车商品
     */
    @Select("SELECT * FROM oms_cart_item WHERE user_id = #{userId} AND selected = 1 AND deleted = 0")
    List<CartItem> selectSelectedByUserId(@Param("userId") Long userId);

    /**
     * 更新购物车商品选中状态
     */
    @Update("UPDATE oms_cart_item SET selected = #{selected}, update_time = NOW() WHERE id = #{cartItemId} AND user_id = #{userId} AND deleted = 0")
    int updateSelected(@Param("cartItemId") Long cartItemId, @Param("userId") Long userId, @Param("selected") Integer selected);

    /**
     * 根据用户ID和SKU ID查询购物车商品
     */
    @Select("SELECT * FROM oms_cart_item WHERE user_id = #{userId} AND sku_id = #{skuId} AND deleted = 0 LIMIT 1")
    CartItem selectByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);
}
