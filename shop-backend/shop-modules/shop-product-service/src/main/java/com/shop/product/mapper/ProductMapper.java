package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper
 *
 * @author shop
 * @since 2026-04-22
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据ID查询商品（包含已删除）
     */
    @Select("SELECT * FROM pms_product WHERE id = #{id}")
    Product selectByIdIncludeDeleted(@Param("id") Long id);

    /**
     * 增加销量
     */
    @Update("UPDATE pms_product SET sales = sales + #{quantity} WHERE id = #{productId} AND deleted = 0")
    int increaseSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 扣减库存
     */
    @Update("UPDATE pms_product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity} AND deleted = 0")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
