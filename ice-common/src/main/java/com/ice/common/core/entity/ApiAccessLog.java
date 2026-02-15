package com.ice.common.core.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiAccessLog {
    private String app;        // 应用名
    private String env;        // 环境
    private String traceId;    // 链路ID（如有）
    private String ip;         // 客户端IP
    private String method;     // GET/POST
    private String uri;        // 请求路径
    private String query;      // 查询参数
    private Integer status;    // HTTP状态码
    private Long duration;     // 耗时(ms)
    private String timestamp;  // 时间戳
}