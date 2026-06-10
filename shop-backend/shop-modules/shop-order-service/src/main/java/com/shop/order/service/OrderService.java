package com.shop.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.order.entity.Order;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author shop
 * @since 2026-04-22
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    Order create(Order order);

    /**
     * 取消订单
     */
    void cancel(Long id, String reason);

    /**
     * 支付订单
     */
    void pay(Long id, Integer payType);

    /**
     * 发货
     */
    void ship(Long id);

    /**
     * 确认收货
     */
    void confirmReceive(Long id);

    /**
     * 删除订单
     */
    void delete(Long id);

    /**
     * 根据ID获取订单
     */
    Order getById(Long id);

    /**
     * 分页查询订单
     */
    PageResult<Order> page(Integer pageNum, Integer pageSize, Long userId, Integer status, String orderNo);

    /**
     * 获取订单详情（包含明细）
     */
    Order getDetail(Long id);

    /**
     * 申请退款
     */
    void refund(Long id, String reason);

    /**
     * 获取用户订单列表
     */
    List<Order> listByUserId(Long userId);
}
