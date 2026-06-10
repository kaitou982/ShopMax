package com.shop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.SeckillProductCreateRequest;
import com.shop.marketing.controller.request.SeckillSessionCreateRequest;
import com.shop.marketing.controller.response.SeckillProductResponse;
import com.shop.marketing.controller.response.SeckillSessionResponse;
import com.shop.marketing.entity.SeckillProduct;
import com.shop.marketing.entity.SeckillSession;
import com.shop.marketing.mapper.SeckillProductMapper;
import com.shop.marketing.mapper.SeckillSessionMapper;
import com.shop.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl extends ServiceImpl<SeckillSessionMapper, SeckillSession> implements SeckillService {

    private final SeckillSessionMapper sessionMapper;
    private final SeckillProductMapper productMapper;
    private final RedisUtil redisUtil;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:user:";
    private static final long STOCK_CACHE_TTL = 7200L; // 2 hours

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillSessionResponse createSession(SeckillSessionCreateRequest request) {
        SeckillSession session = new SeckillSession();
        BeanUtils.copyProperties(request, session);
        session.setStatus(determineSessionStatus(session.getStartTime(), session.getEndTime()));

        baseMapper.insert(session);
        log.info("创建秒杀场次成功: id={}, name={}", session.getId(), session.getName());
        return convertSessionToResponse(session);
    }

    @Override
    public PageResult<SeckillSessionResponse> pageSessions(Integer pageNum, Integer pageSize) {
        Page<SeckillSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillSession::getDeleted, 0);
        wrapper.orderByDesc(SeckillSession::getStartTime);

        Page<SeckillSession> result = baseMapper.selectPage(page, wrapper);
        List<SeckillSessionResponse> records = result.getRecords().stream()
                .map(this::convertSessionToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    public List<SeckillSessionResponse> listActiveSessions() {
        LambdaQueryWrapper<SeckillSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillSession::getDeleted, 0);
        wrapper.eq(SeckillSession::getStatus, 1);
        wrapper.orderByAsc(SeckillSession::getStartTime);

        return baseMapper.selectList(wrapper).stream()
                .map(this::convertSessionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillProductResponse addProduct(Long sessionId, SeckillProductCreateRequest request) {
        SeckillSession session = getSessionById(sessionId);

        SeckillProduct product = new SeckillProduct();
        product.setSessionId(sessionId);
        BeanUtils.copyProperties(request, product);

        productMapper.insert(product);
        log.info("添加秒杀商品成功: sessionId={}, productId={}", sessionId, product.getId());
        return convertProductToResponse(product);
    }

    @Override
    public List<SeckillProductResponse> getSessionProducts(Long sessionId) {
        LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillProduct::getSessionId, sessionId);
        wrapper.eq(SeckillProduct::getDeleted, 0);
        wrapper.orderByAsc(SeckillProduct::getSortOrder);

        return productMapper.selectList(wrapper).stream()
                .map(p -> {
                    SeckillProductResponse r = convertProductToResponse(p);
                    // Overlay Redis stock for real-time display
                    String stockKey = SECKILL_STOCK_KEY + p.getId();
                    Integer redisStock = redisUtil.get(stockKey);
                    if (redisStock != null) {
                        r.setSeckillStock(redisStock);
                    }
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean executeSeckill(Long sessionId, Long productId, Long userId) {
        // Validate session
        SeckillSession session = getSessionById(sessionId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new BusinessException("当前不在秒杀时间段内");
        }

        // Check user already bought
        String userKey = SECKILL_USER_KEY + sessionId + ":" + userId + ":" + productId;
        if (Boolean.TRUE.equals(redisUtil.hasKey(userKey))) {
            throw new BusinessException("您已参与该场次秒杀");
        }

        // Lua script: atomic check + decrement
        String luaScript =
            "local stockKey = KEYS[1] " +
            "local userKey = KEYS[2] " +
            "local stock = redis.call('get', stockKey) " +
            "if not stock or tonumber(stock) <= 0 then return 0 end " +
            "redis.call('decr', stockKey) " +
            "redis.call('set', userKey, '1', 'EX', 7200) " +
            "return 1";

        String stockKey = SECKILL_STOCK_KEY + productId;
        Long result = redisUtil.executeLua(luaScript, Arrays.asList(stockKey, userKey));

        if (result == null || result == 0) {
            throw new BusinessException("秒杀商品已售罄");
        }

        // Async: decrement DB stock (fire-and-forget; scheduled task also syncs)
        productMapper.decrementStock(productId);

        log.info("秒杀成功: sessionId={}, productId={}, userId={}", sessionId, productId, userId);
        return true;
    }

    @Override
    public void loadStockToRedis(Long sessionId) {
        LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillProduct::getSessionId, sessionId);
        wrapper.eq(SeckillProduct::getDeleted, 0);
        wrapper.eq(SeckillProduct::getStatus, 1);

        List<SeckillProduct> products = productMapper.selectList(wrapper);
        for (SeckillProduct p : products) {
            String key = SECKILL_STOCK_KEY + p.getId();
            redisUtil.set(key, p.getSeckillStock());
            redisUtil.expire(key, STOCK_CACHE_TTL, java.util.concurrent.TimeUnit.SECONDS);
        }
        log.info("加载秒杀库存到Redis: sessionId={}, count={}", sessionId, products.size());
    }

    /** 定时同步Redis库存到DB (每30秒) */
    @Scheduled(fixedDelay = 30000)
    public void syncStockToDb() {
        LambdaQueryWrapper<SeckillSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillSession::getStatus, 1);
        wrapper.eq(SeckillSession::getDeleted, 0);

        List<SeckillSession> sessions = baseMapper.selectList(wrapper);
        for (SeckillSession session : sessions) {
            LambdaQueryWrapper<SeckillProduct> pWrapper = new LambdaQueryWrapper<>();
            pWrapper.eq(SeckillProduct::getSessionId, session.getId());
            pWrapper.eq(SeckillProduct::getDeleted, 0);

            List<SeckillProduct> products = productMapper.selectList(pWrapper);
            for (SeckillProduct p : products) {
                String key = SECKILL_STOCK_KEY + p.getId();
                Integer redisStock = redisUtil.get(key);
                if (redisStock != null && redisStock >= 0) {
                    p.setSeckillStock(redisStock);
                    productMapper.updateById(p);
                }
            }
        }
        log.debug("秒杀库存同步完成: sessions={}", sessions.size());
    }

    private SeckillSession getSessionById(Long id) {
        LambdaQueryWrapper<SeckillSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillSession::getId, id);
        wrapper.eq(SeckillSession::getDeleted, 0);

        SeckillSession session = baseMapper.selectOne(wrapper);
        if (session == null) {
            throw new BusinessException("秒杀场次不存在");
        }
        return session;
    }

    private Integer determineSessionStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) return 0;
        if (now.isAfter(endTime)) return 2;
        return 1;
    }

    private SeckillSessionResponse convertSessionToResponse(SeckillSession session) {
        SeckillSessionResponse response = new SeckillSessionResponse();
        BeanUtils.copyProperties(session, response);
        response.setStatus(determineSessionStatus(session.getStartTime(), session.getEndTime()));
        return response;
    }

    private SeckillProductResponse convertProductToResponse(SeckillProduct product) {
        SeckillProductResponse response = new SeckillProductResponse();
        BeanUtils.copyProperties(product, response);
        return response;
    }
}
