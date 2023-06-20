package com.paytm.pgplus.theia.nativ.model.payview.response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.util.Arrays;

/**
 * @author mohit.gupta
 *
 */

public class StatusInfoFilter {

    public StatusInfoFilter() {
    }

    /*
     * Filter is used to stop serialization when "status" is false
     */
    @Override
    public boolean equals(Object obj) {
        String version = MDC.get("version");
        String allowedVersions[] = com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(
                com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.FPO_ALLOWED_VERSIONS).split(
                ",");
        boolean isVersionAllowedInFPO = Arrays.stream(allowedVersions).anyMatch(
                n -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(version, n));
        if (isVersionAllowedInFPO || StringUtils.equals("v4", version)) {
            if (obj == null || !(obj instanceof StatusInfo)) {
                return true;
            }
            StatusInfo status = (StatusInfo) obj;
            return !Boolean.parseBoolean(status.getStatus());
        } else {
            return false;
        }
    }
}
