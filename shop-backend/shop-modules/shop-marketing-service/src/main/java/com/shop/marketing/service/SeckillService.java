package com.shop.marketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.SeckillProductCreateRequest;
import com.shop.marketing.controller.request.SeckillSessionCreateRequest;
import com.shop.marketing.controller.response.SeckillOrderResponse;
import com.shop.marketing.controller.response.SeckillProductResponse;
import com.shop.marketing.controller.response.SeckillSessionResponse;
import com.shop.marketing.entity.SeckillOrder;
import com.shop.marketing.entity.SeckillProduct;
import com.shop.marketing.entity.SeckillSession;

import java.util.List;

public interface SeckillService extends IService<SeckillSession> {

    SeckillSessionResponse createSession(SeckillSessionCreateRequest request);

    PageResult<SeckillSessionResponse> pageSessions(Integer pageNum, Integer pageSize);

    List<SeckillSessionResponse> listActiveSessions();

    SeckillProductResponse addProduct(Long sessionId, SeckillProductCreateRequest request);

    List<SeckillProductResponse> getSessionProducts(Long sessionId);

    /** 核心秒杀逻辑 */
    SeckillOrderResponse executeSeckill(Long sessionId, Long productId, Long userId);

    /** 将秒杀商品库存加载到Redis */
    void loadStockToRedis(Long sessionId);

    /** 同步秒杀库存到数据库 */
    void syncStockToDb();

    /** 处理秒杀消息 */
    void processSeckillMessages();

    /** 处理秒杀超时订单 */
    void processTimeoutOrders();
}
