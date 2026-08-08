package com.shop.common.feign.dto.user;

import lombok.Data;

/**
 * 店家入驻申请请求（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class StoreApplyRequest {

    private String storeName;

    private String storeLogo;

    private String storeDescription;
}
