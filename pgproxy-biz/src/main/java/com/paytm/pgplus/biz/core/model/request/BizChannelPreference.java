package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BizChannelPreference implements Serializable {

    private static final long serialVersionUID = 8786280686723528073L;

    private List<BizPreferenceValue> preferenceValues;

    private Map<String, String> extendInfo;

    public List<BizPreferenceValue> getPreferenceValues() {
        return preferenceValues;
    }

    public void setPreferenceValues(List<BizPreferenceValue> preferenceValues) {
        this.preferenceValues = preferenceValues;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizChannelPreference{");
        sb.append("preferenceValues=").append(preferenceValues);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append('}');
        return sb.toString();
    }
}
