package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.List;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;

public class BizConsultFeeRequestBean implements Serializable {

    private static final long serialVersionUID = 6845900820178888258L;

    private String txnAmount;

    private String merchantID;

    private ERequestType transType;

    private List<EPayMethod> payMethods;

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public ERequestType getTransType() {
        return transType;
    }

    public void setTransType(ERequestType transType) {
        this.transType = transType;
    }

    public List<EPayMethod> getPayMethods() {
        return payMethods;
    }

    public void setPayMethods(List<EPayMethod> payMethods) {
        this.payMethods = payMethods;
    }

}
