package com.shop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.common.service.AlertService;
import com.shop.common.service.MetricsService;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.SeckillProductCreateRequest;
import com.shop.marketing.controller.request.SeckillSessionCreateRequest;
import com.shop.marketing.controller.response.SeckillOrderResponse;
import com.shop.marketing.controller.response.SeckillProductResponse;
import com.shop.marketing.controller.response.SeckillSessionResponse;
import com.shop.marketing.entity.Product;
import com.shop.marketing.entity.SeckillMessage;
import com.shop.marketing.entity.SeckillOrder;
import com.shop.marketing.entity.SeckillProduct;
import com.shop.marketing.entity.SeckillSession;
import com.shop.marketing.mapper.ProductMapper;
import com.shop.marketing.mapper.SeckillMessageMapper;
import com.shop.marketing.mapper.SeckillOrderMapper;
import com.shop.marketing.mapper.SeckillProductMapper;
import com.shop.marketing.mapper.SeckillSessionMapper;
import com.shop.marketing.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.json.JSONUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl extends ServiceImpl<SeckillSessionMapper, SeckillSession> implements SeckillService {

    private final SeckillSessionMapper sessionMapper;
    private final SeckillProductMapper productMapper;
    private final ProductMapper productMapper2;
    private final SeckillOrderMapper orderMapper;
    private final SeckillMessageMapper messageMapper;
    private final RedisUtil redisUtil;
    private final AlertService alertService;
    private final MetricsService metricsService;

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

        List<SeckillProduct> seckillProducts = productMapper.selectList(wrapper);

        // 批量查询商品信息
        List<Long> productIds = seckillProducts.stream()
                .map(SeckillProduct::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.in(Product::getId, productIds);
            List<Product> products = productMapper2.selectList(productWrapper);
            productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
        }

        final Map<Long, Product> finalProductMap = productMap;

        return seckillProducts.stream()
                .map(p -> {
                    SeckillProductResponse r = convertProductToResponse(p);

                    // Overlay Redis stock for real-time display
                    String stockKey = SECKILL_STOCK_KEY + p.getId();
                    Integer redisStock = redisUtil.get(stockKey);
                    if (redisStock != null) {
                        r.setSeckillStock(redisStock);
                    }

                    // 填充商品信息
                    Product product = finalProductMap.get(p.getProductId());
                    if (product != null) {
                        r.setProductName(product.getName());
                        r.setProductImage(product.getMainImage());
                        r.setOriginalPrice(product.getSalePrice());
                        r.setSoldCount(product.getSales());
                    }

                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SeckillOrderResponse executeSeckill(Long sessionId, Long productId, Long userId) {
        long startTime = System.currentTimeMillis();

        // 记录秒杀请求
        metricsService.recordSeckillRequest();

        // ========== 第一步：参数校验 ==========
        SeckillSession session = getSessionById(sessionId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new BusinessException("当前不在秒杀时间段内");
        }

        // 检查商品是否存在
        LambdaQueryWrapper<SeckillProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(SeckillProduct::getSessionId, sessionId);
        productWrapper.eq(SeckillProduct::getProductId, productId);
        productWrapper.eq(SeckillProduct::getStatus, 1);
        productWrapper.eq(SeckillProduct::getDeleted, 0);
        SeckillProduct seckillProduct = productMapper.selectOne(productWrapper);
        if (seckillProduct == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        // ========== 第二步：Redis 分布式锁（防止重复请求）==========
        String lockKey = "seckill:lock:" + sessionId + ":" + userId + ":" + productId;
        Boolean locked = redisUtil.setIfAbsent(lockKey, "1", 5, java.util.concurrent.TimeUnit.SECONDS);
        if (!locked) {
            throw new BusinessException("请求处理中，请勿重复提交");
        }

        try {
            // ========== 第三步：Redis Lua 脚本原子扣减库存 ==========
            String stockKey = SECKILL_STOCK_KEY + productId;
            String userKey = SECKILL_USER_KEY + sessionId + ":" + userId + ":" + productId;

            // Lua 脚本：检查用户是否已购买 + 检查库存 + 扣减库存 + 记录用户
            String luaScript =
                "local stockKey = KEYS[1] " +
                "local userKey = KEYS[2] " +
                "local expireSeconds = tonumber(ARGV[1]) " +
                "if redis.call('exists', userKey) == 1 then return -1 end " +
                "local stock = redis.call('get', stockKey) " +
                "if not stock or tonumber(stock) <= 0 then return 0 end " +
                "redis.call('decr', stockKey) " +
                "redis.call('set', userKey, '1', 'EX', expireSeconds) " +
                "return 1";

            Long result = redisUtil.executeLua(luaScript, Arrays.asList(stockKey, userKey), 900);

            if (result == null || result == 0) {
                metricsService.recordSeckillFailure("sold_out");
                throw new BusinessException("秒杀商品已售罄");
            }
            if (result == -1) {
                metricsService.recordSeckillFailure("duplicate");
                throw new BusinessException("您已参与过该秒杀活动");
            }

            // ========== 第四步：数据库事务 ==========
            try {
                SeckillOrder order = executeSeckillInTransaction(userId, sessionId, seckillProduct, startTime);
                return convertOrderToResponse(order);
            } catch (Exception e) {
                // ========== 第五步：事务失败，回滚 Redis ==========
                rollbackRedisStock(stockKey, userKey);
                throw e;
            }
        } finally {
            redisUtil.delete(lockKey);
        }
    }

    /**
     * 数据库事务内操作：扣减库存 + 创建订单 + 保存消息
     */
    @Transactional(rollbackFor = Exception.class)
    protected SeckillOrder executeSeckillInTransaction(Long userId, Long sessionId, SeckillProduct seckillProduct, long startTime) {
        // 4.1 DB 扣减库存（乐观锁）
        int rows = productMapper.decrementStock(seckillProduct.getId());
        if (rows == 0) {
            throw new BusinessException("库存不足");
        }

        // 4.2 创建秒杀订单
        SeckillOrder order = new SeckillOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setSessionId(sessionId);
        order.setProductId(seckillProduct.getProductId());
        order.setSeckillPrice(seckillProduct.getSeckillPrice());
        order.setStatus(0); // 待支付
        order.setExpireTime(LocalDateTime.now().plusMinutes(15)); // 15分钟超时
        orderMapper.insert(order);

        // 4.3 保存本地消息（用于异步创建正式订单）
        SeckillMessage message = new SeckillMessage();
        message.setMessageType("SECKILL_ORDER");
        message.setBusinessId(order.getOrderNo());
        message.setContent(JSONUtil.toJsonStr(order));
        message.setStatus(0);
        message.setRetryCount(0);
        message.setMaxRetry(3);
        message.setNextRetryTime(LocalDateTime.now());
        messageMapper.insert(message);

        log.info("秒杀成功: sessionId={}, productId={}, userId={}, orderNo={}", sessionId, seckillProduct.getProductId(), userId, order.getOrderNo());

        // 记录秒杀成功
        metricsService.recordSeckillSuccess();
        metricsService.recordSeckillDuration(System.currentTimeMillis() - startTime);

        return order;
    }

    /**
     * 回滚 Redis 库存和用户记录
     */
    private void rollbackRedisStock(String stockKey, String userKey) {
        String rollbackScript =
            "local stockKey = KEYS[1] " +
            "local userKey = KEYS[2] " +
            "redis.call('del', userKey) " +
            "redis.call('incr', stockKey) " +
            "return 1";
        redisUtil.executeLua(rollbackScript, Arrays.asList(stockKey, userKey));

        // 记录库存回滚
        metricsService.recordStockRollback();

        log.warn("Redis 库存已回滚: stockKey={}, userKey={}", stockKey, userKey);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "SK" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
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

    /**
     * 定时处理秒杀消息（每5秒）
     *
     * 调用订单服务创建正式订单
     */
    @Scheduled(fixedDelay = 5000)
    public void processSeckillMessages() {
        List<SeckillMessage> messages = messageMapper.selectPendingMessages(LocalDateTime.now(), 100);

        for (SeckillMessage message : messages) {
            try {
                // 解析消息内容
                SeckillOrder order = JSONUtil.toBean(message.getContent(), SeckillOrder.class);

                // TODO: 调用订单服务创建正式订单
                // 这里需要调用订单服务的 API，暂时跳过
                // Long orderId = orderService.createSeckillOrder(order);

                // 更新消息状态为已处理
                message.setStatus(1);
                messageMapper.updateById(message);

                // 记录消息处理成功
                metricsService.recordMessageProcess("success");

                log.info("秒杀消息处理成功: orderNo={}", message.getBusinessId());
            } catch (Exception e) {
                log.error("秒杀消息处理失败: orderNo={}", message.getBusinessId(), e);

                // 记录消息处理失败
                metricsService.recordMessageProcess("failure");

                // 更新重试次数
                message.setRetryCount(message.getRetryCount() + 1);
                if (message.getRetryCount() >= message.getMaxRetry()) {
                    // 超过最大重试次数，标记为死信
                    message.setStatus(3);
                    log.error("秒杀消息已死信: orderNo={}", message.getBusinessId());
                    // 发送告警通知
                    alertService.sendDeadLetterAlert(message.getMessageType(), message.getBusinessId());
                    // 记录死信消息
                    metricsService.recordDeadLetter();
                } else {
                    // 设置下次重试时间（指数退避）
                    message.setStatus(2);
                    message.setNextRetryTime(LocalDateTime.now().plusMinutes(message.getRetryCount() * 5L));
                }
                messageMapper.updateById(message);
            }
        }
    }

    /**
     * 定时处理超时订单（每分钟）
     *
     * 扫描超时未支付订单，取消订单并回滚库存
     */
    @Scheduled(fixedDelay = 60000)
    public void processTimeoutOrders() {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getStatus, 0);
        wrapper.lt(SeckillOrder::getExpireTime, LocalDateTime.now());
        wrapper.eq(SeckillOrder::getDeleted, 0);

        List<SeckillOrder> timeoutOrders = orderMapper.selectList(wrapper);

        for (SeckillOrder order : timeoutOrders) {
            try {
                // 更新订单状态为超时
                order.setStatus(3);
                orderMapper.updateById(order);

                // 回滚库存
                rollbackStock(order.getSessionId(), order.getProductId());

                log.info("超时订单已处理: orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                log.error("超时订单处理失败: orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    /**
     * 回滚库存（Redis + DB）
     */
    private void rollbackStock(Long sessionId, Long productId) {
        // 查询秒杀商品
        LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillProduct::getSessionId, sessionId);
        wrapper.eq(SeckillProduct::getProductId, productId);
        wrapper.eq(SeckillProduct::getDeleted, 0);
        SeckillProduct seckillProduct = productMapper.selectOne(wrapper);

        if (seckillProduct != null) {
            // 回滚 Redis 库存
            String stockKey = SECKILL_STOCK_KEY + seckillProduct.getId();
            redisUtil.increment(stockKey, 1);

            // 回滚 DB 库存
            seckillProduct.setSeckillStock(seckillProduct.getSeckillStock() + 1);
            productMapper.updateById(seckillProduct);

            log.info("库存已回滚: sessionId={}, productId={}", sessionId, productId);
        }
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

    private SeckillOrderResponse convertOrderToResponse(SeckillOrder order) {
        SeckillOrderResponse response = new SeckillOrderResponse();
        BeanUtils.copyProperties(order, response);
        return response;
    }
}
