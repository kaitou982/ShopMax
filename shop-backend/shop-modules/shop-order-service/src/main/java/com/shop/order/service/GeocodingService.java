package com.shop.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.order.config.AmapProperties;
import com.shop.order.util.AddressUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地理编码服务
 *
 * @author shop
 * @since 2026-06-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final AmapProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /** 坐标缓存（地址 -> 坐标） */
    private final Map<String, Coordinates> cache = new ConcurrentHashMap<>();

    /**
     * 地址转坐标
     *
     * @param address 地址字符串
     * @return 坐标，失败返回null
     */
    public Coordinates addressToCoordinates(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        // 先查缓存（使用原始地址作为缓存 key）
        Coordinates cached = cache.get(address);
        if (cached != null) {
            return cached;
        }

        // 地址清洗：剔除门牌号/楼层/驿站等细节，提升匹配准确度
        String cleanedAddress = AddressUtils.cleanForGeocoding(address);
        // 提取城市作为搜索范围约束
        String city = AddressUtils.extractCity(address);

        try {
            String encodedAddress = URLEncoder.encode(cleanedAddress, StandardCharsets.UTF_8);

            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(properties.getGeocodeUrl())
                    .append("?address=").append(encodedAddress)
                    .append("&key=").append(properties.getWebKey());

            if (!city.isBlank()) {
                urlBuilder.append("&city=").append(URLEncoder.encode(city, StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Coordinates coordinates = parseResponse(response.body(), address);

            // 缓存结果
            if (coordinates != null) {
                cache.put(address, coordinates);
            }

            return coordinates;

        } catch (Exception e) {
            log.error("地理编码失败: address={}, error={}", address, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析响应
     */
    private Coordinates parseResponse(String responseBody, String address) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText();

            if (!"1".equals(status)) {
                String info = root.path("info").asText();
                String infocode = root.path("infocode").asText();
                log.warn("高德地理编码失败: address={}, status={}, infocode={}, info={}", address, status, infocode, info);
                return null;
            }

            JsonNode geocodes = root.path("geocodes");
            if (!geocodes.isArray() || geocodes.isEmpty()) {
                log.warn("高德地理编码返回空结果: address={}, count={}", address,
                        geocodes.isArray() ? geocodes.size() : 0);
                return null;
            }

            JsonNode bestGeocode = geocodes.get(0);
            String level = bestGeocode.path("level").asText("");
            // 精度低于"区县"级别时警告，说明匹配可能不准
            if ("省".equals(level) || "城市".equals(level)) {
                log.warn("地理编码精度偏低: address={}, level={}, 建议提供更详细地址或手动指定坐标", address, level);
            }

            JsonNode location = bestGeocode.path("location");
            if (location.isMissingNode()) {
                log.warn("高德地理编码结果中缺少location字段: address={}", address);
                return null;
            }

            // location格式: "经度,纬度"
            String[] parts = location.asText().split(",");
            if (parts.length != 2) {
                log.warn("高德地理编码location格式异常: address={}, location={}", address, location.asText());
                return null;
            }

            Coordinates coordinates = new Coordinates();
            coordinates.setLongitude(new BigDecimal(parts[0]));
            coordinates.setLatitude(new BigDecimal(parts[1]));

            return coordinates;

        } catch (Exception e) {
            log.error("解析地理编码响应失败: address={}", address, e);
            return null;
        }
    }

    /**
     * 坐标
     */
    @Data
    public static class Coordinates {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}
