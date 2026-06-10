package com.shop.marketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.marketing.entity.PromotionScope;

import java.util.List;

public interface PromotionScopeService extends IService<PromotionScope> {

    void saveScopes(Long promotionId, List<PromotionScope> scopes);

    List<PromotionScope> listByPromotionId(Long promotionId);

    void removeByPromotionId(Long promotionId);
}
