package com.shop.admin.controller;

import ch.qos.logback.classic.Logger;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "优惠券管理")
@RestController
@RequestMapping("/api/v1/admin/marketing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class CouponAdminController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "优惠券领取记录")
    @GetMapping("/coupons/{couponId}/records")
    public Result<PageResult<Map<String, Object>>> redemptionRecords(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String countSql = "SELECT COUNT(*) FROM mms_coupon_receive r " +
                "JOIN ums_user u ON r.user_id = u.id WHERE r.coupon_id = ? AND r.deleted = 0";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, couponId);
        if (total == null) total = 0L;

        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT r.id, r.user_id, u.nickname as userNickname, u.phone as userPhone, " +
                "r.receive_time, r.use_time, r.order_id, r.order_no, r.status " +
                "FROM mms_coupon_receive r " +
                "JOIN ums_user u ON r.user_id = u.id " +
                "WHERE r.coupon_id = ? AND r.deleted = 0 " +
                "ORDER BY r.create_time DESC LIMIT ? OFFSET ?";
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql, couponId, pageSize, offset);

        int pages = (int) Math.ceil((double) total / pageSize);
        return Result.success(PageResult.of(records, total, (long) pages));
    }

    @Operation(summary = "优惠券发放统计")
    @GetMapping("/coupons/{couponId}/stats")
    public Result<Map<String, Object>> couponStats(@PathVariable Long couponId) {
        String sql = "SELECT c.name, c.total_count, c.received_count, c.used_count, " +
                "c.per_limit, c.status, c.create_time " +
                "FROM mms_coupon c WHERE c.id = ? AND c.deleted = 0";
        Map<String, Object> stats = jdbcTemplate.queryForMap(sql, couponId);
        if (stats == null) {
            return Result.error("优惠券不存在");
        }

        // Claim rate and use rate
        Object totalObj = stats.get("total_count");
        Object receivedObj = stats.get("received_count");
        Object usedObj = stats.get("used_count");
        long total = totalObj != null ? ((Number) totalObj).longValue() : 0;
        long received = receivedObj != null ? ((Number) receivedObj).longValue() : 0;
        long used = usedObj != null ? ((Number) usedObj).longValue() : 0;
        stats.put("claimRate", total > 0 ? Math.round(received * 10000.0 / total) / 100.0 : 0);
        stats.put("useRate", received > 0 ? Math.round(used * 10000.0 / received) / 100.0 : 0);

        return Result.success(stats);
    }

    @Operation(summary = "定向发券")
    @PostMapping("/coupons/{couponId}/grant")
    public Result<Map<String, Object>> grantToUsers(
            @PathVariable Long couponId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> userIdsRaw = (List<Integer>) body.get("userIds");
        if (userIdsRaw == null || userIdsRaw.isEmpty()) {
            return Result.badRequest("用户列表不能为空");
        }

        // Check coupon
        String couponSql = "SELECT received_count, total_count, per_limit, status FROM mms_coupon WHERE id = ? AND deleted = 0";
        Map<String, Object> coupon = jdbcTemplate.queryForMap(couponSql, couponId);
        if (coupon == null) return Result.error("优惠券不存在");
        int status = (Integer) coupon.get("status");
        if (status != 1) return Result.error("优惠券已禁用");
        long totalCount = ((Number) coupon.get("total_count")).longValue();
        long receivedCount = ((Number) coupon.get("received_count")).longValue();

        int success = 0;
        int failed = 0;
        List<Long> userIds = userIdsRaw.stream().map(Long::valueOf).toList();

        for (Long userId : userIds) {
            try {
                // Check remaining
                String countSql = "SELECT received_count FROM mms_coupon WHERE id = ? AND deleted = 0";
                Map<String, Object> current = jdbcTemplate.queryForMap(countSql, couponId);
                long currentReceived = ((Number) current.get("received_count")).longValue();
                if (currentReceived >= totalCount) { failed++; continue; }

                // Per-limit check
                String limitSql = "SELECT COUNT(*) as cnt FROM mms_coupon_receive WHERE coupon_id = ? AND user_id = ? AND deleted = 0";
                Map<String, Object> limitResult = jdbcTemplate.queryForMap(limitSql, couponId, userId);
                long userCount = ((Number) limitResult.get("cnt")).longValue();
                Integer perLimit = (Integer) coupon.get("per_limit");
                if (perLimit != null && perLimit > 0 && userCount >= perLimit) { failed++; continue; }

                // Grant
                jdbcTemplate.update("INSERT INTO mms_coupon_receive (coupon_id, user_id, receive_time, status, create_time) VALUES (?, ?, NOW(), 0, NOW())", couponId, userId);
                jdbcTemplate.update("UPDATE mms_coupon SET received_count = received_count + 1 WHERE id = ? AND received_count < total_count", couponId);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("total", userIds.size());
        log.info("定向发券完成: couponId={}, success={}, failed={}", couponId, success, failed);
        return Result.success(result);
    }

    @Operation(summary = "领取趋势统计")
    @GetMapping("/coupons/{couponId}/trend")
    public Result<List<Map<String, Object>>> couponTrend(@PathVariable Long couponId) {
        String sql = "SELECT DATE(receive_time) as date, COUNT(*) as count " +
                "FROM mms_coupon_receive WHERE coupon_id = ? AND deleted = 0 " +
                "AND receive_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(receive_time) ORDER BY DATE(receive_time)";
        List<Map<String, Object>> raw = jdbcTemplate.queryForList(sql, couponId);

        // Fill missing days with 0
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.util.Map<String, Long> dateMap = new java.util.LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dateMap.put(java.time.LocalDate.now().minusDays(i).format(fmt), 0L);
        }
        for (Map<String, Object> row : raw) {
            Object dateObj = row.get("date");
            Object countObj = row.get("count");
            if (dateObj != null) {
                dateMap.put(dateObj.toString(), countObj != null ? ((Number) countObj).longValue() : 0L);
            }
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : dateMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return Result.success(result);
    }
}
