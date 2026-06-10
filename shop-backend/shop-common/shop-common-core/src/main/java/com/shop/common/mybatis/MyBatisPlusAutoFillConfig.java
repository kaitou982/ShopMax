package com.shop.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充配置类
 * <p>
 * 配合 @TableField(fill = FieldFill.xxx) 注解使用，自动处理实体类中的时间字段
 * <p>
 * 支持的字段：
 * <ul>
 *     <li>createTime - 插入时自动填充当前时间</li>
 *     <li>updateTime - 插入和更新时自动填充当前时间</li>
 *     <li>deleted - 插入时自动填充 0（未删除）</li>
 * </ul>
 *
 * @author shop
 * @since 2026-04-27
 */
@Slf4j
@AutoConfiguration
public class MyBatisPlusAutoFillConfig implements MetaObjectHandler {

    private static final String FIELD_CREATE_TIME = "createTime";
    private static final String FIELD_UPDATE_TIME = "updateTime";
    private static final String FIELD_DELETED = "deleted";

    /**
     * 插入操作自动填充
     * <p>
     * 自动填充 createTime、updateTime 为当前时间，deleted 为 0
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();

        try {
            // 填充 createTime（严格模式：字段不为空则不填充）
            this.strictInsertFill(metaObject, FIELD_CREATE_TIME, LocalDateTime.class, now);

            // 填充 updateTime（严格模式：字段不为空则不填充）
            this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, now);

            // 填充 deleted 标志（严格模式：字段不为空则不填充）
            this.strictInsertFill(metaObject, FIELD_DELETED, Integer.class, 0);

            log.debug("插入操作自动填充完成: createTime={}, updateTime={}, deleted=0", now);
        } catch (Exception e) {
            log.warn("插入操作自动填充失败: {}", e.getMessage());
        }
    }

    /**
     * 更新操作自动填充
     * <p>
     * 自动填充 updateTime 为当前时间
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();

        try {
            // 填充 updateTime（严格模式：字段不为空则不填充）
            this.strictUpdateFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, now);

            log.debug("更新操作自动填充完成: updateTime={}", now);
        } catch (Exception e) {
            log.warn("更新操作自动填充失败: {}", e.getMessage());
        }
    }
}
