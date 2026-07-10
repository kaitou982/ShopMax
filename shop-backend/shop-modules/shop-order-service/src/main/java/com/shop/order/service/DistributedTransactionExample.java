package com.shop.order.service;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Seata 分布式事务示例
 * <p>
 * 在订单创建等跨服务场景中使用 @GlobalTransactional 注解
 * 确保多个服务的数据一致性
 *
 * @author shop
 * @since 2026-06-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedTransactionExample {

    /**
     * 示例：创建订单的分布式事务
     * <p>
     * 涉及的操作：
     * 1. 创建订单记录（订单服务）
     * 2. 扣减库存（商品服务）
     * 3. 扣减用户余额（用户服务）
     * 4. 使用优惠券（营销服务）
     * <p>
     * 使用 @GlobalTransactional 注解，如果任何一步失败，所有操作都会回滚
     */
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    public void createOrderWithDistributedTx(Long userId, Long productId, Integer quantity) {
        log.info("开始分布式事务: userId={}, productId={}, quantity={}", userId, productId, quantity);

        // 1. 创建订单（本地操作）
        // orderService.createOrder(userId, productId, quantity);

        // 2. 扣减库存（远程调用商品服务）
        // productService.deductStock(productId, quantity);

        // 3. 扣减用户余额（远程调用用户服务）
        // userService.deductBalance(userId, amount);

        // 4. 使用优惠券（远程调用营销服务）
        // couponService.useCoupon(userId, couponId);

        log.info("分布式事务完成: userId={}", userId);
    }

    /**
     * 示例：退款的分布式事务
     */
    @GlobalTransactional(name = "refund-order-tx", rollbackFor = Exception.class)
    public void refundOrderWithDistributedTx(Long orderId, Long userId) {
        log.info("开始退款分布式事务: orderId={}, userId={}", orderId, userId);

        // 1. 更新订单状态为已退款（订单服务）
        // orderService.updateOrderStatus(orderId, OrderStatus.REFUNDED);

        // 2. 恢复库存（商品服务）
        // productService.restoreStock(orderId);

        // 3. 退还用户余额（用户服务）
        // userService.refundBalance(userId, amount);

        // 4. 恢复优惠券（营销服务）
        // couponService.restoreCoupon(orderId);

        log.info("退款分布式事务完成: orderId={}", orderId);
    }
}
