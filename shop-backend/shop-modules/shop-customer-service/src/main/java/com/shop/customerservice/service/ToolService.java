package com.shop.customerservice.service;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.shop.common.feign.client.OrderServiceClient;
import com.shop.common.feign.client.ProductServiceClient;
import com.shop.common.feign.dto.order.OrderSimpleResponse;
import com.shop.common.feign.dto.product.CategorySimpleResponse;
import com.shop.common.feign.dto.product.ProductSimpleResponse;
import com.shop.common.web.Result;
import com.shop.customerservice.mapper.CsFaqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolService {

    private final ProductServiceClient productServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final CsFaqMapper faqMapper;

    public String executeTool(String toolName, Map<String, Object> args, Long userId) {
        try {
            return switch (toolName) {
                case "queryProduct" -> queryProduct((String) args.get("keyword"));
                case "queryOrder" -> {
                    String orderNo = (String) args.get("orderNo");
                    yield queryOrder(orderNo, userId);
                }
                case "searchFAQ" -> searchFAQ((String) args.get("question"));
                case "recommendProducts" -> {
                    String category = (String) args.get("category");
                    Number budget = (Number) args.get("budget");
                    yield recommendProducts(category, budget != null ? budget.doubleValue() : null);
                }
                default -> "{\"error\": \"未知工具\"}";
            };
        } catch (Exception e) {
            log.error("工具调用失败: tool={}, args={}, error={}", toolName, args, e.getMessage(), e);
            return "{\"error\": \"查询暂时不可用，请稍后再试\"}";
        }
    }

    private String queryProduct(String keyword) {
        Result<List<ProductSimpleResponse>> result = productServiceClient.searchProducts(
                keyword, null, null, "sales", 5);

        if (result.getCode() != 200 || result.getData() == null || result.getData().isEmpty()) {
            return "{\"message\": \"未找到与'" + keyword + "'相关的商品\"}";
        }

        List<Dict> products = result.getData().stream().map(p -> Dict.create()
                .set("id", p.getId())
                .set("name", p.getName())
                .set("price", p.getSalePrice().doubleValue())
                .set("originalPrice", p.getOriginalPrice() != null ? p.getOriginalPrice().doubleValue() : null)
                .set("stock", p.getStock())
                .set("sales", p.getSales())
                .set("mainImage", p.getMainImage())
        ).toList();

        return JSONUtil.toJsonStr(products);
    }

    private String queryOrder(String orderNo, Long userId) {
        Result<OrderSimpleResponse> result = orderServiceClient.getByOrderNo(orderNo, userId);

        if (result.getCode() != 200 || result.getData() == null) {
            return "{\"error\": \"未找到该订单，请确认订单号是否正确\"}";
        }

        OrderSimpleResponse order = result.getData();

        String statusText = switch (order.getStatus()) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "退款中";
            case 6 -> "已退款";
            default -> "未知";
        };

        String phone = order.getReceiverPhone();
        if (phone != null && phone.length() >= 7) {
            phone = phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }

        Dict dict = Dict.create()
                .set("orderNo", order.getOrderNo())
                .set("status", statusText)
                .set("totalAmount", order.getTotalAmount().doubleValue())
                .set("payAmount", order.getPayAmount().doubleValue())
                .set("freightAmount", order.getFreightAmount() != null ? order.getFreightAmount().doubleValue() : 0)
                .set("receiverName", maskName(order.getReceiverName()))
                .set("receiverPhone", phone)
                .set("receiverAddress", order.getReceiverAddress())
                .set("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : null)
                .set("payTime", order.getPayTime() != null ? order.getPayTime().toString() : null)
                .set("deliveryTime", order.getDeliveryTime() != null ? order.getDeliveryTime().toString() : null);

        return JSONUtil.toJsonStr(dict);
    }

    private String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*" + (name.length() > 2 ? name.substring(2) : "");
    }

    private String searchFAQ(String question) {
        List<com.shop.customerservice.entity.CsFaq> faqs = faqMapper.search(question);
        if (faqs.isEmpty()) {
            return "{\"message\": \"未找到相关问题，建议联系人工客服\"}";
        }

        List<Dict> result = faqs.stream().map(f -> Dict.create()
                .set("question", f.getQuestion())
                .set("answer", f.getAnswer())
                .set("category", f.getCategory())
        ).toList();

        return JSONUtil.toJsonStr(result);
    }

    private String recommendProducts(String category, Double budget) {
        Long categoryId = null;

        if (category != null && !category.isBlank()) {
            Result<List<CategorySimpleResponse>> catResult = productServiceClient.searchCategories(category);
            if (catResult.getCode() == 200 && catResult.getData() != null && !catResult.getData().isEmpty()) {
                categoryId = catResult.getData().get(0).getId();
            }
        }

        Result<List<ProductSimpleResponse>> result = productServiceClient.searchProducts(
                null, categoryId, budget, "sales", 5);

        if (result.getCode() != 200 || result.getData() == null || result.getData().isEmpty()) {
            String msg = budget != null
                    ? "未找到" + budget + "元以内的" + (category != null ? category : "商品")
                    : "暂无合适的商品推荐";
            return "{\"message\": \"" + msg + "\"}";
        }

        List<Dict> products = result.getData().stream().map(p -> Dict.create()
                .set("id", p.getId())
                .set("name", p.getName())
                .set("price", p.getSalePrice().doubleValue())
                .set("originalPrice", p.getOriginalPrice() != null ? p.getOriginalPrice().doubleValue() : null)
                .set("sales", p.getSales())
                .set("stock", p.getStock())
                .set("mainImage", p.getMainImage())
        ).toList();

        return JSONUtil.toJsonStr(products);
    }
}
