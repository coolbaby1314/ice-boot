package com.ice.framework.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.common.core.entity.ApiAccessLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ApiAccessInterceptor implements HandlerInterceptor {

    // 关键点：这里的名称必须和你 logback-spring.xml 中的 logger name 一致
    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("API-ACCESS");

    private static final ObjectMapper JSON = new ObjectMapper();
    // 使用 ThreadLocal 记录开始时间，确保线程安全
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    // 链路追踪ID
    private static final String TRACE_ID = "traceId";

    @Value("${spring.application.name:ice-admin}")
    private String appName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 生成唯一 ID（去掉横杠）
        String tid = UUID.randomUUID().toString().replace("-", "");
        // 2. 存入 MDC
        MDC.put(TRACE_ID, tid);
        // 3. 顺便放入响应头，方便前端反馈问题
        response.setHeader("X-Trace-Id", tid);

        START_TIME.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            long duration = System.currentTimeMillis() - START_TIME.get();

            ApiAccessLog logEntity = ApiAccessLog.builder()
                    .app(appName)
                    .env(activeProfile)
                    .traceId(MDC.get("traceId"))
                    .ip(getClientIp(request))
                    .method(request.getMethod())
                    .uri(request.getRequestURI())
                    .query(request.getQueryString())
                    .status(response.getStatus())
                    .duration(duration)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
                    .build();

            ACCESS_LOG.info(JSON.writeValueAsString(logEntity));
        } catch (Exception e) {
            // 降级处理，避免日志记录失败影响主业务
            ACCESS_LOG.error("Failed to record API access log", e);
        } finally {
            MDC.remove(TRACE_ID);
            START_TIME.remove(); // 必须清除，防止内存泄漏
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip.split(",")[0]; // 处理多重代理情况
    }
}