package com.shop.gateway.filter;

import io.micrometer.tracing.Tracer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 链路追踪过滤器
 * 在 Gateway 中注入 traceId 到请求头，传递给下游服务
 *
 * @author kaitou
 * @since 2026/06/23
 */
@Component
public class TraceFilter implements GlobalFilter, Ordered {

    private final Tracer tracer;

    public TraceFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var span = tracer.currentSpan();
        if (span != null) {
            String traceId = span.context().traceId();
            exchange = exchange.mutate().request(
                exchange.getRequest().mutate()
                    .header("X-Trace-Id", traceId)
                    .build()
            ).build();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -150; // 在 RateLimitFilter(-200) 和 AuthGlobalFilter(-100) 之间
    }
}
