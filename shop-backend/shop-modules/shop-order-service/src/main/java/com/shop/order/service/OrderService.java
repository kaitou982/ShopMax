package com.shop.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.order.entity.Order;

import java.util.List;
import java.util.Map;

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

    /**
     * 更新订单状态（内部接口）
     */
    void updateOrderStatus(Long orderId, Integer status);

    /**
     * 获取订单基本信息（内部接口）
     */
    Map<String, Object> getOrderBasicInfo(Long orderId);

    /**
     * 根据订单号获取订单信息（内部接口）
     */
    Map<String, Object> getOrderInfoByOrderNo(String orderNo);

    /**
     * 根据订单号更新订单状态（内部接口）
     */
    void updateOrderStatusByOrderNo(String orderNo, Integer status);

    /**
     * 获取订单商品明细（内部接口）
     */
    List<Map<String, Object>> getOrderItems(Long orderId);

    /**
     * 获取仪表盘统计数据（内部接口）
     */
    Map<String, Object> getDashboardStats();

    /**
     * 自动取消超时订单
     */
    void autoCancelTimeoutOrders();
}
