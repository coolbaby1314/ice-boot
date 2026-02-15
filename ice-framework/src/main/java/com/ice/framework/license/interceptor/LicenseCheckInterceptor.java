package com.ice.framework.license.interceptor;

import com.alibaba.fastjson2.JSON;
import com.ice.framework.license.LicenseCheckResult;
import com.ice.framework.license.LicenseVerify;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 *  LicenseCheckInterceptor
 */
@Slf4j
@Component
public class LicenseCheckInterceptor implements HandlerInterceptor {

    @Autowired
    LicenseVerify licenseVerify;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //校验证书是否有效

        LicenseCheckResult licenseCheckResult = licenseVerify.verify();
        boolean verifyResult = licenseCheckResult.isValid();

        if(verifyResult){
            return true;
        }else{
            response.setCharacterEncoding("utf-8");
            Map<String,String> result = new HashMap<>(1);
            result.put("result","您的证书无效，请核查服务器是否取得授权或重新申请证书！");

            response.getWriter().write(JSON.toJSONString(result));

            return false;
        }
    }
}
