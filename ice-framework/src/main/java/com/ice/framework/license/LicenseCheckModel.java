package com.ice.framework.license;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义需要校验的License参数
 * 校验模型
 * 扩展参数
 */
@Data
public class LicenseCheckModel implements Serializable {


    /**
     * 可被允许的IP地址
     */
    private List<String> ipAddress;

    /**
     * 可被允许的MAC地址
     */
    private List<String> macAddress;

    /**
     * 可被允许的CPU序列号
     */
    private String cpuSerial;

    /**
     * 可被允许的主板序列号
     */
    private String mainBoardSerial;

    /**
     * 可被允许的访问的module
     */
    private List<String> authorizedModules;

    /**
     * 最大并发用户数
     */
    private Integer maxUserCount;

    /**
     * 客户公司名称
     */
    private String companyName;

    /**
     * 客户联系人
     */
    private String contactPerson;
}
