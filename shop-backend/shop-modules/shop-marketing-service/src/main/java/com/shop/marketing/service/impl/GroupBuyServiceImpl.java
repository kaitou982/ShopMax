package com.shop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.GroupBuyActivityCreateRequest;
import com.shop.marketing.controller.response.GroupBuyActivityResponse;
import com.shop.marketing.controller.response.GroupBuyGroupResponse;
import com.shop.marketing.controller.response.GroupBuyMemberResponse;
import com.shop.marketing.entity.GroupBuyActivity;
import com.shop.marketing.entity.GroupBuyGroup;
import com.shop.marketing.entity.GroupBuyMember;
import com.shop.marketing.mapper.GroupBuyActivityMapper;
import com.shop.marketing.mapper.GroupBuyGroupMapper;
import com.shop.marketing.mapper.GroupBuyMemberMapper;
import com.shop.marketing.service.GroupBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyServiceImpl extends ServiceImpl<GroupBuyActivityMapper, GroupBuyActivity> implements GroupBuyService {

    private final GroupBuyGroupMapper groupMapper;
    private final GroupBuyMemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupBuyActivityResponse createActivity(GroupBuyActivityCreateRequest request) {
        GroupBuyActivity activity = new GroupBuyActivity();
        BeanUtils.copyProperties(request, activity);

        baseMapper.insert(activity);
        log.info("创建拼团活动成功: id={}, name={}", activity.getId(), activity.getName());
        return convertActivityToResponse(activity);
    }

    @Override
    public PageResult<GroupBuyActivityResponse> pageActivities(Integer pageNum, Integer pageSize) {
        Page<GroupBuyActivity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupBuyActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivity::getDeleted, 0);
        wrapper.orderByDesc(GroupBuyActivity::getCreateTime);

        Page<GroupBuyActivity> result = baseMapper.selectPage(page, wrapper);
        List<GroupBuyActivityResponse> records = result.getRecords().stream()
                .map(this::convertActivityToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    public List<GroupBuyActivityResponse> listActive() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<GroupBuyActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivity::getDeleted, 0);
        wrapper.eq(GroupBuyActivity::getStatus, 1);
        wrapper.le(GroupBuyActivity::getStartTime, now);
        wrapper.ge(GroupBuyActivity::getEndTime, now);
        wrapper.orderByDesc(GroupBuyActivity::getCreateTime);

        return baseMapper.selectList(wrapper).stream()
                .map(this::convertActivityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupBuyGroupResponse startGroup(Long activityId, Long userId) {
        GroupBuyActivity activity = getActivityById(activityId);
        validateActivityActive(activity);

        // Create group
        GroupBuyGroup group = new GroupBuyGroup();
        group.setActivityId(activityId);
        group.setLeaderId(userId);
        group.setCurrentCount(1);
        group.setRequiredCount(activity.getRequiredCount());
        group.setStatus(0);
        group.setExpireTime(LocalDateTime.now().plusHours(activity.getExpireHours()));

        groupMapper.insert(group);

        // Add leader as member
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setIsLeader(1);
        member.setJoinTime(LocalDateTime.now());
        memberMapper.insert(member);

        log.info("发起拼团成功: groupId={}, userId={}", group.getId(), userId);
        return convertGroupToResponse(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupBuyGroupResponse joinGroup(Long groupId, Long userId) {
        GroupBuyGroup group = getGroupById(groupId);

        if (group.getStatus() != 0) {
            throw new BusinessException("该拼团已结束");
        }
        if (LocalDateTime.now().isAfter(group.getExpireTime())) {
            group.setStatus(2);
            group.setCompleteTime(LocalDateTime.now());
            groupMapper.updateById(group);
            throw new BusinessException("该拼团已过期");
        }
        if (group.getCurrentCount() >= group.getRequiredCount()) {
            throw new BusinessException("该拼团已满员");
        }

        // Check if already joined
        LambdaQueryWrapper<GroupBuyMember> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(GroupBuyMember::getGroupId, groupId);
        checkWrapper.eq(GroupBuyMember::getUserId, userId);
        if (memberMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("您已参与该拼团");
        }

        // Add member
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setIsLeader(0);
        member.setJoinTime(LocalDateTime.now());
        memberMapper.insert(member);

        // Increment count
        groupMapper.incrementCurrentCount(groupId);
        group.setCurrentCount(group.getCurrentCount() + 1);

        // Check if group is complete
        if (group.getCurrentCount() >= group.getRequiredCount()) {
            group.setStatus(1);
            group.setCompleteTime(LocalDateTime.now());
            groupMapper.updateById(group);
            log.info("拼团成功: groupId={}, count={}", groupId, group.getCurrentCount());
        }

        return convertGroupToResponse(group);
    }

    @Override
    public GroupBuyGroupResponse getGroupDetail(Long groupId) {
        GroupBuyGroup group = getGroupById(groupId);

        // Get members
        LambdaQueryWrapper<GroupBuyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyMember::getGroupId, groupId);
        wrapper.orderByAsc(GroupBuyMember::getJoinTime);

        List<GroupBuyMember> members = memberMapper.selectList(wrapper);
        List<GroupBuyMemberResponse> memberResponses = members.stream().map(m -> {
            GroupBuyMemberResponse r = new GroupBuyMemberResponse();
            BeanUtils.copyProperties(m, r);
            return r;
        }).collect(Collectors.toList());

        GroupBuyGroupResponse response = convertGroupToResponse(group);
        response.setMembers(memberResponses);
        return response;
    }

    @Override
    public PageResult<GroupBuyGroupResponse> pageMyGroups(Long userId, Integer pageNum, Integer pageSize) {
        // Find groups where user is a member
        LambdaQueryWrapper<GroupBuyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupBuyMember::getUserId, userId);
        memberWrapper.eq(GroupBuyMember::getDeleted, 0);
        List<Long> groupIds = memberMapper.selectList(memberWrapper).stream()
                .map(GroupBuyMember::getGroupId)
                .distinct()
                .collect(Collectors.toList());

        if (groupIds.isEmpty()) {
            return PageResult.empty();
        }

        Page<GroupBuyGroup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GroupBuyGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GroupBuyGroup::getId, groupIds);
        wrapper.eq(GroupBuyGroup::getDeleted, 0);
        wrapper.orderByDesc(GroupBuyGroup::getCreateTime);

        Page<GroupBuyGroup> result = groupMapper.selectPage(page, wrapper);
        List<GroupBuyGroupResponse> records = result.getRecords().stream()
                .map(this::convertGroupToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(fixedDelay = 60000)
    public void processExpiredGroups() {
        LambdaQueryWrapper<GroupBuyGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroup::getStatus, 0);
        wrapper.lt(GroupBuyGroup::getExpireTime, LocalDateTime.now());
        wrapper.eq(GroupBuyGroup::getDeleted, 0);

        List<GroupBuyGroup> expired = groupMapper.selectList(wrapper);
        for (GroupBuyGroup group : expired) {
            group.setStatus(2);
            group.setCompleteTime(LocalDateTime.now());
            groupMapper.updateById(group);
            log.info("拼团过期: groupId={}", group.getId());

            // 查询拼团成员，逐个退款
            LambdaQueryWrapper<GroupBuyMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(GroupBuyMember::getGroupId, group.getId());
            memberWrapper.eq(GroupBuyMember::getDeleted, 0);
            List<GroupBuyMember> members = memberMapper.selectList(memberWrapper);

            for (GroupBuyMember member : members) {
                if (member.getOrderNo() == null) continue;
                try {
                    // 取消订单（状态 6 = 已取消）
                    jdbcTemplate.update(
                            "UPDATE oms_order SET status = 6, update_time = NOW() WHERE order_no = ? AND status IN (0, 1)",
                            member.getOrderNo());
                    // 退款支付单（状态 3 = 已退款）
                    int updated = jdbcTemplate.update(
                            "UPDATE oms_payment SET status = 3, update_time = NOW() WHERE order_no = ? AND status = 2",
                            member.getOrderNo());
                    if (updated > 0) {
                        log.info("拼团过期退款成功: groupId={}, userId={}, orderNo={}",
                                group.getId(), member.getUserId(), member.getOrderNo());
                    }
                } catch (Exception e) {
                    log.error("拼团过期退款失败: groupId={}, userId={}, orderNo={}, error={}",
                            group.getId(), member.getUserId(), member.getOrderNo(), e.getMessage());
                }
            }
        }
        if (!expired.isEmpty()) {
            log.info("处理过期拼团: {}个", expired.size());
        }
    }

    private GroupBuyActivity getActivityById(Long id) {
        LambdaQueryWrapper<GroupBuyActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyActivity::getId, id);
        wrapper.eq(GroupBuyActivity::getDeleted, 0);

        GroupBuyActivity activity = baseMapper.selectOne(wrapper);
        if (activity == null) {
            throw new BusinessException("拼团活动不存在");
        }
        return activity;
    }

    private GroupBuyGroup getGroupById(Long id) {
        LambdaQueryWrapper<GroupBuyGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBuyGroup::getId, id);
        wrapper.eq(GroupBuyGroup::getDeleted, 0);

        GroupBuyGroup group = groupMapper.selectOne(wrapper);
        if (group == null) {
            throw new BusinessException("拼团不存在");
        }
        return group;
    }

    private void validateActivityActive(GroupBuyActivity activity) {
        if (activity.getStatus() != 1) {
            throw new BusinessException("该活动已禁用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动尚未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动已结束");
        }
    }

    private GroupBuyActivityResponse convertActivityToResponse(GroupBuyActivity activity) {
        GroupBuyActivityResponse response = new GroupBuyActivityResponse();
        BeanUtils.copyProperties(activity, response);
        return response;
    }

    private GroupBuyGroupResponse convertGroupToResponse(GroupBuyGroup group) {
        GroupBuyGroupResponse response = new GroupBuyGroupResponse();
        BeanUtils.copyProperties(group, response);
        return response;
    }
}
