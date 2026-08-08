package com.shop.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.enums.MemberLevelConstants;
import com.shop.common.enums.OrderStatus;
import com.shop.common.enums.PayMethod;
import com.shop.common.exception.BusinessException;
import com.shop.common.feign.client.InternalCouponClient;
import com.shop.common.feign.client.InternalPaymentClient;
import com.shop.common.feign.client.InternalProductClient;
import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.feign.client.NotificationClient;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.order.entity.Order;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.shop.order.entity.OrderItem;
import com.shop.order.entity.OrderLog;
import com.shop.order.entity.RefundRecord;
import com.shop.order.mapper.CouponReceiveMapper;
import com.shop.order.mapper.OrderItemMapper;
import com.shop.order.mapper.OrderLogMapper;
import com.shop.order.mapper.OrderMapper;
import com.shop.order.mapper.RefundRecordMapper;
import com.shop.order.mq.OrderMessageProducer;
import com.shop.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 订单服务实现
 *
 * @author shop
 * @since 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CouponReceiveMapper couponReceiveMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogMapper orderLogMapper;
    private final RefundRecordMapper refundRecordMapper;
    private final InternalCouponClient internalCouponClient;
    private final OrderMessageProducer orderMessageProducer;

    @Autowired(required = false)
    private InternalUserClient internalUserClient;

    @Autowired(required = false)
    private InternalProductClient internalProductClient;

    @Autowired(required = false)
    private InternalPaymentClient internalPaymentClient;

    @Autowired(required = false)
    private NotificationClient notificationClient;

    /** 订单超时时间（分钟） */
    private static final int ORDER_TIMEOUT_MINUTES = 30;
    /** 免费配送门槛 */
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("99.00");
    /** 默认运费 */
    private static final BigDecimal DEFAULT_FREIGHT = new BigDecimal("10.00");

    /**
     * 定时任务：自动取消超时未支付订单（每 60 秒执行一次）
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional(rollbackFor = Exception.class)
    public void autoCancelTimeoutOrders() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PENDING_PAY.getCode())
               .eq(Order::getDeleted, 0)
               .le(Order::getCreateTime, timeout);

        List<Order> timeoutOrders = baseMapper.selectList(wrapper);
        if (timeoutOrders.isEmpty()) return;

        for (Order order : timeoutOrders) {
            try {
                cancelOrderAndRestoreStock(order, "订单超时未支付，系统自动取消");
                log.info("自动取消超时订单: orderNo={}, createTime={}", order.getOrderNo(), order.getCreateTime());
            } catch (Exception e) {
                log.error("自动取消订单异常: orderNo={}, error={}", order.getOrderNo(), e.getMessage(), e);
            }
        }

        int count = timeoutOrders.size();
        if (count > 0) {
            log.info("自动取消超时订单完成: 本次处理 {} 笔", count);
        }
    }

    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Order create(Order order) {
        // 计算运费
        BigDecimal freight = calculateFreight(order.getTotalAmount());
        order.setFreightAmount(freight);

        // 会员等级折扣（银卡98折、金卡95折、钻石9折）
        int memberLevel = 1;
        try {
            if (internalUserClient != null) {
                Result<Integer> levelResult = internalUserClient.getMemberLevel(order.getUserId());
                if (levelResult != null && levelResult.getCode() == 200 && levelResult.getData() != null) {
                    memberLevel = levelResult.getData();
                }
            }
        } catch (Exception e) {
            log.warn("查询用户等级失败，使用默认等级: userId={}, error={}", order.getUserId(), e.getMessage());
        }
        double levelDiscount = MemberLevelConstants.getDiscount(memberLevel);
        if (levelDiscount < 1.0) {
            order.setTotalAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(levelDiscount))
                    .setScale(2, RoundingMode.HALF_UP));
        }

        // 校验 + 核销优惠券
        BigDecimal totalDiscount = BigDecimal.ZERO;
        boolean usedCoupon2 = false;

        if (order.getUserCouponId() != null) {
            totalDiscount = validateAndUseCoupon(order, order.getUserCouponId(), freight, null);
        }
        if (order.getUserCouponId2() != null) {
            totalDiscount = totalDiscount.add(
                validateAndUseCoupon(order, order.getUserCouponId2(), freight, usedCoupon2 ? null : order.getUserCouponId()));
            usedCoupon2 = true;
        }

        // 计算实付金额
        BigDecimal realPay = order.getTotalAmount()
                .add(order.getFreightAmount())
                .subtract(totalDiscount);
        if (realPay.compareTo(BigDecimal.ZERO) < 0) {
            realPay = BigDecimal.ZERO;
        }

        // 积分抵扣
        BigDecimal integralDeducted = BigDecimal.ZERO;
        if (order.getUseIntegral() != null && order.getUseIntegral() > 0) {
            BigDecimal maxIntegralDiscount = realPay.multiply(new BigDecimal("0.5"));
            integralDeducted = new BigDecimal(order.getUseIntegral())
                    .divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
            if (integralDeducted.compareTo(maxIntegralDiscount) > 0) {
                integralDeducted = maxIntegralDiscount;
            }
            int actualIntegralUse = integralDeducted.multiply(new BigDecimal("100")).intValue();
            if (internalUserClient != null) {
                Result<Void> deductResult = internalUserClient.deductIntegral(order.getUserId(),
                        Map.of("amount", actualIntegralUse, "description", "积分抵扣订单"));
                if (deductResult == null || deductResult.getCode() != 200) {
                    throw new BusinessException("积分扣减失败: " + (deductResult != null ? deductResult.getMessage() : "服务不可用"));
                }
            }
            realPay = realPay.subtract(integralDeducted);
            if (realPay.compareTo(BigDecimal.ZERO) < 0) realPay = BigDecimal.ZERO;
        }
        order.setIntegralAmount(integralDeducted);
        order.setPayAmount(realPay);
        order.setCouponAmount(totalDiscount);

        // 生成订单号
        String orderNo = generateOrderNo();
        LambdaQueryWrapper<Order> dupCheck = new LambdaQueryWrapper<>();
        dupCheck.eq(Order::getOrderNo, orderNo);
        int retry = 0;
        while (baseMapper.selectCount(dupCheck) > 0 && retry < 5) {
            orderNo = generateOrderNo();
            dupCheck = new LambdaQueryWrapper<>();
            dupCheck.eq(Order::getOrderNo, orderNo);
            retry++;
        }
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());

        baseMapper.insert(order);

        // 保存订单商品 + 扣减库存
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                orderItemMapper.insert(item);
                if (internalProductClient != null) {
                    Result<Void> stockResult = internalProductClient.deductStock(item.getProductId(),
                            Map.of("quantity", item.getQuantity()));
                    if (stockResult == null || stockResult.getCode() != 200) {
                        throw new BusinessException("商品 [" + item.getProductName() + "] 库存不足");
                    }
                }
            }
        }

        // 记录操作日志
        saveOrderLog(order.getId(), orderNo, "CREATE", null, "订单创建",
                getCurrentUserIdOrNull());

        log.info("创建订单成功: orderNo={}, userId={}, total={}, freight={}, discount={}, pay={}",
                order.getOrderNo(), order.getUserId(), order.getTotalAmount(),
                order.getFreightAmount(), order.getCouponAmount(), order.getPayAmount());

        // 发送订单创建消息到 RocketMQ
        try {
            orderMessageProducer.sendOrderCreatedMessage(order.getId(), order.getUserId());
        } catch (Exception e) {
            log.warn("发送订单创建消息失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }

        return order;
    }

    /**
     * 校验并核销单张优惠券
     */
    private BigDecimal validateAndUseCoupon(Order order, Long receiveId, BigDecimal freight, Long firstCouponId) {
        Result<Map<String, Object>> detailResult = internalCouponClient.getCouponDetail(receiveId, order.getUserId());
        Map<String, Object> detail = detailResult.getData();
        if (detail == null || detail.isEmpty()) {
            throw new BusinessException("优惠券不存在或不属于当前用户");
        }

        int status = ((Number) detail.get("status")).intValue();
        if (status != 0) {
            throw new BusinessException("优惠券已使用或已过期");
        }

        int couponType = ((Number) detail.get("coupon_type")).intValue();
        BigDecimal minAmount = toDecimal(detail.get("min_amount"));
        BigDecimal discountAmount = toDecimal(detail.get("discount_amount"));
        BigDecimal discountRate = toDecimal(detail.get("discount_rate"));
        Integer validDays = (Integer) detail.get("valid_days");
        LocalDateTime useStart = (LocalDateTime) detail.get("use_start_time");
        LocalDateTime useEnd = (LocalDateTime) detail.get("use_end_time");
        int applicableType = ((Number) detail.get("applicable_type")).intValue();
        String applicableIds = (String) detail.get("applicable_ids");
        int stackable = ((Number) detail.get("stackable")).intValue();

        // 有效期校验
        LocalDateTime now = LocalDateTime.now();
        if (useStart != null && useEnd != null) {
            if (now.isBefore(useStart) || now.isAfter(useEnd)) {
                throw new BusinessException("优惠券不在有效期内");
            }
        } else if (validDays != null && validDays > 0) {
            LocalDateTime receiveTime = (LocalDateTime) detail.get("receive_time");
            if (receiveTime != null && now.isAfter(receiveTime.plusDays(validDays))) {
                throw new BusinessException("优惠券已过期");
            }
        }

        // 叠加校验
        if (firstCouponId != null) {
            if (firstCouponId.equals(receiveId)) {
                throw new BusinessException("不可重复使用同一张优惠券");
            }
            Map<String, Object> firstDetail = internalCouponClient.getCouponDetail(firstCouponId, order.getUserId()).getData();
            int firstStackable = ((Number) firstDetail.get("stackable")).intValue();
            if (firstStackable == 0 || stackable == 0) {
                throw new BusinessException("该优惠券不可叠加使用");
            }
            int firstCouponType = ((Number) firstDetail.get("coupon_type")).intValue();
            if (firstCouponType == couponType) {
                throw new BusinessException("不可使用同类型优惠券");
            }
        }

        // 新人券校验
        if (couponType == 4) {
            int orderCount = couponReceiveMapper.countUserOrders(order.getUserId());
            if (orderCount > 0) {
                throw new BusinessException("新人券仅限首次下单使用");
            }
        }

        // 门槛校验
        BigDecimal applicableTotal;
        if (couponType == 3) {
            applicableTotal = freight;
        } else {
            applicableTotal = order.getTotalAmount();
        }

        if (applicableTotal.compareTo(minAmount) < 0) {
            throw new BusinessException("未达到优惠券使用门槛");
        }

        // 计算优惠金额
        BigDecimal discount;
        if (couponType == 2) {
            BigDecimal one = BigDecimal.ONE;
            discount = applicableTotal.multiply(one.subtract(discountRate)).setScale(2, RoundingMode.HALF_UP);
        } else if (couponType == 3) {
            BigDecimal freightDiscount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
            discount = freightDiscount.min(freight);
        } else {
            discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        }

        if (couponType != 3 && discount.compareTo(order.getTotalAmount()) > 0) {
            discount = order.getTotalAmount();
        }

        // 核销 via Feign
        Map<String, Object> useReq = new HashMap<>();
        useReq.put("id", receiveId);
        useReq.put("userId", order.getUserId());
        useReq.put("orderId", order.getId());
        useReq.put("orderNo", order.getOrderNo());
        Result<Void> useResult = internalCouponClient.useCoupon(useReq);
        if (useResult.getCode() != 200) {
            throw new BusinessException("优惠券不可用");
        }

        log.info("优惠券核销成功: receiveId={}, type={}, discount={}", receiveId, couponType, discount);
        return discount;
    }

    private BigDecimal calculateFreight(BigDecimal totalAmount) {
        if (totalAmount == null) return DEFAULT_FREIGHT;
        if (totalAmount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }
        return DEFAULT_FREIGHT;
    }

    // getLevelDiscount 已移至 MemberLevelConstants.getDiscount()

    private BigDecimal toDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, String reason) {
        Order entity = getEntityById(id);
        if (entity.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BusinessException("只能取消待付款的订单");
        }

        String oldStatus = OrderStatus.fromCode(entity.getStatus()).getDesc();
        cancelOrderAndRestoreStock(entity, reason);

        saveOrderLog(id, entity.getOrderNo(), oldStatus, "CANCEL", reason, getCurrentUserIdOrNull());

        // 发送订单取消消息到 RocketMQ
        try {
            orderMessageProducer.sendOrderCancelledMessage(entity.getId(), entity.getUserId());
        } catch (Exception e) {
            log.warn("发送订单取消消息失败: orderNo={}, error={}", entity.getOrderNo(), e.getMessage());
        }
    }

    /** 取消订单 + 恢复库存（内部方法，被 cancel 和 autoCancel 共用） */
    private void cancelOrderAndRestoreStock(Order entity, String reason) {
        entity.setStatus(OrderStatus.CANCELLED.getCode());
        entity.setCancelTime(LocalDateTime.now());
        entity.setCancelReason(reason);
        baseMapper.updateById(entity);

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(entity.getId());
        for (OrderItem item : items) {
            if (internalProductClient != null) {
                try {
                    internalProductClient.restoreStock(item.getProductId(),
                            Map.of("quantity", item.getQuantity()));
                } catch (Exception e) {
                    log.warn("恢复库存失败: productId={}, quantity={}, error={}",
                            item.getProductId(), item.getQuantity(), e.getMessage());
                }
            }
        }

        // 如果使用了优惠券，退还优惠券 via Feign
        if (entity.getUserCouponId() != null) {
            Map<String, Object> restoreReq = new HashMap<>();
            restoreReq.put("id", entity.getUserCouponId());
            restoreReq.put("userId", entity.getUserId());
            internalCouponClient.restoreCoupon(restoreReq);
        }
        if (entity.getUserCouponId2() != null) {
            Map<String, Object> restoreReq2 = new HashMap<>();
            restoreReq2.put("id", entity.getUserCouponId2());
            restoreReq2.put("userId", entity.getUserId());
            internalCouponClient.restoreCoupon(restoreReq2);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long id, Integer payType) {
        Order entity = getEntityById(id);
        if (entity.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BusinessException("订单状态不正确");
        }

        String oldStatus = OrderStatus.fromCode(entity.getStatus()).getDesc();

        // 余额支付
        if (payType != null && payType == PayMethod.BALANCE.getCode()) {
            BigDecimal payAmount = entity.getPayAmount();
            if (internalUserClient != null) {
                Result<Void> balanceResult = internalUserClient.deductBalance(entity.getUserId(),
                        Map.of("amount", payAmount, "description", "余额支付订单"));
                if (balanceResult == null || balanceResult.getCode() != 200) {
                    throw new BusinessException("余额不足");
                }
            }
        }

        entity.setStatus(OrderStatus.PENDING_SHIP.getCode());
        entity.setPayType(payType);
        entity.setPayTime(LocalDateTime.now());
        baseMapper.updateById(entity);

        String payDesc = payType != null ? PayMethod.fromCode(payType).getDesc() : "未知方式";
        saveOrderLog(id, entity.getOrderNo(), oldStatus, "PAY",
                "支付成功，支付方式: " + payDesc, getCurrentUserIdOrNull());
        log.info("支付订单成功: id={}, payType={}", id, payType);

        // 发送订单支付成功消息到 RocketMQ
        try {
            orderMessageProducer.sendOrderPaidMessage(entity.getId(), entity.getUserId());
        } catch (Exception e) {
            log.warn("发送订单支付消息失败: orderNo={}, error={}", entity.getOrderNo(), e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long id) {
        Order entity = getEntityById(id);
        if (entity.getStatus() != OrderStatus.PENDING_SHIP.getCode()) {
            throw new BusinessException("订单状态不正确");
        }

        String oldStatus = OrderStatus.fromCode(entity.getStatus()).getDesc();
        entity.setStatus(OrderStatus.PENDING_RECEIVE.getCode());
        entity.setDeliveryTime(LocalDateTime.now());
        baseMapper.updateById(entity);

        saveOrderLog(id, entity.getOrderNo(), oldStatus, "SHIP", "订单已发货", getCurrentUserIdOrNull());
        log.info("订单发货成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long id) {
        Order entity = getEntityById(id);
        if (entity.getStatus() != OrderStatus.PENDING_RECEIVE.getCode()) {
            throw new BusinessException("订单状态不正确");
        }

        String oldStatus = OrderStatus.fromCode(entity.getStatus()).getDesc();
        entity.setStatus(OrderStatus.COMPLETED.getCode());
        entity.setReceiveTime(LocalDateTime.now());
        baseMapper.updateById(entity);

        // 奖励积分
        int earnedIntegral = entity.getPayAmount().multiply(new BigDecimal("10")).intValue();
        if (earnedIntegral > 0 && internalUserClient != null) {
            try {
                internalUserClient.addIntegral(entity.getUserId(),
                        Map.of("amount", earnedIntegral, "description", "订单完成奖励"));
            } catch (Exception e) {
                log.warn("奖励积分失败: userId={}, amount={}, error={}", entity.getUserId(), earnedIntegral, e.getMessage());
            }
        }

        // 增加成长值 + 升级会员
        int growthToAdd = entity.getPayAmount().intValue();
        if (growthToAdd > 0 && internalUserClient != null) {
            try {
                internalUserClient.addGrowthValue(entity.getUserId(),
                        Map.of("amount", growthToAdd));
            } catch (Exception e) {
                log.warn("增加成长值失败: userId={}, amount={}, error={}", entity.getUserId(), growthToAdd, e.getMessage());
            }
        }

        // 增加商品销量
        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        for (OrderItem item : items) {
            if (internalProductClient != null) {
                try {
                    internalProductClient.addSales(item.getProductId(),
                            Map.of("quantity", item.getQuantity()));
                } catch (Exception e) {
                    log.warn("增加销量失败: productId={}, quantity={}, error={}",
                            item.getProductId(), item.getQuantity(), e.getMessage());
                }
            }
        }

        saveOrderLog(id, entity.getOrderNo(), oldStatus, "CONFIRM",
                "确认收货，获得" + earnedIntegral + "积分", getCurrentUserIdOrNull());
        log.info("确认收货成功: id={}, earnedIntegral={}, growth={}", id, earnedIntegral, growthToAdd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id, String reason) {
        Order entity = getEntityById(id);
        if (entity.getStatus() != OrderStatus.PENDING_SHIP.getCode()
                && entity.getStatus() != OrderStatus.PENDING_RECEIVE.getCode()) {
            throw new BusinessException("仅待发货或待收货的订单可申请退款");
        }

        String oldStatus = OrderStatus.fromCode(entity.getStatus()).getDesc();
        entity.setStatus(OrderStatus.REFUNDING.getCode());
        baseMapper.updateById(entity);

        // 创建退款记录（余额/支付宝/微信统一走审批流程）
        String paymentNo = entity.getPayType() != null && entity.getPayType() == PayMethod.BALANCE.getCode()
                ? null : findPaymentNoByOrderId(id);
        String refundNo = "RF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setRefundNo(refundNo);
        refundRecord.setPaymentNo(paymentNo);
        refundRecord.setOrderNo(entity.getOrderNo());
        refundRecord.setUserId(entity.getUserId());
        refundRecord.setRefundAmount(entity.getPayAmount());
        refundRecord.setRefundReason(reason);
        refundRecord.setStatus(0);
        refundRecord.setPayMethod(entity.getPayType());
        refundRecordMapper.insert(refundRecord);
        log.info("退款记录已创建: refundNo={}, orderNo={}, payMethod={}", refundNo, entity.getOrderNo(), entity.getPayType());

        saveOrderLog(id, entity.getOrderNo(), oldStatus, "REFUND_APPLY",
                "申请退款: " + reason, getCurrentUserIdOrNull());

        log.info("退款申请: id={}, reason={}", id, reason);

        // 通知管理员有新的退款申请
        try {
            if (notificationClient != null) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("type", 1);
                notif.put("title", "新的退款申请");
                notif.put("content", "订单 " + entity.getOrderNo() + " 申请退款 ¥" + entity.getPayAmount() + "，原因: " + reason);
                notif.put("refId", id);
                notif.put("refType", "refund");
                notificationClient.createNotification(notif);
            }
        } catch (Exception e) {
            log.warn("发送退款申请通知失败: {}", e.getMessage());
        }
    }

    /** 通过订单ID查找支付单号 */
    private String findPaymentNoByOrderId(Long orderId) {
        try {
            if (internalPaymentClient != null) {
                Result<String> result = internalPaymentClient.getPaymentNoByOrderId(orderId);
                if (result != null && result.getCode() == 200) {
                    return result.getData();
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("查询支付单号失败: orderId={}, error={}", orderId, e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Order entity = getEntityById(id);
        baseMapper.deleteById(id);

        LambdaUpdateWrapper<OrderItem> itemWrapper = new LambdaUpdateWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, id);
        itemWrapper.set(OrderItem::getDeleted, 1);
        orderItemMapper.update(null, itemWrapper);

        saveOrderLog(id, entity.getOrderNo(),
                OrderStatus.fromCode(entity.getStatus()).getDesc(), "DELETE", "订单删除", getCurrentUserIdOrNull());
        log.info("删除订单成功: id={}", id);
    }

    @Override
    public Order getById(Long id) {
        return getEntityById(id);
    }

    @Override
    public PageResult<Order> page(Integer pageNum, Integer pageSize, Long userId, Integer status, String orderNo) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);

        if (isStoreUser()) {
            Long currentUserId = getCurrentUserId();
            List<Long> storeOrderIds = baseMapper.selectOrderIdsByProductCreator(currentUserId);
            if (storeOrderIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L, 0L);
            }
            wrapper.in(Order::getId, storeOrderIds);
        }

        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    public Order getDetail(Long id) {
        return getEntityById(id);
    }

    @Override
    public List<Order> listByUserId(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.eq(Order::getDeleted, 0);
        wrapper.orderByDesc(Order::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    /** 查询订单操作日志 */
    public List<OrderLog> getLogs(Long orderId) {
        LambdaQueryWrapper<OrderLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderLog::getOrderId, orderId)
               .orderByAsc(OrderLog::getCreateTime);
        return orderLogMapper.selectList(wrapper);
    }

    // ─── 内部工具方法 ───

    private Order getEntityById(Long id) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getId, id);
        wrapper.eq(Order::getDeleted, 0);
        Order entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("订单不存在");
        }
        return entity;
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%06d", new Random().nextInt(1000000));
        return "SN" + dateStr + randomStr;
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isStoreUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STORE"));
    }

    /** 保存订单操作日志 */
    private void saveOrderLog(Long orderId, String orderNo, String oldStatus, String action, String remark, Long operatorId) {
        try {
            OrderLog logEntry = new OrderLog();
            logEntry.setOrderId(orderId);
            logEntry.setOrderNo(orderNo);
            logEntry.setOldStatus(oldStatus);
            logEntry.setAction(action);
            logEntry.setRemark(remark);
            logEntry.setOperatorId(operatorId);
            orderLogMapper.insert(logEntry);
        } catch (Exception e) {
            // 日志写入失败不影响主流程
            log.warn("保存订单日志异常: orderId={}, action={}, error={}", orderId, action, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, Integer status) {
        Order entity = getEntityById(orderId);
        entity.setStatus(status);
        baseMapper.updateById(entity);
        log.info("更新订单状态成功: orderId={}, status={}", orderId, status);
    }

    @Override
    public Map<String, Object> getOrderBasicInfo(Long orderId) {
        Order entity = getEntityById(orderId);
        Map<String, Object> info = new HashMap<>();
        info.put("id", entity.getId());
        info.put("order_no", entity.getOrderNo());
        info.put("status", entity.getStatus());
        info.put("pay_amount", entity.getPayAmount());
        info.put("user_id", entity.getUserId());
        return info;
    }

    @Override
    public Map<String, Object> getOrderInfoByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getDeleted, 0);
        Order entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("订单不存在: " + orderNo);
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", entity.getId());
        info.put("order_no", entity.getOrderNo());
        info.put("status", entity.getStatus());
        info.put("pay_amount", entity.getPayAmount());
        info.put("user_id", entity.getUserId());
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusByOrderNo(String orderNo, Integer status) {
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getDeleted, 0);
        wrapper.set(Order::getStatus, status);
        baseMapper.update(null, wrapper);
        log.info("根据订单号更新状态成功: orderNo={}, status={}", orderNo, status);
    }

    @Override
    public List<Map<String, Object>> getOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        wrapper.eq(OrderItem::getDeleted, 0);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("product_id", item.getProductId());
            map.put("quantity", item.getQuantity());
            return map;
        }).toList();
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 今日销售额
        BigDecimal todaySales = baseMapper.sumTodaySales();
        stats.put("today_sales", todaySales != null ? todaySales : BigDecimal.ZERO);

        // 今日订单数
        Long todayOrders = baseMapper.countTodayOrders();
        stats.put("today_orders", todayOrders != null ? todayOrders : 0L);

        // 待处理订单数
        Long pendingOrders = baseMapper.countPendingOrders();
        stats.put("pending_orders", pendingOrders != null ? pendingOrders : 0L);

        // 昨日销售额
        BigDecimal yesterdaySales = baseMapper.sumYesterdaySales();
        stats.put("yesterday_sales", yesterdaySales != null ? yesterdaySales : BigDecimal.ZERO);

        // 昨日订单数
        Long yesterdayOrders = baseMapper.countYesterdayOrders();
        stats.put("yesterday_orders", yesterdayOrders != null ? yesterdayOrders : 0L);

        // 7天销售趋势
        List<Map<String, Object>> salesTrend = baseMapper.salesTrend7Days();
        stats.put("sales_trend_7days", salesTrend != null ? salesTrend : Collections.emptyList());

        return stats;
    }
}
