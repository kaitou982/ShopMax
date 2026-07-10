package com.shop.common.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * 在微服务间调用时传播用户身份信息和链路追踪ID
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 传播用户身份头
        propagateHeader(request, template, "X-User-Id");
        propagateHeader(request, template, "X-User-Role");
        propagateHeader(request, template, "Authorization");
        // 传播链路追踪ID
        propagateHeader(request, template, "X-Trace-Id");
    }

    private void propagateHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isEmpty()) {
            template.header(headerName, value);
        }
    }
}
