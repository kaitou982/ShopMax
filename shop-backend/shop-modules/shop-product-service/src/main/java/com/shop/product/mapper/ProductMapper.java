package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

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

    // ==================== 新品管理 ====================

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

    @Select("SELECT COUNT(*) FROM pms_product WHERE deleted = 0 AND is_new = 1")
    int countNewProducts();

    @Select("SELECT COUNT(*) FROM pms_product WHERE deleted = 0 AND is_new = 1 AND category_id = #{categoryId}")
    int countNewProductsByCategory(@Param("categoryId") Long categoryId);

    @Select("""
        SELECT COUNT(*) FROM pms_product
        WHERE deleted = 0 AND is_new = 1
        AND (new_product_start_time IS NULL OR new_product_start_time <= NOW())
        AND (new_product_end_time IS NULL OR new_product_end_time >= NOW())
    """)
    int countActiveNewProducts();

    @Select("""
        SELECT COUNT(*) FROM pms_product
        WHERE deleted = 0 AND is_new = 1
        AND new_product_end_time IS NOT NULL
        AND new_product_end_time > NOW()
        AND new_product_end_time <= DATE_ADD(NOW(), INTERVAL 7 DAY)
    """)
    int countExpiringNewProducts();

    @Select("""
        SELECT COUNT(*) FROM pms_product
        WHERE deleted = 0 AND is_new = 1
        AND DATE(create_time) = CURDATE()
    """)
    int countTodayNewProducts();

    @Update("""
        <script>
        UPDATE pms_product SET is_new = 1 WHERE deleted = 0 AND id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    int batchMarkNew(@Param("ids") List<Long> ids);

    @Update("""
        <script>
        UPDATE pms_product SET is_new = 0, new_product_sort = 0, new_product_start_time = NULL, new_product_end_time = NULL WHERE deleted = 0 AND id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    int batchUnmarkNew(@Param("ids") List<Long> ids);

    @Update("UPDATE pms_product SET new_product_sort = #{sort}, new_product_start_time = #{startTime}, new_product_end_time = #{endTime} WHERE id = #{id} AND deleted = 0")
    int updateNewProductSettings(@Param("id") Long id, @Param("sort") Integer sort, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
