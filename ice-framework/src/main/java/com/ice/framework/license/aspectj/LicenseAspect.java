package com.ice.framework.license.aspectj;

import com.ice.framework.license.annotation.LicenseCheck;
import com.ice.framework.license.annotation.RequiresLicense;
import com.ice.common.exception.ServiceException;
import com.ice.framework.license.IceLicenseManager;
import com.ice.framework.license.service.LicenseService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 授权校验的切面
 */
@Slf4j
@Aspect
@Component
public class LicenseAspect {

    @Autowired
    IceLicenseManager licenseManager; // 负责解析本地 License 文件或校验服务器授权

    @Autowired
    LicenseService licenseService;


    @Before("@annotation(requiresLicense)")
    public void doBefore(JoinPoint point, RequiresLicense requiresLicense) {
        String pluginKey = requiresLicense.value();

        // 核心判断逻辑
        if (!licenseService.isPluginActive(pluginKey)) {
            throw new ServiceException("该模块未授权或已过期，请联系管理员激活！");
        }
    }

    @Around("@annotation(com.ice.framework.license.annotation.LicenseCheck)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LicenseCheck licenseCheck = method.getAnnotation(LicenseCheck.class);
        String moduleCode = licenseCheck.module();
        boolean checkBasic = licenseCheck.checkBasic();
        log.info("开始License检查 - 方法: {}, 模块: {}", method.getName(), moduleCode);
        // 检查基本License有效性
        if (checkBasic) {
            if (!licenseService.verify()) {
                log.error("License验证失败，无法执行方法: {}", method.getName());
                throw new RuntimeException("License验证失败，系统无法使用");
            }
        }
        if (!licenseManager.checkModule(moduleCode)) {
            log.error("模块 {} 未授权，无法执行方法: {}", licenseCheck, method.getName());
            // 抛出自定义异常，由全局异常处理器捕获返回 JSON
            throw new ServiceException("该模块未授权或已过期，请联系管理员激活！"+licenseCheck);
        }
        log.info("License检查通过，执行方法: {}", method.getName());
        return joinPoint.proceed();
    }
}
