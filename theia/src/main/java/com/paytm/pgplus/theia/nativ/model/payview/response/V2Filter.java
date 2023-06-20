package com.paytm.pgplus.theia.nativ.model.payview.response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

/**
 * @author mohit.gupta
 *
 */

public class V2Filter {

    public V2Filter() {
    }

    /*
     * Filter is used to stop serialization when "version" is v1
     */
    @Override
    public boolean equals(Object obj) {
        String version = MDC.get("version");

        return (obj != null) || !(StringUtils.equals("v2", version) || StringUtils.equals("v4", version));
    }
}
