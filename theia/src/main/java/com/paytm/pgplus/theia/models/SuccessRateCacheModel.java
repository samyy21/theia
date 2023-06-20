package com.paytm.pgplus.theia.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kartik
 * @date 09-03-2017
 */
public class SuccessRateCacheModel implements Serializable {
    private static final long serialVersionUID = -6354666081851396660L;
    private Map<String, String> successRatesMap;

    public SuccessRateCacheModel() {
        successRatesMap = new HashMap<String, String>();
    }

    public SuccessRateCacheModel(Map<String, String> successRatesMap) {
        this.successRatesMap = successRatesMap;
    }

    public Map<String, String> getSuccessRatesMap() {
        return successRatesMap;
    }

    public void setSuccessRatesMap(Map<String, String> successRatesMap) {
        this.successRatesMap = successRatesMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SuccessRateCacheModel [successRatesMap=");
        builder.append(successRatesMap);
        builder.append("]");
        return builder.toString();
    }
}
