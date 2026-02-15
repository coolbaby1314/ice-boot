package com.ice.framework.license;

import de.schlichtherle.license.LicenseContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IceLicense管理器
 * 审查是否启用了授权module
 */
@Component
@Slf4j
public class IceLicenseManager {

    @Autowired
    private LicenseVerify licenseVerify;


    // 使用 ConcurrentHashMap 保证线程安全，缓存各个模块的授权状态
    // Key: 模块名称/代码, Value: 该模块的详细配置或过期时间
    private final Map<String, Object> moduleCache = new ConcurrentHashMap<>();

    // 标记整体 License 是否有效
    private volatile boolean isLicenseValid = false;



    // 缓存：授权的模块列表
    private final Set<String> authorizedModules = ConcurrentHashMap.newKeySet();
    // 缓存：最近一次物理校验时间
    private long lastCheckTime = 0L;
    // 校验间隔（例如 1 小时物理验签一次，平时查内存）
    private static final long CHECK_INTERVAL = 3600 * 1000L;

    /**
     * 核心校验方法：外部调用只需查 Map，复杂度 O(1)
     */
    public boolean checkModule(String moduleCode) {
        if (!isLicenseValid) {
            // 尝试触发一次刷新
            refreshModuleCache();
        }
        // 2. 检查模块是否在已授权列表中
        return isLicenseValid && authorizedModules.contains(moduleCode);

    }

    /**
     * 深度刷新：解析 License 内容并同步到内存
     */
    @Scheduled(cron = "0 0/10 * * * ?") // 每10分钟自动同步一次
    public synchronized void refreshModuleCache() {
        try {
            // 调用你原有的 verify()
            // 注意：这里可以根据需要修改 LicenseVerify.verify() 返回 LicenseContent
            LicenseContent content = licenseVerify.getLicenseManager().verify();

            // 获取 extra 字段中的模块列表（假设你存储的是 List<String>）
            Object extra = content.getExtra();
            if (extra instanceof List) {
                List<String> modules = (List<String>) extra;
                authorizedModules.clear();
                modules.forEach(m -> authorizedModules.add(m.trim()));
            }

            isLicenseValid = true;
            lastCheckTime = System.currentTimeMillis();
            log.info("License 缓存已更新，当前授权模块：{}", authorizedModules);
        } catch (Exception e) {
            isLicenseValid = false;
            authorizedModules.clear();
            log.error("License 深度校验失败，系统功能已锁定");
        }
    }

    /**
     * 刷新缓存：由定时任务或手动激活调用
     */
    public synchronized void refreshCache() {
        try {
            // 执行深度验签（耗时操作）
            LicenseContent content = licenseVerify.getLicenseManager().verify();

            // 假设 extra 中存的是模块列表
            List<String> modules = (List<String>) content.getExtra();
            moduleCache.clear();
            modules.forEach(m -> moduleCache.put(m, new Object()));
            isLicenseValid = true;
        } catch (Exception e) {
            isLicenseValid = false;
            moduleCache.clear();
        }
    }
}
