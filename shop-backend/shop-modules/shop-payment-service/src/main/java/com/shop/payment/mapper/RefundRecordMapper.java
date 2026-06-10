package com.shop.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.payment.entity.RefundRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款记录 Mapper
 *
 * @author shop
 * @since 2026-06-01
 */
@Mapper
public interface RefundRecordMapper extends BaseMapper<RefundRecord> {
}
