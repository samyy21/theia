package com.paytm.pgplus.biz.core.model.oauth;

import java.io.Serializable;

public class VerifyLoginRequestBizBean implements Serializable {

    private static final long serialVersionUID = 3L;

    private String oAuthCode;

    private String clientID;

    private String secretKey;

    private String custId;

    private String mId;

    private boolean isStoreCardPrefEnabled;

    public String getoAuthCode() {
        return oAuthCode;
    }

    public void setoAuthCode(String oAuthCode) {
        this.oAuthCode = oAuthCode;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public VerifyLoginRequestBizBean(String oAuthCode) {
        this.oAuthCode = oAuthCode;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public boolean isStoreCardPrefEnabled() {
        return isStoreCardPrefEnabled;
    }

    public void setStoreCardPrefEnabled(boolean isStoreCardPrefEnabled) {
        this.isStoreCardPrefEnabled = isStoreCardPrefEnabled;
    }

    public VerifyLoginRequestBizBean() {

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerifyLoginRequestBizBean [oAuthCode=").append(oAuthCode).append(", clientID=")
                .append(clientID).append(", secretKey=").append(secretKey).append(", custId=").append(custId)
                .append(", mId=").append(mId).append(", isStoreCardPrefEnabled=").append(isStoreCardPrefEnabled)
                .append("]");
        return builder.toString();
    }

}
