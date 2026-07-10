package com.shop.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MDC 追踪过滤器
 * 从请求头中读取 traceId 并注入到 MDC，供日志框架使用
 *
 * @author kaitou
 * @since 2026/06/23
 */
@Component
public class MdcTraceFilter implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest httpRequest) {
                String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
                if (traceId != null && !traceId.isEmpty()) {
                    MDC.put(TRACE_ID_KEY, traceId);
                }
            }
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
