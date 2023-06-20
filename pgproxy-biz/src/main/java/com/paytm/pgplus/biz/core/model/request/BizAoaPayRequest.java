package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETransType;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.payment.models.ChannelPreference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BizAoaPayRequest implements Serializable {

    private static final long serialVersionUID = 3639010592326277671L;

    private String transID;

    private List<BizAoaPayOptionBill> aoaPayOptionBills;

    private EnvInfo envInfo;

    private String requestID;

    private Map<String, String> extInfo;

    private BizChannelPreference bizChannelPreference;

    public BizAoaPayRequest(String transID, List<BizAoaPayOptionBill> aoaPayOptionBills, String requestID,
            Map<String, String> extInfo, BizChannelPreference bizChannelPreference) {
        this.transID = transID;
        this.aoaPayOptionBills = aoaPayOptionBills;
        this.requestID = requestID;
        this.extInfo = extInfo;
        this.bizChannelPreference = bizChannelPreference;
    }

    public String getTransID() {
        return transID;
    }

    public void setTransID(String transID) {
        this.transID = transID;
    }

    public List<BizAoaPayOptionBill> getPayOptionBills() {
        return aoaPayOptionBills;
    }

    public void setPayOptionBills(List<BizAoaPayOptionBill> aoaPayOptionBills) {
        this.aoaPayOptionBills = aoaPayOptionBills;
    }

    public EnvInfo getEnvInfo() {
        return envInfo;
    }

    public void setEnvInfo(EnvInfo envInfo) {
        this.envInfo = envInfo;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public Map<String, String> getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Map<String, String> extInfo) {
        this.extInfo = extInfo;
    }

    public BizChannelPreference getChannelPreference() {
        return bizChannelPreference;
    }

    public void setChannelPreference(BizChannelPreference bizChannelPreference) {
        this.bizChannelPreference = bizChannelPreference;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizAoaPayRequest{");
        sb.append("transID='").append(transID).append('\'');
        sb.append(", payOptionBills=").append(aoaPayOptionBills);
        sb.append(", envInfo=").append(envInfo);
        sb.append(", requestID='").append(requestID).append('\'');
        sb.append(", extInfo=").append(extInfo);
        sb.append(", bizChannelPreference=").append(bizChannelPreference);
        sb.append('}');
        return sb.toString();
    }
}