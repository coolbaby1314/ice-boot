package com.ice.framework.license.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能模块授权注解
 * 用于标记需要License授权的功能模块
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LicenseCheck {
    /**
     * 功能模块名称
     * 例如: ORDER_MANAGEMENT, INVENTORY_MANAGEMENT, FINANCIAL_MANAGEMENT
     */
    String module() default "";
    /**
     * 是否检查License基本有效性（时间等）
     */
    boolean checkBasic() default true;
}