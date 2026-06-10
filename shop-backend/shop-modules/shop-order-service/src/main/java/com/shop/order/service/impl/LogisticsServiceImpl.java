package com.shop.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.order.controller.request.LogisticsCreateRequest;
import com.shop.order.controller.request.LogisticsTraceRequest;
import com.shop.order.entity.Logistics;
import com.shop.order.entity.LogisticsTrace;
import com.shop.order.mapper.LogisticsMapper;
import com.shop.order.mapper.LogisticsTraceMapper;
import com.shop.order.service.GeocodingService;
import com.shop.order.service.KdniaoApiService;
import com.shop.order.service.KdniaoApiService.LogisticsQueryResult;
import com.shop.order.service.KdniaoApiService.TraceItem;
import com.shop.order.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流服务实现
 *
 * @author shop
 * @since 2026-06-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl extends ServiceImpl<LogisticsMapper, Logistics> implements LogisticsService {

    private final LogisticsTraceMapper traceMapper;
    private final KdniaoApiService kdniaoApiService;
    private final GeocodingService geocodingService;

    /** 缓存过期时间（2小时） */
    private static final Duration CACHE_EXPIRE = Duration.ofHours(2);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Logistics createLogistics(LogisticsCreateRequest request) {
        // 检查订单是否已有物流
        LambdaQueryWrapper<Logistics> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Logistics::getOrderId, request.getOrderId())
                    .eq(Logistics::getDeleted, 0);
        if (baseMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("该订单已存在物流信息");
        }

        Logistics logistics = new Logistics();
        logistics.setOrderId(request.getOrderId());
        logistics.setLogisticsNo(request.getLogisticsNo());
        logistics.setCompany(request.getCompany());
        logistics.setStatus(0); // 已发货
        logistics.setSenderName(request.getSenderName());
        logistics.setSenderPhone(request.getSenderPhone());
        logistics.setSenderAddress(request.getSenderAddress());
        logistics.setReceiverName(request.getReceiverName());
        logistics.setReceiverPhone(request.getReceiverPhone());
        logistics.setReceiverAddress(request.getReceiverAddress());

        // 获取发件人坐标：优先使用前端传入的精确坐标，否则自动地理编码
        if (request.getSenderLongitude() != null && request.getSenderLatitude() != null) {
            logistics.setSenderLongitude(request.getSenderLongitude());
            logistics.setSenderLatitude(request.getSenderLatitude());
            log.info("使用前端传入的发件人坐标: lat={}, lng={}", request.getSenderLatitude(), request.getSenderLongitude());
        } else if (request.getSenderAddress() != null && !request.getSenderAddress().isBlank()) {
            GeocodingService.Coordinates senderCoord = geocodingService.addressToCoordinates(request.getSenderAddress());
            if (senderCoord != null) {
                logistics.setSenderLatitude(senderCoord.getLatitude());
                logistics.setSenderLongitude(senderCoord.getLongitude());
                log.info("发件人地址坐标(自动编码): lat={}, lng={}", senderCoord.getLatitude(), senderCoord.getLongitude());
            } else {
                log.warn("发件人地址地理编码失败，坐标为空: address={}", request.getSenderAddress());
            }
        }

        // 获取收件人坐标：优先使用前端传入的精确坐标，否则自动地理编码
        if (request.getReceiverLongitude() != null && request.getReceiverLatitude() != null) {
            logistics.setReceiverLongitude(request.getReceiverLongitude());
            logistics.setReceiverLatitude(request.getReceiverLatitude());
            log.info("使用前端传入的收件人坐标: lat={}, lng={}", request.getReceiverLatitude(), request.getReceiverLongitude());
        } else if (request.getReceiverAddress() != null && !request.getReceiverAddress().isBlank()) {
            GeocodingService.Coordinates receiverCoord = geocodingService.addressToCoordinates(request.getReceiverAddress());
            if (receiverCoord != null) {
                logistics.setReceiverLatitude(receiverCoord.getLatitude());
                logistics.setReceiverLongitude(receiverCoord.getLongitude());
                log.info("收件人地址坐标(自动编码): lat={}, lng={}", receiverCoord.getLatitude(), receiverCoord.getLongitude());
            } else {
                log.warn("收件人地址地理编码失败，坐标为空: address={}", request.getReceiverAddress());
            }
        }

        baseMapper.insert(logistics);

        // 添加初始轨迹（使用已获取的发件人坐标，不重复调用 API）
        LogisticsTrace initTrace = new LogisticsTrace();
        initTrace.setLogisticsId(logistics.getId());
        initTrace.setTraceTime(logistics.getCreateTime());
        initTrace.setContent("快件已发货");
        initTrace.setLocation(request.getSenderAddress());
        if (logistics.getSenderLatitude() != null && logistics.getSenderLongitude() != null) {
            initTrace.setLatitude(logistics.getSenderLatitude());
            initTrace.setLongitude(logistics.getSenderLongitude());
        }

        traceMapper.insert(initTrace);

        log.info("创建物流信息成功: orderId={}, logisticsNo={}", request.getOrderId(), request.getLogisticsNo());
        return logistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTrace(Long logisticsId, LogisticsTraceRequest request) {
        Logistics logistics = baseMapper.selectById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流信息不存在");
        }

        LogisticsTrace trace = new LogisticsTrace();
        trace.setLogisticsId(logisticsId);
        trace.setTraceTime(request.getTraceTime());
        trace.setContent(request.getContent());
        trace.setLocation(request.getLocation());

        // 地理编码
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            GeocodingService.Coordinates coordinates = geocodingService.addressToCoordinates(request.getLocation());
            if (coordinates != null) {
                trace.setLatitude(coordinates.getLatitude());
                trace.setLongitude(coordinates.getLongitude());
            }
        }

        traceMapper.insert(trace);

        // 更新物流状态
        if (request.getContent().contains("派送")) {
            logistics.setStatus(2); // 派送中
        } else if (logistics.getStatus() == 0) {
            logistics.setStatus(1); // 运输中
        }
        baseMapper.updateById(logistics);

        log.info("添加物流轨迹成功: logisticsId={}", logisticsId);
    }

    @Override
    public Logistics getLogisticsByOrderId(Long orderId) {
        LambdaQueryWrapper<Logistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Logistics::getOrderId, orderId)
               .eq(Logistics::getDeleted, 0);
        Logistics logistics = baseMapper.selectOne(wrapper);
        if (logistics == null) {
            return null;
        }

        // 查询轨迹
        List<LogisticsTrace> traces = getTraces(logistics.getId());
        logistics.setTraces(traces);
        return logistics;
    }

    /**
     * 查询物流信息（带API更新）
     */
    public Logistics getLogisticsWithApiUpdate(Long orderId) {
        Logistics logistics = getLogisticsByOrderId(orderId);
        if (logistics == null) {
            return null;
        }

        // 判断是否需要调用API
        if (shouldQueryApi(logistics)) {
            try {
                refreshLogisticsFromApi(logistics);
            } catch (Exception e) {
                log.error("刷新物流信息失败: orderId={}, error={}", orderId, e.getMessage(), e);
                // 失败时返回缓存数据
            }
        }

        return logistics;
    }

    /**
     * 判断是否需要调用API
     */
    private boolean shouldQueryApi(Logistics logistics) {
        // 首次查询 或 缓存过期 或 物流未签收
        return logistics.getLastQueryTime() == null
            || logistics.getLastQueryTime().plus(CACHE_EXPIRE).isBefore(LocalDateTime.now())
            || logistics.getStatus() < 3; // 未签收
    }

    /**
     * 从API刷新物流信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void refreshLogisticsFromApi(Logistics logistics) {
        // 快递100 phone 参数：多数快递使用收件人手机号，顺丰使用发件人手机号后四位
        String phone;
        if ("shunfeng".equals(logistics.getCompany())) {
            phone = logistics.getSenderPhone();
            if (phone == null || phone.isBlank()) {
                phone = logistics.getReceiverPhone();
            }
        } else {
            phone = logistics.getReceiverPhone();
            if (phone == null || phone.isBlank()) {
                phone = logistics.getSenderPhone();
            }
        }

        // 提取城市信息用于快递100查询
        String fromCity = KdniaoApiService.extractCity(logistics.getSenderAddress());
        String toCity = KdniaoApiService.extractCity(logistics.getReceiverAddress());

        LogisticsQueryResult result = kdniaoApiService.queryLogistics(
                logistics.getLogisticsNo(),
                logistics.getCompany(),
                phone,
                fromCity,
                toCity
        );

        if (!result.isSuccess()) {
            log.warn("快递100查询失败: logisticsNo={}, message={}", logistics.getLogisticsNo(), result.getMessage());
            return;
        }

        // 保存新轨迹
        List<TraceItem> traces = result.getTraces();
        if (traces != null && !traces.isEmpty()) {
            // API 返回了有效数据，才删除旧轨迹
            LambdaQueryWrapper<LogisticsTrace> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(LogisticsTrace::getLogisticsId, logistics.getId());
            traceMapper.delete(deleteWrapper);

            for (TraceItem item : traces) {
                LogisticsTrace trace = new LogisticsTrace();
                trace.setLogisticsId(logistics.getId());
                trace.setTraceTime(item.getTraceTime());
                trace.setContent(item.getContent());
                trace.setLocation(item.getLocation());

                // 地理编码
                if (item.getLocation() != null && !item.getLocation().isBlank()) {
                    GeocodingService.Coordinates coordinates = geocodingService.addressToCoordinates(item.getLocation());
                    if (coordinates != null) {
                        trace.setLatitude(coordinates.getLatitude());
                        trace.setLongitude(coordinates.getLongitude());
                    }
                }

                traceMapper.insert(trace);
            }
        } else {
            log.warn("快递100返回空轨迹: logisticsNo={}", logistics.getLogisticsNo());
        }

        // 更新物流状态和查询时间
        if (result.getStatus() != null) {
            logistics.setStatus(result.getStatus());
        }
        logistics.setLastQueryTime(LocalDateTime.now());
        baseMapper.updateById(logistics);

        // 刷新轨迹数据
        List<LogisticsTrace> updatedTraces = getTraces(logistics.getId());
        logistics.setTraces(updatedTraces);

        log.info("刷新物流信息成功: logisticsNo={}, status={}", logistics.getLogisticsNo(), logistics.getStatus());
    }

    @Override
    public Logistics getLogisticsDetail(Long logisticsId) {
        Logistics logistics = baseMapper.selectById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流信息不存在");
        }

        List<LogisticsTrace> traces = getTraces(logisticsId);
        logistics.setTraces(traces);
        return logistics;
    }

    @Override
    public List<LogisticsTrace> getTraces(Long logisticsId) {
        LambdaQueryWrapper<LogisticsTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsTrace::getLogisticsId, logisticsId)
               .orderByAsc(LogisticsTrace::getTraceTime);
        return traceMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long logisticsId, Integer status) {
        Logistics logistics = baseMapper.selectById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流信息不存在");
        }

        logistics.setStatus(status);
        baseMapper.updateById(logistics);
        log.info("更新物流状态成功: logisticsId={}, status={}", logisticsId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signLogistics(Long logisticsId) {
        Logistics logistics = baseMapper.selectById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流信息不存在");
        }

        logistics.setStatus(3); // 已签收
        baseMapper.updateById(logistics);

        // 添加签收轨迹
        LogisticsTrace trace = new LogisticsTrace();
        trace.setLogisticsId(logisticsId);
        trace.setTraceTime(LocalDateTime.now());
        trace.setContent("快件已签收");
        trace.setLocation(logistics.getReceiverAddress());

        // 地理编码
        GeocodingService.Coordinates coordinates = geocodingService.addressToCoordinates(logistics.getReceiverAddress());
        if (coordinates != null) {
            trace.setLatitude(coordinates.getLatitude());
            trace.setLongitude(coordinates.getLongitude());
        }

        traceMapper.insert(trace);

        log.info("物流签收成功: logisticsId={}", logisticsId);
    }

    /**
     * 手动刷新物流
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshLogistics(Long logisticsId) {
        Logistics logistics = baseMapper.selectById(logisticsId);
        if (logistics == null) {
            throw new BusinessException("物流信息不存在");
        }

        refreshLogisticsFromApi(logistics);
    }
}
