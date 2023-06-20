package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author vivek kumar
 *
 */
public class UPIPushInitiateRequestBean implements Serializable {

    private static final long serialVersionUID = 3862681468653444472L;

    private UPIPushTxnRequestParams upiPushTxnRequestParams;
    private String requestUrl;
    private String externalSrNo;
    private String amount;

    public UPIPushInitiateRequestBean(UPIPushTxnRequestParams upiPushTxnRequestParams, String requestUrl,
            String externalSrNo, String amount) {
        this.upiPushTxnRequestParams = upiPushTxnRequestParams;
        this.requestUrl = requestUrl;
        this.externalSrNo = externalSrNo;
        this.amount = amount;
    }

    public UPIPushInitiateRequestBean(UPIPushTxnRequestParams upiPushTxnRequestParams, String amount) {
        this.upiPushTxnRequestParams = upiPushTxnRequestParams;
        this.amount = amount;
    }

    public UPIPushTxnRequestParams getUpiPushTransactionParams() {
        return upiPushTxnRequestParams;
    }

    public void setUpiPushTransactionParams(UPIPushTxnRequestParams upiPushTransactionParams) {
        this.upiPushTxnRequestParams = upiPushTransactionParams;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getExternalSrNo() {
        return externalSrNo;
    }

    public void setExternalSrNo(String externalSrNo) {
        this.externalSrNo = externalSrNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UPIPushInitiateRequestBean [upiPushTransactionParams=").append(upiPushTxnRequestParams)
                .append(", requestUrl=").append(requestUrl).append(", externalSrNo=").append(externalSrNo)
                .append(", amount=").append(amount).append("]");
        return builder.toString();
    }

}
