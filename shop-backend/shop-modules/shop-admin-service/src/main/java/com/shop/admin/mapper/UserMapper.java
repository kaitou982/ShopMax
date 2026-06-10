package com.shop.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.admin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COALESCE(COUNT(*), 0) FROM ums_user WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    Long countTodayNewUsers();

    @Select("SELECT COALESCE(COUNT(*), 0) FROM ums_user WHERE deleted = 0 AND DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayNewUsers();
}
