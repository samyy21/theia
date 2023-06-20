package com.paytm.pgplus.theia.nativ.model.transactionStatus;

import com.paytm.pgplus.response.ResultInfo;

import java.io.Serializable;
import java.util.Map;

public class TransactionStatusResponseBody implements Serializable {

    private final static long serialVersionUID = 7756999814268002474L;
    private ResultInfo resultInfo;
    private Map<String, String> txnInfo;
    private String callbackUrl;
    private boolean isPollingRequired;
    private String declineReason;

    public boolean getPollingRequired() {
        return isPollingRequired;
    }

    public void setPollingRequired(boolean pollingRequired) {
        isPollingRequired = pollingRequired;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public Map<String, String> getTxnInfo() {
        return txnInfo;
    }

    public void setTxnInfo(Map<String, String> txnInfo) {
        this.txnInfo = txnInfo;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionStatusResponseBody{");
        sb.append("resultInfo=").append(resultInfo);
        sb.append(", txnInfo=").append(txnInfo);
        sb.append(", callbackUrl='").append(callbackUrl);
        sb.append(", declineReason='").append(declineReason).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
