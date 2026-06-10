package com.shop.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.order.config.AmapProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 驾车路线规划服务
 *
 * @author shop
 * @since 2026-06-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingRouteService {

    private final AmapProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 获取驾车路线
     *
     * @param originLng 起点经度
     * @param originLat 起点纬度
     * @param destLng 终点经度
     * @param destLat 终点纬度
     * @return 路线坐标点列表
     */
    public RouteResult getDrivingRoute(BigDecimal originLng, BigDecimal originLat,
                                       BigDecimal destLng, BigDecimal destLat) {
        try {
            String origin = originLng + "," + originLat;
            String destination = destLng + "," + destLat;

            String url = String.format(
                    "https://restapi.amap.com/v3/direction/driving?origin=%s&destination=%s&key=%s&strategy=0",
                    origin, destination, properties.getWebKey()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseRouteResponse(response.body());

        } catch (Exception e) {
            log.error("获取驾车路线失败: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析路线响应
     */
    private RouteResult parseRouteResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText();

            if (!"1".equals(status)) {
                log.warn("高德路线API返回错误: status={}", status);
                return null;
            }

            JsonNode paths = root.path("route").path("paths");
            if (!paths.isArray() || paths.isEmpty()) {
                return null;
            }

            JsonNode path = paths.get(0);
            RouteResult result = new RouteResult();

            // 总距离和时间
            result.setDistance(path.path("distance").asText());
            result.setDuration(path.path("duration").asText());

            // 解析所有步骤的坐标点
            List<double[]> routePoints = new ArrayList<>();
            JsonNode steps = path.path("steps");

            if (steps.isArray()) {
                for (JsonNode step : steps) {
                    String polyline = step.path("polyline").asText();
                    if (polyline != null && !polyline.isEmpty()) {
                        String[] points = polyline.split(";");
                        for (String point : points) {
                            String[] coords = point.split(",");
                            if (coords.length == 2) {
                                try {
                                    double lng = Double.parseDouble(coords[0]);
                                    double lat = Double.parseDouble(coords[1]);
                                    routePoints.add(new double[]{lng, lat});
                                } catch (NumberFormatException e) {
                                    // 忽略解析失败的点
                                }
                            }
                        }
                    }
                }
            }

            result.setRoutePoints(routePoints);
            return result;

        } catch (Exception e) {
            log.error("解析路线响应失败", e);
            return null;
        }
    }

    /**
     * 路线结果
     */
    @Data
    public static class RouteResult {
        /** 总距离（米） */
        private String distance;
        /** 总时间（秒） */
        private String duration;
        /** 路线坐标点 [lng, lat] */
        private List<double[]> routePoints;
    }
}
