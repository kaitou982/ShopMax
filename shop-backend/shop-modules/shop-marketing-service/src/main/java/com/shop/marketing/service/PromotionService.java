package com.shop.marketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.PromotionCreateRequest;
import com.shop.marketing.controller.request.PromotionUpdateRequest;
import com.shop.marketing.controller.response.PromotionResponse;
import com.shop.marketing.entity.Promotion;

import java.util.List;

public interface PromotionService extends IService<Promotion> {

    PromotionResponse create(PromotionCreateRequest request);

    PromotionResponse update(Long id, PromotionUpdateRequest request);

    void delete(Long id);

    PromotionResponse getById(Long id);

    PageResult<PromotionResponse> page(Integer pageNum, Integer pageSize, Integer status);

    List<PromotionResponse> listActive();

    void enable(Long id);

    void disable(Long id);
}
