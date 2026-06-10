package com.shop.live.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.live.controller.request.LiveProductAddRequest;
import com.shop.live.controller.request.LiveProductUpdateRequest;
import com.shop.live.controller.response.LiveProductResponse;
import com.shop.live.entity.LiveProduct;

import java.util.List;

public interface LiveProductService extends IService<LiveProduct> {

    LiveProductResponse addProduct(LiveProductAddRequest request);

    void removeProduct(Long id);

    LiveProductResponse updateProduct(Long id, LiveProductUpdateRequest request);

    List<LiveProductResponse> getRoomProducts(Long roomId);

    void setExplaining(Long id);

    void unexplain(Long id);
}
