package com.shop.common.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @author shop
 * @since 2026-04-15
 */
@Data
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页数据")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "每页大小")
    private Long size;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long pages) {
        this.records = records;
        this.total = total;
        this.pages = pages;
    }

    public PageResult(List<T> records, Long total, Long pages, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.size = size;
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long pages) {
        return new PageResult<>(records, total, pages);
    }

    /**
     * 构建分页结果（带当前页信息）
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long pages, Long current, Long size) {
        return new PageResult<>(records, total, pages, current, size);
    }

    /**
     * 空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }
}
