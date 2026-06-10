package com.shop.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.common.security.jwt.JwtUtil;
import com.shop.common.web.PageResult;
import com.shop.live.controller.request.LiveRoomCreateRequest;
import com.shop.live.controller.request.LiveRoomUpdateRequest;
import com.shop.live.controller.response.LiveRoomResponse;
import com.shop.live.entity.Anchor;
import com.shop.live.entity.LiveRoom;
import com.shop.live.mapper.LiveRoomMapper;
import com.shop.live.service.AnchorService;
import com.shop.live.service.LiveRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {

    private final AnchorService anchorService;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    @Value("${srs.push-base-url:rtmp://localhost:1935/live}")
    private String srsPushBaseUrl;

    @Value("${srs.pull-base-url:http://localhost:8085/live}")
    private String srsPullBaseUrl;

    @Value("${srs.push-token-expiration:7200}")
    private long pushTokenExpiration;

    /** 推流白名单 Redis Key 前缀 */
    private static final String PUSH_WHITELIST_KEY = "live:push:whitelist:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoomResponse create(LiveRoomCreateRequest request) {
        // Verify anchor exists and is approved
        Anchor anchor = anchorService.getBaseMapper().selectById(request.getAnchorId());
        if (anchor == null || anchor.getStatus() != 1) {
            throw new BusinessException("主播未认证或已被禁用");
        }

        LiveRoom room = new LiveRoom();
        BeanUtils.copyProperties(request, room);
        room.setStatus(0);
        room.setOnlineCount(0);
        room.setTotalViewCount(0);
        room.setPeakOnlineCount(0);
        room.setLikeCount(0);
        room.setGiftCount(0);
        room.setDuration(0L);

        baseMapper.insert(room);
        log.info("创建直播间成功: id={}, title={}", room.getId(), room.getTitle());
        return convertToResponse(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoomResponse update(Long id, LiveRoomUpdateRequest request) {
        LiveRoom room = getEntityById(id);
        if (room.getStatus() == 1) {
            throw new BusinessException("直播进行中，无法修改信息");
        }

        BeanUtils.copyProperties(request, room);
        baseMapper.updateById(room);
        log.info("更新直播间成功: id={}", id);
        return convertToResponse(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        LiveRoom room = getEntityById(id);
        if (room.getStatus() == 1) {
            throw new BusinessException("直播进行中，无法删除");
        }
        baseMapper.deleteById(id);
        log.info("删除直播间成功: id={}", id);
    }

    @Override
    public LiveRoomResponse getById(Long id) {
        LiveRoom room = getEntityById(id);
        LiveRoomResponse response = convertToResponse(room);

        // Attach anchor info
        Anchor anchor = anchorService.getBaseMapper().selectById(room.getAnchorId());
        if (anchor != null) {
            response.setAnchorNickname(anchor.getNickname());
            response.setAnchorAvatar(anchor.getAvatar());
        }
        return response;
    }

    @Override
    public PageResult<LiveRoomResponse> page(Integer pageNum, Integer pageSize, Integer type, Integer status) {
        Page<LiveRoom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LiveRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveRoom::getDeleted, 0);
        if (type != null) {
            wrapper.eq(LiveRoom::getType, type);
        }
        if (status != null) {
            wrapper.eq(LiveRoom::getStatus, status);
        }
        wrapper.orderByDesc(LiveRoom::getCreateTime);

        Page<LiveRoom> result = baseMapper.selectPage(page, wrapper);
        List<LiveRoomResponse> records = result.getRecords().stream()
                .map(room -> {
                    LiveRoomResponse r = convertToResponse(room);
                    Anchor anchor = anchorService.getBaseMapper().selectById(room.getAnchorId());
                    if (anchor != null) {
                        r.setAnchorNickname(anchor.getNickname());
                        r.setAnchorAvatar(anchor.getAvatar());
                    }
                    return r;
                })
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    public List<LiveRoomResponse> listLiving() {
        LambdaQueryWrapper<LiveRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveRoom::getDeleted, 0);
        wrapper.eq(LiveRoom::getStatus, 1);
        wrapper.orderByDesc(LiveRoom::getOnlineCount);

        return baseMapper.selectList(wrapper).stream()
                .map(room -> {
                    LiveRoomResponse r = convertToResponse(room);
                    Anchor anchor = anchorService.getBaseMapper().selectById(room.getAnchorId());
                    if (anchor != null) {
                        r.setAnchorNickname(anchor.getNickname());
                        r.setAnchorAvatar(anchor.getAvatar());
                    }
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoomResponse startLive(Long id) {
        LiveRoom room = getEntityById(id);
        if (room.getStatus() == 1) {
            throw new BusinessException("直播已在进行中");
        }

        // 生成推流 Token（有效期 2 小时）
        String pushToken = jwtUtil.generateToken(room.getAnchorId());

        // 生成推流地址（含 Token）
        String pushUrl = srsPushBaseUrl + "/" + room.getId() + "?token=" + pushToken;

        // 生成拉流地址（含 Token，用于 SRS on_play 鉴权）
        String pullUrlFlv = srsPullBaseUrl + "/" + room.getId() + ".flv?token=" + pushToken;
        String pullUrlHls = srsPullBaseUrl + "/" + room.getId() + ".m3u8?token=" + pushToken;

        // 写入 Redis 白名单（用于 SRS 回调验证）
        String whitelistKey = PUSH_WHITELIST_KEY + room.getId();
        redisUtil.set(whitelistKey, pushToken, pushTokenExpiration, TimeUnit.SECONDS);

        // 更新房间状态为"待推流"（等待 SRS on_publish 回调确认）
        room.setStatus(4); // 待推流
        room.setPushUrl(pushUrl);
        room.setPullUrl(pullUrlFlv); // 默认使用 HTTP-FLV
        baseMapper.updateById(room);

        // Update anchor live count
        Anchor anchor = anchorService.getBaseMapper().selectById(room.getAnchorId());
        if (anchor != null) {
            anchor.setTotalLiveCount(anchor.getTotalLiveCount() + 1);
            anchorService.getBaseMapper().updateById(anchor);
        }

        log.info("生成推流地址: id={}, pushUrl={}", id, pushUrl);
        return convertToResponse(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoomResponse endLive(Long id) {
        LiveRoom room = getEntityById(id);
        if (room.getStatus() != 1 && room.getStatus() != 4) {
            throw new BusinessException("当前不在直播中");
        }

        room.setStatus(2);
        room.setEndTime(LocalDateTime.now());
        if (room.getActualStartTime() != null) {
            room.setDuration(Duration.between(room.getActualStartTime(), room.getEndTime()).getSeconds());
        }
        baseMapper.updateById(room);

        // 清理 Redis 白名单和状态
        redisUtil.delete(PUSH_WHITELIST_KEY + id);

        // Update anchor total duration
        Anchor anchor = anchorService.getBaseMapper().selectById(room.getAnchorId());
        if (anchor != null) {
            anchor.setTotalDuration(anchor.getTotalDuration() + room.getDuration());
            anchorService.getBaseMapper().updateById(anchor);
        }

        log.info("结束直播: id={}, duration={}s", id, room.getDuration());
        return convertToResponse(room);
    }

    private LiveRoom getEntityById(Long id) {
        LambdaQueryWrapper<LiveRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveRoom::getId, id);
        wrapper.eq(LiveRoom::getDeleted, 0);

        LiveRoom room = baseMapper.selectOne(wrapper);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        return room;
    }

    private LiveRoomResponse convertToResponse(LiveRoom room) {
        LiveRoomResponse response = new LiveRoomResponse();
        BeanUtils.copyProperties(room, response);
        return response;
    }
}
