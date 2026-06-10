package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    @Select("SELECT AVG(rating) FROM pms_product_review WHERE product_id = #{productId} AND status = 1")
    Double avgRating(@Param("productId") Long productId);

    @Select("SELECT COUNT(1) FROM pms_product_review WHERE product_id = #{productId} AND status = 1")
    int countByProduct(@Param("productId") Long productId);
}
