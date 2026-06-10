package com.shop.customerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.customerservice.entity.CsFaq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CsFaqMapper extends BaseMapper<CsFaq> {

    @Select("SELECT * FROM csms_faq WHERE status = 1 AND deleted = 0 AND (question LIKE CONCAT('%', #{keyword}, '%') OR category = #{keyword}) ORDER BY sort_order LIMIT 5")
    List<CsFaq> search(@Param("keyword") String keyword);

    @Select("SELECT * FROM csms_faq WHERE status = 1 AND category = #{category} AND deleted = 0 ORDER BY sort_order")
    List<CsFaq> selectByCategory(@Param("category") String category);
}
