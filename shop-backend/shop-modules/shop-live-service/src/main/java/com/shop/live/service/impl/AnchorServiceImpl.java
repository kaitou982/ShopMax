package com.shop.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.live.controller.request.AnchorApplyRequest;
import com.shop.live.controller.request.AnchorAuditRequest;
import com.shop.live.controller.response.AnchorResponse;
import com.shop.live.entity.Anchor;
import com.shop.live.mapper.AnchorMapper;
import com.shop.live.service.AnchorService;
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
public class AnchorServiceImpl extends ServiceImpl<AnchorMapper, Anchor> implements AnchorService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnchorResponse apply(Long userId, AnchorApplyRequest request) {
        // Check if already applied
        LambdaQueryWrapper<Anchor> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Anchor::getUserId, userId);
        checkWrapper.eq(Anchor::getDeleted, 0);
        Anchor existing = baseMapper.selectOne(checkWrapper);
        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new BusinessException("您已是认证主播，无需重复申请");
            }
            if (existing.getStatus() == 0) {
                throw new BusinessException("您的申请正在审核中，请耐心等待");
            }
        }

        Anchor anchor = new Anchor();
        anchor.setUserId(userId);
        BeanUtils.copyProperties(request, anchor);
        anchor.setStatus(0);
        anchor.setLevel(1);
        anchor.setFansCount(0);
        anchor.setTotalLiveCount(0);
        anchor.setTotalDuration(0L);

        baseMapper.insert(anchor);
        log.info("主播申请成功: userId={}, id={}", userId, anchor.getId());
        return convertToResponse(anchor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnchorResponse audit(Long id, AnchorAuditRequest request) {
        Anchor anchor = getEntityById(id);

        if (anchor.getStatus() != 0) {
            throw new BusinessException("该申请已审核，无需重复操作");
        }

        anchor.setStatus(request.getStatus());
        anchor.setRejectReason(request.getRejectReason());
        anchor.setAuditTime(LocalDateTime.now());

        baseMapper.updateById(anchor);
        log.info("主播审核完成: id={}, status={}", id, request.getStatus());
        return convertToResponse(anchor);
    }

    @Override
    public AnchorResponse getById(Long id) {
        return convertToResponse(getEntityById(id));
    }

    @Override
    public AnchorResponse getByUserId(Long userId) {
        LambdaQueryWrapper<Anchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Anchor::getUserId, userId);
        wrapper.eq(Anchor::getDeleted, 0);

        Anchor anchor = baseMapper.selectOne(wrapper);
        if (anchor == null) {
            throw new BusinessException("您还未申请成为主播");
        }
        return convertToResponse(anchor);
    }

    @Override
    public PageResult<AnchorResponse> page(Integer pageNum, Integer pageSize, Integer status) {
        Page<Anchor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Anchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Anchor::getDeleted, 0);
        if (status != null) {
            wrapper.eq(Anchor::getStatus, status);
        }
        wrapper.orderByDesc(Anchor::getCreateTime);

        Page<Anchor> result = baseMapper.selectPage(page, wrapper);
        List<AnchorResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    private Anchor getEntityById(Long id) {
        LambdaQueryWrapper<Anchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Anchor::getId, id);
        wrapper.eq(Anchor::getDeleted, 0);

        Anchor anchor = baseMapper.selectOne(wrapper);
        if (anchor == null) {
            throw new BusinessException("主播不存在");
        }
        return anchor;
    }

    private AnchorResponse convertToResponse(Anchor anchor) {
        AnchorResponse response = new AnchorResponse();
        BeanUtils.copyProperties(anchor, response);
        return response;
    }
}
