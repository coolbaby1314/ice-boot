package com.ice.framework.aspectj;

import com.alibaba.fastjson2.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
public class ApiLogAspect {
    private static final Logger log = LoggerFactory.getLogger("API-ACCESS");

    @Around("execution(* com.ice..controller..*(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        log.info("Method: {} | Args: {}", methodName, Arrays.toString(args));

        Object result = joinPoint.proceed();

        String output="";
        if(Objects.nonNull(result)){
            output = JSON.toJSONString(result).substring(0,2000);
        }
        log.info("Method: {} | Result: {}", methodName, output);
        return result;
    }
}