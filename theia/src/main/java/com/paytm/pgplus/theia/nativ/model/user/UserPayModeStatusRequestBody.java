package com.paytm.pgplus.theia.nativ.model.user;

import java.io.Serializable;
import java.util.List;

public class UserPayModeStatusRequestBody implements Serializable {

    private static final long serialVersionUID = 3596230140000954372L;

    private String mid;
    private String mobileNo;
    private String userId;
    private List<String> paymentMode;
    private String referenceId;
    private String txnAmount;

    public UserPayModeStatusRequestBody() {
    }

    public UserPayModeStatusRequestBody(String mid, String mobileNo, String userId, List<String> paymentMode,
            String referenceId, String txnAmount) {
        this.mid = mid;
        this.mobileNo = mobileNo;
        this.userId = userId;
        this.paymentMode = paymentMode;
        this.referenceId = referenceId;
        this.txnAmount = txnAmount;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(List<String> paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserPayModeStatusRequestBody{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", mobileNo='").append(mobileNo).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", paymentMode=").append(paymentMode);
        sb.append(", referenceId='").append(referenceId).append('\'');
        sb.append(", txnAmount='").append(txnAmount).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
