package com.shop.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 *
 * @author shop
 * @since 2026-06-15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流 key（默认使用方法名）
     */
    String key() default "";

    /**
     * 每秒允许的请求数
     */
    double permitsPerSecond() default 100;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
