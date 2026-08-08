package com.shop.product.elasticsearch;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品 Elasticsearch 文档
 *
 * @author shop
 * @since 2026-06-23
 */
@Data
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String brandId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String brandName;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Double)
    private BigDecimal originalPrice;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Integer)
    private Integer sales;

    @Field(type = FieldType.Keyword)
    private String mainImage;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Boolean)
    private Boolean isNew;

    @Field(type = FieldType.Boolean)
    private Boolean isHot;

    @Field(type = FieldType.Integer)
    private Integer sort;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
}
