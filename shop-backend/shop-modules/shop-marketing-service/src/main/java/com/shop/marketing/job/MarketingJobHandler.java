package com.shop.marketing.job;

import com.shop.marketing.service.GroupBuyService;
import com.shop.marketing.service.SeckillService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 营销服务 XXL-JOB 任务处理器
 *
 * @author shop
 * @since 2026-06-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingJobHandler {

    private final GroupBuyService groupBuyService;
    private final SeckillService seckillService;

    /**
     * 处理过期拼团
     * 原 @Scheduled(fixedDelay = 60000) 在 GroupBuyServiceImpl
     */
    @XxlJob("processExpiredGroupsJob")
    public void processExpiredGroups() {
        log.info("XXL-JOB: 处理过期拼团开始");
        try {
            groupBuyService.processExpiredGroups();
            log.info("XXL-JOB: 处理过期拼团完成");
        } catch (Exception e) {
            log.error("XXL-JOB: 处理过期拼团失败", e);
        }
    }

    /**
     * 秒杀库存同步到数据库
     * 原 @Scheduled(fixedDelay = 30000) 在 SeckillServiceImpl
     */
    @XxlJob("syncStockToDbJob")
    public void syncStockToDb() {
        log.info("XXL-JOB: 同步秒杀库存到数据库开始");
        try {
            seckillService.syncStockToDb();
            log.info("XXL-JOB: 同步秒杀库存到数据库完成");
        } catch (Exception e) {
            log.error("XXL-JOB: 同步秒杀库存到数据库失败", e);
        }
    }

    /**
     * 秒杀消息处理
     * 原 @Scheduled(fixedDelay = 5000) 在 SeckillServiceImpl
     */
    @XxlJob("processSeckillMessagesJob")
    public void processSeckillMessages() {
        log.info("XXL-JOB: 处理秒杀消息开始");
        try {
            seckillService.processSeckillMessages();
            log.info("XXL-JOB: 处理秒杀消息完成");
        } catch (Exception e) {
            log.error("XXL-JOB: 处理秒杀消息失败", e);
        }
    }

    /**
     * 秒杀超时订单处理
     * 原 @Scheduled(fixedDelay = 60000) 在 SeckillServiceImpl
     */
    @XxlJob("processTimeoutOrdersJob")
    public void processTimeoutOrders() {
        log.info("XXL-JOB: 处理秒杀超时订单开始");
        try {
            seckillService.processTimeoutOrders();
            log.info("XXL-JOB: 处理秒杀超时订单完成");
        } catch (Exception e) {
            log.error("XXL-JOB: 处理秒杀超时订单失败", e);
        }
    }
}
