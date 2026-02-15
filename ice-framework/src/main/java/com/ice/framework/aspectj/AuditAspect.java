package com.ice.framework.aspectj;

import com.alibaba.fastjson2.JSON;
import com.ice.common.annotation.AuditLog;
import com.ice.common.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 审计操作日志记录处理
 *
 * @author ice
 */
@Aspect
@Component
public class AuditAspect {
    private static final Logger auditLogger =
            LoggerFactory.getLogger("audit.log");

    /** 参数最大长度限制 */
    private static final int PARAM_MAX_LENGTH = 2000;

    @Pointcut("@annotation(auditLog)")
    public void auditPoint(AuditLog auditLog) {}

    @AfterReturning(value = "@annotation(auditLog)", returning = "result")
    public void afterSuccess(JoinPoint joinPoint,
                             AuditLog auditLog,
                             Object result) {

        Long userId = getCurrentUserId(); // 自己实现
        String bizId = extractBizId(joinPoint, auditLog.bizIdParam());

        auditLogger.info(
                "userId={} module={} action={} bizId={} result=SUCCESS",
                userId,
                auditLog.module(),
                auditLog.action(),
                bizId
        );
    }

    @AfterThrowing(value = "@annotation(auditLog)", throwing = "e")
    public void afterError(JoinPoint joinPoint,
                           AuditLog auditLog,
                           Throwable e) {

        Long userId = getCurrentUserId();
        String bizId = extractBizId(joinPoint, auditLog.bizIdParam());

        auditLogger.error(
                "userId={} module={} action={} bizId={} result=FAILED",
                userId,
                auditLog.module(),
                auditLog.action(),
                bizId,
                e
        );
    }

    private Long getCurrentUserId() {
        // 从 SecurityContext 取
        return 10001L;
    }

    private String extractBizId(JoinPoint joinPoint, String paramName) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            return String.valueOf(args[0]);
        }
        return "UNKNOWN";
    }

    /**
     * 参数拼装
     * @param paramsArray
     * @param excludeParamNames
     * @return
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames)
    {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null)
        {
            for (Object o : paramsArray)
            {
                if (StringUtils.isNotNull(o))
                {
                    try
                    {
                        String jsonObj = JSON.toJSONString(o);
                        params.append(jsonObj).append(" ");
                        if (params.length() >= PARAM_MAX_LENGTH)
                        {
                            return StringUtils.substring(params.toString(), 0, PARAM_MAX_LENGTH);
                        }
                    }
                    catch (Exception e)
                    {
                        auditLogger.error("请求参数拼装异常 msg:{}, 参数:{}", e.getMessage(), paramsArray, e);
                    }
                }
            }
        }
        return params.toString();
    }
}
