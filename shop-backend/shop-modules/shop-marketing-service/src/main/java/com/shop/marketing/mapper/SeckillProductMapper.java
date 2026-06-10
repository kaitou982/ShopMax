package com.shop.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.marketing.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {

    @Update("UPDATE mms_seckill_product SET seckill_stock = seckill_stock - 1 WHERE id = #{id} AND seckill_stock > 0 AND deleted = 0")
    int decrementStock(@Param("id") Long id);
}
