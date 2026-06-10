package com.shop.order.service;

import com.shop.order.controller.request.LogisticsCreateRequest;
import com.shop.order.controller.request.LogisticsTraceRequest;
import com.shop.order.entity.Logistics;
import com.shop.order.entity.LogisticsTrace;

import java.util.List;

/**
 * 物流服务接口
 *
 * @author shop
 * @since 2026-06-07
 */
public interface LogisticsService {

    /**
     * 创建物流信息
     *
     * @param request 物流信息
     * @return 物流实体
     */
    Logistics createLogistics(LogisticsCreateRequest request);

    /**
     * 添加物流轨迹
     *
     * @param logisticsId 物流ID
     * @param request 轨迹信息
     */
    void addTrace(Long logisticsId, LogisticsTraceRequest request);

    /**
     * 根据订单ID查询物流信息
     *
     * @param orderId 订单ID
     * @return 物流信息（含轨迹）
     */
    Logistics getLogisticsByOrderId(Long orderId);

    /**
     * 查询物流信息（带API更新）
     *
     * @param orderId 订单ID
     * @return 物流信息（含轨迹）
     */
    Logistics getLogisticsWithApiUpdate(Long orderId);

    /**
     * 获取物流详情（含轨迹）
     *
     * @param logisticsId 物流ID
     * @return 物流信息（含轨迹）
     */
    Logistics getLogisticsDetail(Long logisticsId);

    /**
     * 获取物流轨迹列表
     *
     * @param logisticsId 物流ID
     * @return 轨迹列表
     */
    List<LogisticsTrace> getTraces(Long logisticsId);

    /**
     * 更新物流状态
     *
     * @param logisticsId 物流ID
     * @param status 状态
     */
    void updateStatus(Long logisticsId, Integer status);

    /**
     * 签收物流
     *
     * @param logisticsId 物流ID
     */
    void signLogistics(Long logisticsId);

    /**
     * 手动刷新物流
     *
     * @param logisticsId 物流ID
     */
    void refreshLogistics(Long logisticsId);
}
