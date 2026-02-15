package com.ice.framework.license;

import de.schlichtherle.license.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * License校验类
 */
@Slf4j
public class LicenseVerify {

    /**
     * 证书subject
     */
    private final String subject;
    /**
     * 公钥别称
     */
    private final String publicAlias;
    /**
     * 访问公钥库的密码
     */
    private final String storePass;
    /**
     * 证书生成路径
     */
    private final String licensePath;
    /**
     * 密钥库存储路径
     */
    private final String publicKeysStorePath;
    /**
     * LicenseManager
     */
    @Getter
    private LicenseManager licenseManager;
    /**
     * 证书是否安装成功标记
     */
    private boolean installSuccess;

    /**
     * 缓存授权模块列表
     */
    @Getter
    private Set<String> cachedModules = new HashSet<>();

    public LicenseVerify(String subject, String publicAlias, String storePass, String licensePath, String publicKeysStorePath) {
        this.subject = subject;
        this.publicAlias = publicAlias;
        this.storePass = storePass;
        this.licensePath = licensePath;
        this.publicKeysStorePath = publicKeysStorePath;
    }

    public synchronized void installLicense() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(LicenseVerify.class);

            CipherParam cipherParam = new DefaultCipherParam(storePass);

            //设置密钥库参数
            KeyStoreParam publicStoreParam = new CustomKeyStoreParam(
                    LicenseVerify.class,
                    publicKeysStorePath,
                    publicAlias,
                    storePass,
                    null);
            LicenseParam licenseParam = new DefaultLicenseParam(
                    subject,
                    preferences,
                    publicStoreParam,
                    cipherParam);

            licenseManager = new CustomLicenseManager(licenseParam);
            licenseManager.uninstall();
            LicenseContent licenseContent = licenseManager.install(new File(licensePath));
            extractModules(licenseContent);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            installSuccess = true;

            log.info("------------------------------- 证书安装成功 -------------------------------");
            logLicenseInfo();
            log.info(MessageFormat.format("证书校验通过，证书有效期：{0} - {1}", format.format(licenseContent.getNotBefore()), format.format(licenseContent.getNotAfter())));
        } catch (Exception e) {
            installSuccess = false;
            log.error("------------------------------- 证书安装失败 -------------------------------");
            log.error(e.getMessage(), e);
        }
    }


    public synchronized void unInstallLicense() {
        if (installSuccess) {
            try {
                licenseManager.uninstall();
                log.info("------------------------------- 证书卸载成功 -------------------------------");
            } catch (Exception e) {
                log.error("------------------------------- 证书卸载失败 -------------------------------");
                log.error(e.getMessage(), e);
            }
        }
    }

    public synchronized LicenseCheckResult verify() {
        LicenseCheckResult result = new LicenseCheckResult();
        try {
            if (licenseManager == null) {
                log.error("LicenseManager未初始化，License验证失败");
                result.setValid(false);
                result.setMsg("LicenseManager未初始化，License验证失败");
                return result;
            }
            LicenseContent licenseContent = licenseManager.verify();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info("证书有效期：{} - {}", format.format(licenseContent.getNotBefore()), format.format(licenseContent.getNotAfter()));
            result.setContent(licenseContent);
            result.setValid(true);

            if(validateExpiry(licenseContent)){
                result.setValid(false);
                result.setMsg("License已过期");
                return result;
            }

            return result;
        } catch (Exception e) {
            log.error("证书校验失败{}", e.getMessage(), e);
            return result;
        }
    }

    /**
     * 验证指定功能模块是否已授权
     */
    public synchronized boolean verifyModule(String moduleName) {
        try {
            if (!verify().isValid()) {
                log.error("License验证失败，模块 {} 无法使用", moduleName);
                return false;
            }

            LicenseContent licenseContent = licenseManager.verify();
            LicenseCheckModel checkModel = (LicenseCheckModel) licenseContent.getExtra();

            if (checkModel == null || checkModel.getAuthorizedModules() == null) {
                log.warn("License中未设置功能模块授权信息，默认允许所有模块");
                return true;
            }

            List<String> authorizedModules = checkModel.getAuthorizedModules();
            boolean authorized = authorizedModules.contains(moduleName);

            if (authorized) {
                log.info("模块 {} 已授权", moduleName);
            } else {
                log.warn("模块 {} 未授权", moduleName);
            }

            return authorized;

        } catch (Exception e) {
            log.error("验证模块授权失败: {}", e.getMessage());
            return false;
        }
    }
    /**
     * 校验licese是否过有效期
     */
    private boolean validateExpiry( LicenseContent licenseContent){
        Date now = new Date();
        return !now.before(licenseContent.getNotBefore()) &&
                !now.after(licenseContent.getNotAfter());
    }

    private boolean validateHardware( LicenseContent licenseContent){
                return  true;
    }

    /**
     * 提取授权的模块
     */
    private void extractModules(LicenseContent content) {
        if (content != null) {
            LicenseCheckModel licenseCheckModel = (LicenseCheckModel)content.getExtra();
            List<String> moduleList = licenseCheckModel.getAuthorizedModules();
            this.cachedModules.addAll(moduleList);
            log.info("检测到授权模块: {}", this.cachedModules);
        } else {
            this.cachedModules = Collections.emptySet();
            log.warn("未发现模块授权信息");
        }
    }


    /**
     * 打印License详细信息
     */
    private void logLicenseInfo() {
        try {
            LicenseContent content = licenseManager.verify();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            log.info("----------------------------------------");
            log.info("证书主题: {}", content.getSubject());
            log.info("证书持有人: {}", content.getHolder());
            log.info("证书颁发者: {}", content.getIssuer());
            log.info("生效时间: {}", format.format(content.getNotBefore()));
            log.info("失效时间: {}", format.format(content.getNotAfter()));
            log.info("用户类型: {}", content.getConsumerType());
            log.info("用户数量: {}", content.getConsumerAmount());
            log.info("证书描述: {}", content.getInfo());

            LicenseCheckModel checkModel = (LicenseCheckModel) content.getExtra();
            if (checkModel != null) {
                log.info("--- 额外授权信息 ---");
                if (checkModel.getIpAddress() != null) {
                    log.info("授权IP: {}", checkModel.getIpAddress());
                }
                if (checkModel.getMacAddress() != null) {
                    log.info("授权MAC: {}", checkModel.getMacAddress());
                }
                if (checkModel.getAuthorizedModules() != null) {
                    log.info("授权功能模块: {}", checkModel.getAuthorizedModules());
                }
                if (checkModel.getMaxUserCount() != null) {
                    log.info("最大用户数: {}", checkModel.getMaxUserCount());
                }
                if (checkModel.getCompanyName() != null) {
                    log.info("客户公司: {}", checkModel.getCompanyName());
                }
            }
            log.info("----------------------------------------");

        } catch (Exception e) {
            log.error("打印License信息失败", e);
        }

    }
    /**
     * 获取License信息
     */
    public LicenseContent getLicenseInfo () {
        try {
            if (licenseManager != null) {
                return licenseManager.verify();
            }
        } catch (Exception e) {
            log.error("获取License信息失败", e);
        }
        return null;
    }
}
