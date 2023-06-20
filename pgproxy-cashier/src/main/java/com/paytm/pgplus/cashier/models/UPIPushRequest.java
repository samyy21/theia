package com.paytm.pgplus.cashier.models;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.paytm.pgplus.facade.user.models.SarvatraVpaDetails;

/**
 * @author Santosh Chourasia
 *
 */
public class UPIPushRequest implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6401659329846971096L;

    private boolean upiPushTxn;
    private SarvatraVpaDetails sarvatraVpaDetails;
    private String mpin;
    private String deviceId;
    private String mobile;
    private String seqNo;
    private String orderId;
    private boolean upiPushExpressSupported;
    private String appId;

    public SarvatraVpaDetails getSarvatraVpaDetails() {
        return sarvatraVpaDetails;
    }

    public void setSarvatraVpaDetails(SarvatraVpaDetails sarvatraVpaDetails) {
        this.sarvatraVpaDetails = sarvatraVpaDetails;
    }

    public boolean isUpiPushTxn() {
        return upiPushTxn;
    }

    public void setUpiPushTxn(boolean upiPushTxn) {
        this.upiPushTxn = upiPushTxn;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isUpiPushExpressSupported() {
        return upiPushExpressSupported;
    }

    public void setUpiPushExpressSupported(boolean upiPushExpressSupported) {
        this.upiPushExpressSupported = upiPushExpressSupported;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UPIPushRequest [upiPushTxn=").append(upiPushTxn).append(", sarvatraVpaDetails=")
                .append(sarvatraVpaDetails).append(", mpin=").append(mpin).append(", deviceId=").append(deviceId)
                .append(", mobile=").append(mobile).append(", seqNo=").append(seqNo).append(", orderId=")
                .append(orderId).append(", upiPushExpressSupported=").append(upiPushExpressSupported)
                .append(", appId=").append(appId).append("]");
        return builder.toString();
    }

}
