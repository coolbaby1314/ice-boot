package com.ice.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 日志链路追踪记录过滤器
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {
    private static final String TRACE_ID = "traceId";
    private static final String REQUEST_ID = "requestId";
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader("traceId");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        String requestId =  request.getRequestId();;

        MDC.put(TRACE_ID, traceId);
        MDC.put(REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear(); // 防止线程复用污染
        }
    }
}
