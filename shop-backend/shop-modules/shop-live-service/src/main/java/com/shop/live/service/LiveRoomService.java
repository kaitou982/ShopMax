package com.shop.live.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.live.controller.request.LiveRoomCreateRequest;
import com.shop.live.controller.request.LiveRoomUpdateRequest;
import com.shop.live.controller.response.LiveRoomResponse;
import com.shop.live.entity.LiveRoom;

import java.util.List;

public interface LiveRoomService extends IService<LiveRoom> {

    LiveRoomResponse create(LiveRoomCreateRequest request);

    LiveRoomResponse update(Long id, LiveRoomUpdateRequest request);

    void delete(Long id);

    LiveRoomResponse getById(Long id);

    PageResult<LiveRoomResponse> page(Integer pageNum, Integer pageSize, Integer type, Integer status);

    List<LiveRoomResponse> listLiving();

    LiveRoomResponse startLive(Long id);

    LiveRoomResponse endLive(Long id);
}
