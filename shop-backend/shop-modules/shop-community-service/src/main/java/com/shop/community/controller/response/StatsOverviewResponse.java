package com.shop.community.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsOverviewResponse {

    private Long pendingReviewCount;
    private Long todayApprovedCount;
    private Long todayRejectedCount;
    private Long totalNoteCount;
}
