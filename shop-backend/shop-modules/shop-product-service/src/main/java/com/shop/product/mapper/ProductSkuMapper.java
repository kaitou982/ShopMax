package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品SKU Mapper
 *
 * @author shop
 * @since 2026-04-22
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    /**
     * 根据商品ID查询SKU列表
     */
    @Select("SELECT * FROM pms_product_sku WHERE product_id = #{productId} AND deleted = 0")
    List<ProductSku> selectByProductId(@Param("productId") Long productId);

    /**
     * 扣减SKU库存
     */
    @Update("UPDATE pms_product_sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity} AND deleted = 0")
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
