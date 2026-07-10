package com.shop.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 新品管理 Mapper（Admin端，自定义查询）
 *
 * @author shop
 * @since 2026-06-17
 */
@Mapper
public interface NewProductMapper {

    /**
     * 新品分页列表（联表查分类名）
     */
    @Select("""
        SELECT p.id, p.name, p.main_image, p.sale_price, p.is_new, p.new_product_sort,
               p.new_product_start_time, p.new_product_end_time, p.create_time,
               c.name AS category_name
        FROM pms_product p
        LEFT JOIN pms_category c ON p.category_id = c.id
        WHERE p.deleted = 0 AND p.is_new = 1
        ORDER BY p.new_product_sort DESC, p.create_time DESC
        LIMIT #{offset}, #{limit}
    """)
    List<Map<String, Object>> selectNewProductPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 新品分页列表（带分类筛选）
     */
    @Select("""
        SELECT p.id, p.name, p.main_image, p.sale_price, p.is_new, p.new_product_sort,
               p.new_product_start_time, p.new_product_end_time, p.create_time,
               c.name AS category_name
        FROM pms_product p
        LEFT JOIN pms_category c ON p.category_id = c.id
        WHERE p.deleted = 0 AND p.is_new = 1 AND p.category_id = #{categoryId}
        ORDER BY p.new_product_sort DESC, p.create_time DESC
        LIMIT #{offset}, #{limit}
    """)
    List<Map<String, Object>> selectNewProductPageByCategory(@Param("categoryId") Long categoryId,
                                                              @Param("offset") int offset,
                                                              @Param("limit") int limit);

    /**
     * 新品总数
     */
    @Select("SELECT COUNT(*) FROM pms_product WHERE deleted = 0 AND is_new = 1")
    int countNewProducts();

    /**
     * 按分类统计新品数
     */
    @Select("SELECT COUNT(*) FROM pms_product WHERE deleted = 0 AND is_new = 1 AND category_id = #{categoryId}")
    int countNewProductsByCategory(@Param("categoryId") Long categoryId);

    /**
     * 进行中的新品数（时间范围内）
     */
    @Select("""
        SELECT COUNT(*) FROM pms_product
        WHERE deleted = 0 AND is_new = 1
        AND (new_product_start_time IS NULL OR new_product_start_time <= NOW())
        AND (new_product_end_time IS NULL OR new_product_end_time >= NOW())
    """)
    int countActiveNewProducts();

    /**
     * 即将过期的新品数（7天内过期）
     */
    @Select("""
        SELECT COUNT(*) FROM pms_product
        WHERE deleted = 0 AND is_new = 1
        AND new_product_end_time IS NOT NULL
        AND new_product_end_time > NOW()
        AND new_product_end_time <= DATE_ADD(NOW(), INTERVAL 7 DAY)
    """)
    int countExpiringNewProducts();

    /**
     * 今日新增新品数
     */
    @Select("""
        SELECT COUNT(*) FROM pms_product
        WHERE deleted = 0 AND is_new = 1
        AND DATE(create_time) = CURDATE()
    """)
    int countTodayNewProducts();

    /**
     * 批量标记新品
     */
    @org.apache.ibatis.annotations.Update("""
        <script>
        UPDATE pms_product SET is_new = 1 WHERE deleted = 0 AND id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    int batchMarkNew(@Param("ids") List<Long> ids);

    /**
     * 批量取消新品
     */
    @org.apache.ibatis.annotations.Update("""
        <script>
        UPDATE pms_product SET is_new = 0, new_product_sort = 0, new_product_start_time = NULL, new_product_end_time = NULL WHERE deleted = 0 AND id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    int batchUnmarkNew(@Param("ids") List<Long> ids);

    /**
     * 更新新品设置
     */
    @Update("UPDATE pms_product SET new_product_sort = #{sort}, new_product_start_time = #{startTime}, new_product_end_time = #{endTime} WHERE id = #{id} AND deleted = 0")
    int updateNewProductSettings(@Param("id") Long id, @Param("sort") Integer sort, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
