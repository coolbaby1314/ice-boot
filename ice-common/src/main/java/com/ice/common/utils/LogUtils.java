package com.ice.common.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 处理并记录日志文件
 * 
 * @author ice
 */
public class LogUtils
{

    private LogUtils() {}

    public static Logger invoice() {
        return LoggerFactory.getLogger("invoice");
    }

    public static Logger cbs() {
        return LoggerFactory.getLogger("cbs");
    }

    public static Logger images() {
        return LoggerFactory.getLogger("images");
    }

    public static Logger voucher() {
        return LoggerFactory.getLogger("voucher");
    }

    public static Logger api() {
        return LoggerFactory.getLogger("api.access");
    }
    public static Logger audit() {
        return LoggerFactory.getLogger("audit");
    }
    public static String getBlock(Object msg)
    {
        if (msg == null)
        {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
