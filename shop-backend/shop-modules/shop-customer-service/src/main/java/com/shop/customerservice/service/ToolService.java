package com.shop.customerservice.service;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.customerservice.mapper.CsFaqMapper;
import com.shop.order.entity.Order;
import com.shop.order.mapper.OrderMapper;
import com.shop.product.entity.Category;
import com.shop.product.entity.Product;
import com.shop.product.mapper.CategoryMapper;
import com.shop.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolService {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;
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
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getDeleted, 0)
               .like(Product::getName, keyword)
               .orderByDesc(Product::getSales)
               .last("LIMIT 5");

        List<Product> products = productMapper.selectList(wrapper);
        if (products.isEmpty()) {
            return "{\"message\": \"未找到与'" + keyword + "'相关的商品\"}";
        }

        List<Dict> result = products.stream().map(p -> Dict.create()
                .set("id", p.getId())
                .set("name", p.getName())
                .set("price", p.getSalePrice().doubleValue())
                .set("originalPrice", p.getOriginalPrice() != null ? p.getOriginalPrice().doubleValue() : null)
                .set("stock", p.getStock())
                .set("sales", p.getSales())
                .set("mainImage", p.getMainImage())
        ).collect(Collectors.toList());

        return JSONUtil.toJsonStr(result);
    }

    private String queryOrder(String orderNo, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getUserId, userId)
               .eq(Order::getDeleted, 0);

        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            return "{\"error\": \"未找到该订单，请确认订单号是否正确\"}";
        }

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

        // 手机号脱敏
        String phone = order.getReceiverPhone();
        if (phone != null && phone.length() >= 7) {
            phone = phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }

        Dict result = Dict.create()
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

        return JSONUtil.toJsonStr(result);
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
        ).collect(Collectors.toList());

        return JSONUtil.toJsonStr(result);
    }

    private String recommendProducts(String category, Double budget) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getDeleted, 0);

        // 如果指定了分类，查找分类ID
        if (category != null && !category.isBlank()) {
            LambdaQueryWrapper<Category> catWrapper = new LambdaQueryWrapper<>();
            catWrapper.eq(Category::getStatus, 1)
                      .eq(Category::getDeleted, 0)
                      .like(Category::getName, category);
            List<Category> categories = categoryMapper.selectList(catWrapper);
            if (!categories.isEmpty()) {
                wrapper.eq(Product::getCategoryId, categories.get(0).getId());
            }
        }

        if (budget != null && budget > 0) {
            wrapper.le(Product::getSalePrice, BigDecimal.valueOf(budget));
        }

        wrapper.orderByDesc(Product::getSales)
               .last("LIMIT 5");

        List<Product> products = productMapper.selectList(wrapper);
        if (products.isEmpty()) {
            String msg = budget != null
                    ? "未找到" + budget + "元以内的" + (category != null ? category : "商品")
                    : "暂无合适的商品推荐";
            return "{\"message\": \"" + msg + "\"}";
        }

        List<Dict> result = products.stream().map(p -> Dict.create()
                .set("id", p.getId())
                .set("name", p.getName())
                .set("price", p.getSalePrice().doubleValue())
                .set("originalPrice", p.getOriginalPrice() != null ? p.getOriginalPrice().doubleValue() : null)
                .set("sales", p.getSales())
                .set("stock", p.getStock())
                .set("mainImage", p.getMainImage())
        ).collect(Collectors.toList());

        return JSONUtil.toJsonStr(result);
    }
}
