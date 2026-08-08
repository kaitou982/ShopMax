package com.shop.common.feign.fallback;

import com.shop.common.feign.client.ProductServiceClient;
import com.shop.common.feign.dto.product.CategorySimpleResponse;
import com.shop.common.feign.dto.product.ProductSimpleResponse;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 商品服务 FeignClient 降级工厂
 *
 * @author shop
 * @since 2026-06-17
 */
@Slf4j
@Component
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        log.error("商品服务调用失败: {}", cause.getMessage(), cause);
        return new ProductServiceClient() {
            @Override
            public Result<List<ProductSimpleResponse>> searchProducts(String keyword, Long categoryId, Double maxPrice, String sortBy, Integer pageSize) {
                return Result.success(Collections.emptyList());
            }

            @Override
            public Result<List<CategorySimpleResponse>> searchCategories(String name) {
                return Result.success(Collections.emptyList());
            }
        };
    }
}
