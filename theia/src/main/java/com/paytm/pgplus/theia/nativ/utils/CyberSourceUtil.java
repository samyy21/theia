package com.paytm.pgplus.theia.nativ.utils;

import java.util.Properties;

import com.paytm.pgplus.theia.services.impl.VisaCyberSourceServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CyberSource Util class
 */
public class CyberSourceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CyberSourceUtil.class);

    public static Properties getStatusQryProps(String mbid, String keysDirectory, String timeout, String environment) {

        Properties props = new Properties();
        if (StringUtils.isNotBlank(mbid) && StringUtils.isNotBlank(keysDirectory) && StringUtils.isNotBlank(timeout)
                && StringUtils.isNotBlank(environment)) {
            // HTTP_Signature = http_signature and JWT = jwt
            props.setProperty("authenticationType", "jwt");
            props.setProperty("merchantID", mbid);

            props.setProperty("runEnvironment", environment);

            // JWT Parameters
            props.setProperty("keyAlias", mbid);
            props.setProperty("keyPass", mbid);
            props.setProperty("keyFileName", mbid);

            // P12 key path. Enter the folder path where the .p12 file is
            // located.
            props.setProperty("keysDirectory", keysDirectory);

            // Logging to be enabled or not.
            props.setProperty("enableLog", "false");

            // Timeout
            props.setProperty("timeout", timeout);
        }
        LOGGER.info("map for merchant configuration for cyber source API {}", props);
        return props;
    }

}
