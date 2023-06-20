package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by charu on 04/06/18.
 */

public class PayMethodInfo implements Serializable {

    private static final long serialVersionUID = 758058285414864751L;

    private String payMethod;

    private Map<String, String> extendInfo;

    public PayMethodInfo(String payMethod) {
        this.payMethod = payMethod;
    }

    public PayMethodInfo(String payMethod, Map<String, String> extendInfo) {
        this.payMethod = payMethod;
        this.extendInfo = extendInfo;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayMethodInfo{");
        sb.append("payMethod='").append(payMethod).append('\'');
        sb.append(", extendInfo='").append(extendInfo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
