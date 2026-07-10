package com.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.order.entity.RefundRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款记录Mapper
 *
 * @author shop
 * @since 2026-06-23
 */
@Mapper
public interface RefundRecordMapper extends BaseMapper<RefundRecord> {
}
