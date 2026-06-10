package com.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.order.entity.Logistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物流信息Mapper
 *
 * @author shop
 * @since 2026-06-07
 */
@Mapper
public interface LogisticsMapper extends BaseMapper<Logistics> {
}
