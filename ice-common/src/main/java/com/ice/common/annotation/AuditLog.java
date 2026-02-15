package com.ice.common.annotation;
import java.lang.annotation.*;

/**
 * 自定义审计log注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface  AuditLog {
    /**
     * 模块
     */
    String module();

    /**
     * 操作
     */
    String action();

    /**
     * 方法参数名
     */
    String bizIdParam() default ""; // 支持SpEL表达式

    boolean recordRequest() default true;

    boolean recordResponse() default false;

    boolean recordDiff() default false;

    boolean financial() default false; // 金融场景增强
}
