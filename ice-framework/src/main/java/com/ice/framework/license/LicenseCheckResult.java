package com.ice.framework.license;

import de.schlichtherle.license.LicenseContent;
import lombok.Data;

import java.io.Serializable;

@Data
public class LicenseCheckResult implements Serializable {

    LicenseContent content;

    boolean valid;

    String msg;
}
