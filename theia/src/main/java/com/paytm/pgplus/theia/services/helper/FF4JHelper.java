package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.theia.services.impl.CloseOrderServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("fF4JHelper")
public class FF4JHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FF4JHelper.class);

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    public boolean isFF4JFeatureForMidEnabled(String featureName, String mid) {

        if (StringUtils.isBlank(mid)) {
            return Boolean.FALSE;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("mid", mid);

        // If mid is present in FF4J DB then feature is enabled otherwise it
        // is disabled
        if (iPgpFf4jClient.checkWithdefault(featureName, context, false)) {
            LOGGER.info("Feature " + featureName + " is enabled for mid: {}", mid);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
