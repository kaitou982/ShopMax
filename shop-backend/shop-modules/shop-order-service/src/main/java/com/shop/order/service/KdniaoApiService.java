package com.shop.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.order.config.KdniaoProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 快递100 API服务
 *
 * @author shop
 * @since 2026-06-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KdniaoApiService {

    private final KdniaoProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 查询物流轨迹
     *
     * @param logisticsNo 物流单号
     * @param companyCode 快递公司编码
     * @return 物流查询结果
     */
    public LogisticsQueryResult queryLogistics(String logisticsNo, String companyCode) {
        return queryLogistics(logisticsNo, companyCode, null, null, null);
    }

    /**
     * 查询物流轨迹（带手机号）
     *
     * @param logisticsNo 物流单号
     * @param companyCode 快递公司编码
     * @param phone 手机号（顺丰、中通等需要）
     * @return 物流查询结果
     */
    public LogisticsQueryResult queryLogistics(String logisticsNo, String companyCode, String phone) {
        return queryLogistics(logisticsNo, companyCode, phone, null, null);
    }

    /**
     * 查询物流轨迹（完整参数）
     *
     * @param logisticsNo 物流单号
     * @param companyCode 快递公司编码
     * @param phone 手机号（顺丰、中通等需要）
     * @param fromCity 发件城市（如"深圳市"）
     * @param toCity 收件城市（如"北京市"）
     * @return 物流查询结果
     */
    public LogisticsQueryResult queryLogistics(String logisticsNo, String companyCode, String phone,
                                                String fromCity, String toCity) {
        try {
            // 构建请求参数
            String param = buildQueryParam(logisticsNo, companyCode, phone, fromCity, toCity);
            String sign = generateSign(param);

            // 构建请求体
            String requestBody = String.format(
                    "customer=%s&param=%s&sign=%s",
                    properties.getCustomerId(),
                    param,
                    sign
            );

            // 发送请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getQueryUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            // 解析响应
            log.debug("快递100响应: {}", response.body());
            return parseResponse(response.body());

        } catch (Exception e) {
            log.error("查询物流失败: logisticsNo={}, error={}", logisticsNo, e.getMessage(), e);
            return LogisticsQueryResult.failure("查询物流失败: " + e.getMessage());
        }
    }

    /**
     * 从地址文本中提取城市名称（如 "广东省深圳市南山区..." → "深圳市"）
     */
    public static String extractCity(String address) {
        return com.shop.order.util.AddressUtils.extractCity(address);
    }

    /**
     * 构建查询参数
     */
    private String buildQueryParam(String logisticsNo, String companyCode, String phone,
                                    String fromCity, String toCity) {
        String from = fromCity != null ? fromCity : "";
        String to = toCity != null ? toCity : "";
        if (phone != null && !phone.isBlank()) {
            return String.format(
                    "{\"com\":\"%s\",\"num\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"phone\":\"%s\"}",
                    companyCode, logisticsNo, from, to, phone
            );
        }
        return String.format(
                "{\"com\":\"%s\",\"num\":\"%s\",\"from\":\"%s\",\"to\":\"%s\"}",
                companyCode, logisticsNo, from, to
        );
    }

    /**
     * 生成签名
     * 签名规则: MD5(param + key + customer)
     */
    private String generateSign(String param) {
        try {
            String raw = param + properties.getAppKey() + properties.getCustomerId();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest).toUpperCase();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "";
        }
    }

    /**
     * 字节数组转十六进制
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 解析响应
     */
    private LogisticsQueryResult parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText();

            if (!"200".equals(status)) {
                String message = root.path("message").asText();
                log.warn("快递100返回错误: status={}, message={}", status, message);
                return LogisticsQueryResult.failure(message);
            }

            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return LogisticsQueryResult.failure("返回数据格式错误");
            }

            List<TraceItem> traces = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (JsonNode item : data) {
                TraceItem trace = new TraceItem();
                trace.setTime(item.path("ftime").asText());
                trace.setContent(item.path("context").asText());
                trace.setLocation(item.path("location").asText());
                trace.setTraceTime(LocalDateTime.parse(item.path("ftime").asText(), formatter));
                traces.add(trace);
            }

            // 快递100返回的轨迹是倒序的（最新的在前面），我们需要反转
            List<TraceItem> reversedTraces = new ArrayList<>();
            for (int i = traces.size() - 1; i >= 0; i--) {
                reversedTraces.add(traces.get(i));
            }

            LogisticsQueryResult result = LogisticsQueryResult.success();
            result.setTraces(reversedTraces);

            // 根据最新轨迹判断状态
            if (!traces.isEmpty()) {
                String latestContent = traces.get(0).getContent();
                if (latestContent.contains("签收") || latestContent.contains("已签收")) {
                    result.setStatus(3); // 已签收
                } else if (latestContent.contains("派送") || latestContent.contains("派件")) {
                    result.setStatus(2); // 派送中
                } else {
                    result.setStatus(1); // 运输中
                }
            }

            return result;

        } catch (Exception e) {
            log.error("解析快递100响应失败", e);
            return LogisticsQueryResult.failure("解析响应失败");
        }
    }

    /**
     * 物流查询结果
     */
    @Data
    public static class LogisticsQueryResult {
        private boolean success;
        private String message;
        private Integer status;
        private List<TraceItem> traces;

        public static LogisticsQueryResult success() {
            LogisticsQueryResult result = new LogisticsQueryResult();
            result.setSuccess(true);
            return result;
        }

        public static LogisticsQueryResult failure(String message) {
            LogisticsQueryResult result = new LogisticsQueryResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }

    /**
     * 轨迹项
     */
    @Data
    public static class TraceItem {
        private String time;
        private String content;
        private String location;
        private LocalDateTime traceTime;
    }
}
