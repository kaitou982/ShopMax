package com.shop.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.live.controller.request.LiveProductAddRequest;
import com.shop.live.controller.request.LiveProductUpdateRequest;
import com.shop.live.controller.response.LiveProductResponse;
import com.shop.live.entity.LiveProduct;
import com.shop.live.entity.LiveRoom;
import com.shop.live.mapper.LiveProductMapper;
import com.shop.live.mapper.LiveRoomMapper;
import com.shop.live.service.LiveProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveProductServiceImpl extends ServiceImpl<LiveProductMapper, LiveProduct> implements LiveProductService {

    private final LiveProductMapper productMapper;
    private final LiveRoomMapper liveRoomMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveProductResponse addProduct(LiveProductAddRequest request) {
        // Verify room exists
        LiveRoom room = liveRoomMapper.selectById(request.getRoomId());
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }

        LiveProduct product = new LiveProduct();
        product.setRoomId(request.getRoomId());
        product.setProductId(request.getProductId());
        product.setSkuId(request.getSkuId());
        product.setLivePrice(request.getLivePrice());
        product.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        product.setStatus(1);

        productMapper.insert(product);
        log.info("直播商品上架成功: roomId={}, productId={}", request.getRoomId(), product.getId());
        return convertToResponse(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProduct(Long id) {
        LiveProduct product = getEntityById(id);
        product.setStatus(0);
        productMapper.updateById(product);
        log.info("直播商品下架: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveProductResponse updateProduct(Long id, LiveProductUpdateRequest request) {
        LiveProduct product = getEntityById(id);
        if (request.getLivePrice() != null) product.setLivePrice(request.getLivePrice());
        if (request.getSortOrder() != null) product.setSortOrder(request.getSortOrder());
        productMapper.updateById(product);
        return convertToResponse(product);
    }

    @Override
    public List<LiveProductResponse> getRoomProducts(Long roomId) {
        LambdaQueryWrapper<LiveProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveProduct::getRoomId, roomId);
        wrapper.eq(LiveProduct::getDeleted, 0);
        wrapper.ne(LiveProduct::getStatus, 0);

        return productMapper.selectList(wrapper).stream()
                .sorted(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setExplaining(Long id) {
        LiveProduct product = getEntityById(id);

        // Reset all other products in the same room from 'explaining' to 'listed'
        LambdaQueryWrapper<LiveProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveProduct::getRoomId, product.getRoomId());
        wrapper.eq(LiveProduct::getStatus, 2);
        List<LiveProduct> explaining = productMapper.selectList(wrapper);
        for (LiveProduct p : explaining) {
            p.setStatus(1);
            productMapper.updateById(p);
        }

        // Set current product to explaining
        product.setStatus(2);
        productMapper.updateById(product);
        log.info("标记讲解中: roomId={}, productId={}", product.getRoomId(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unexplain(Long id) {
        LiveProduct product = getEntityById(id);
        if (product.getStatus() != 2) {
            throw new BusinessException("该商品不在讲解中状态");
        }

        product.setStatus(1); // 恢复为上架状态
        productMapper.updateById(product);
        log.info("取消讲解: roomId={}, productId={}", product.getRoomId(), id);
    }

    private LiveProduct getEntityById(Long id) {
        LambdaQueryWrapper<LiveProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveProduct::getId, id);
        wrapper.eq(LiveProduct::getDeleted, 0);

        LiveProduct product = productMapper.selectOne(wrapper);
        if (product == null) {
            throw new BusinessException("直播商品不存在");
        }
        return product;
    }

    private LiveProductResponse convertToResponse(LiveProduct product) {
        LiveProductResponse response = new LiveProductResponse();
        BeanUtils.copyProperties(product, response);
        return response;
    }
}
