package com.paytm.pgplus.theia.enums;

import com.paytm.pgplus.facade.enums.Pg2DirectServiceUrl;
import com.paytm.pgplus.facade.utils.PropertiesUtil;
import com.paytm.pgplus.theia.models.splitSettlement.SplitSettlementRequestBody;
import com.paytm.pgplus.theia.models.splitSettlement.SplitSettlementResponseBody;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public enum AcquiringPG2ServiceUrl implements Pg2DirectServiceUrl {
    ACQUIRING_SPLIT("/acquiring/split", SplitSettlementResponseBody.class, SplitSettlementRequestBody.class);

    private static final String BASE_URL;
    private static final Logger LOGGER = LoggerFactory.getLogger(AcquiringPG2ServiceUrl.class);

    static {
        final Properties prop = PropertiesUtil.getProperties();
        BASE_URL = prop.getProperty("acq.base.url");
        if (StringUtils.isEmpty(BASE_URL)) {
            LOGGER.error("acq.base.url is not defined in properties file.");
        }
    }
    private String url;
    private Class<?> responseClass;
    private Class<?> requestClass;

    AcquiringPG2ServiceUrl(final String url, final Class<?> responseClass, final Class<?> requestClass) {
        this.url = url;
        this.responseClass = responseClass;
        this.requestClass = requestClass;
    }

    public String getFunctionUrl() {
        return url;
    }

    public String getUrl() {
        return BASE_URL + url;
    }

    @Override
    public String getName() {
        return this.name();
    }

    public Class<?> getResponseClass() {
        return responseClass;
    }

    public Class<?> getRequestClass() {
        return requestClass;
    }
}
