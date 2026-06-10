package com.shop.marketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.GroupBuyActivityCreateRequest;
import com.shop.marketing.controller.response.GroupBuyActivityResponse;
import com.shop.marketing.controller.response.GroupBuyGroupResponse;
import com.shop.marketing.entity.GroupBuyActivity;

import java.util.List;

public interface GroupBuyService extends IService<GroupBuyActivity> {

    GroupBuyActivityResponse createActivity(GroupBuyActivityCreateRequest request);

    PageResult<GroupBuyActivityResponse> pageActivities(Integer pageNum, Integer pageSize);

    List<GroupBuyActivityResponse> listActive();

    /** 开团 */
    GroupBuyGroupResponse startGroup(Long activityId, Long userId);

    /** 参团 */
    GroupBuyGroupResponse joinGroup(Long groupId, Long userId);

    /** 拼团详情 */
    GroupBuyGroupResponse getGroupDetail(Long groupId);

    PageResult<GroupBuyGroupResponse> pageMyGroups(Long userId, Integer pageNum, Integer pageSize);

    /** 处理过期拼团 */
    void processExpiredGroups();
}
