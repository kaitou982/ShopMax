package com.shop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.PromotionCreateRequest;
import com.shop.marketing.controller.request.PromotionUpdateRequest;
import com.shop.marketing.controller.response.PromotionResponse;
import com.shop.marketing.entity.Promotion;
import com.shop.marketing.mapper.PromotionMapper;
import com.shop.marketing.service.PromotionScopeService;
import com.shop.marketing.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements PromotionService {

    private final PromotionScopeService promotionScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromotionResponse create(PromotionCreateRequest request) {
        Promotion promotion = new Promotion();
        BeanUtils.copyProperties(request, promotion);
        promotion.setStatus(determineStatus(promotion.getStartTime(), promotion.getEndTime()));

        baseMapper.insert(promotion);
        log.info("创建促销活动成功: id={}, name={}", promotion.getId(), promotion.getName());
        return convertToResponse(promotion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromotionResponse update(Long id, PromotionUpdateRequest request) {
        Promotion promotion = getEntityById(id);
        BeanUtils.copyProperties(request, promotion);
        promotion.setStatus(determineStatus(promotion.getStartTime(), promotion.getEndTime()));

        baseMapper.updateById(promotion);
        log.info("更新促销活动成功: id={}", id);
        return convertToResponse(promotion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Promotion promotion = getEntityById(id);
        baseMapper.deleteById(id);

        promotionScopeService.removeByPromotionId(id);

        log.info("删除促销活动成功: id={}", id);
    }

    @Override
    public PromotionResponse getById(Long id) {
        return convertToResponse(getEntityById(id));
    }

    @Override
    public PageResult<PromotionResponse> page(Integer pageNum, Integer pageSize, Integer status) {
        Page<Promotion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getDeleted, 0);
        if (status != null) {
            wrapper.eq(Promotion::getStatus, status);
        }
        wrapper.orderByDesc(Promotion::getCreateTime);

        Page<Promotion> result = baseMapper.selectPage(page, wrapper);
        List<PromotionResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    public List<PromotionResponse> listActive() {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getDeleted, 0);
        wrapper.eq(Promotion::getStatus, 1);
        wrapper.orderByDesc(Promotion::getCreateTime);

        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        Promotion promotion = getEntityById(id);
        promotion.setStatus(1);
        baseMapper.updateById(promotion);
        log.info("启用促销活动: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        Promotion promotion = getEntityById(id);
        promotion.setStatus(3);
        baseMapper.updateById(promotion);
        log.info("停用促销活动: id={}", id);
    }

    private Promotion getEntityById(Long id) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getId, id);
        wrapper.eq(Promotion::getDeleted, 0);

        Promotion promotion = baseMapper.selectOne(wrapper);
        if (promotion == null) {
            throw new BusinessException("促销活动不存在");
        }
        return promotion;
    }

    private Integer determineStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) return 0;
        if (now.isAfter(endTime)) return 2;
        return 1;
    }

    private PromotionResponse convertToResponse(Promotion promotion) {
        PromotionResponse response = new PromotionResponse();
        BeanUtils.copyProperties(promotion, response);
        // Re-evaluate status based on current time
        response.setStatus(determineStatus(promotion.getStartTime(), promotion.getEndTime()));
        return response;
    }
}
