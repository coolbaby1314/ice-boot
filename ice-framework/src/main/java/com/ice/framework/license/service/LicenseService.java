package com.ice.framework.license.service;


import com.ice.framework.license.LicenseManagerHolder;
import com.ice.framework.license.LicenseVerify;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * license授权查验服务
 */
@Service
public class LicenseService {

    @Autowired
    LicenseVerify licenseVerify;
    // 缓存当前的授权信息，避免频繁读取磁盘
    private LicenseContent cachedLicense;



    public boolean verify(){
       return licenseVerify.verify().isValid();
    }

    /**
     * 核心校验逻辑：运行期判断
     * @param pluginKey 插件唯一标识 (如 invoice)
     * @return 是否允许运行
     */
    public boolean isPluginActive(String pluginKey) {
        // 1. 首先校验主授权文件是否合法、是否过期
        LicenseContent license = verifyLicense();
        if (license == null) return false;

        // 2. 校验该插件是否在授权列表中
        // 假设我们将允许的插件列表存放在 License 的 Extra (附加信息) 中
        @SuppressWarnings("unchecked")
        List<String> activePlugins = (List<String>) license.getExtra();

        return activePlugins != null && activePlugins.contains(pluginKey);
    }

    private LicenseContent verifyLicense() {
        if (cachedLicense != null) return cachedLicense;

        try {
            // 这里调用 TrueLicense 的验证逻辑
            // 验证：1.签名是否正确 2.是否在当前服务器(机器码) 3.是否过期
            LicenseManager manager = licenseVerify.getLicenseManager();
            cachedLicense = manager.install(new File("config/license.lic"));
            return cachedLicense;
        } catch (Exception e) {
            return null;
        }
    }
}
