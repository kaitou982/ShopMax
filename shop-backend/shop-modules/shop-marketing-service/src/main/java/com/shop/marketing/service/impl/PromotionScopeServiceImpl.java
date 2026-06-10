package com.shop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.marketing.entity.PromotionScope;
import com.shop.marketing.mapper.PromotionScopeMapper;
import com.shop.marketing.service.PromotionScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionScopeServiceImpl extends ServiceImpl<PromotionScopeMapper, PromotionScope> implements PromotionScopeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScopes(Long promotionId, List<PromotionScope> scopes) {
        removeByPromotionId(promotionId);

        for (PromotionScope scope : scopes) {
            scope.setPromotionId(promotionId);
            baseMapper.insert(scope);
        }
        log.info("保存促销适用范围成功: promotionId={}, count={}", promotionId, scopes.size());
    }

    @Override
    public List<PromotionScope> listByPromotionId(Long promotionId) {
        LambdaQueryWrapper<PromotionScope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromotionScope::getPromotionId, promotionId);
        wrapper.eq(PromotionScope::getDeleted, 0);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByPromotionId(Long promotionId) {
        LambdaQueryWrapper<PromotionScope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromotionScope::getPromotionId, promotionId);
        baseMapper.delete(wrapper);
    }
}
