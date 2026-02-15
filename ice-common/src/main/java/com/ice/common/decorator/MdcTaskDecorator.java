package com.ice.common.decorator;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        // 1. 获取主线程（当前线程）的 MDC 内容
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 2. 将内容设置进子线程的 MDC 中
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                // 3. 执行真正的异步任务
                runnable.run();
            } finally {
                // 4. 任务结束后清理，防止线程复用导致的污染
                MDC.clear();
            }
        };
    }
}