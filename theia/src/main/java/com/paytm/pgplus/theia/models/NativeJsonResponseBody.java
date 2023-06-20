package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.common.bankForm.model.RiskContent;
import com.paytm.pgplus.common.model.UpiLiteResponseData;
import com.paytm.pgplus.payloadvault.merchant.status.response.BankResultInfo;
import com.paytm.pgplus.payloadvault.theia.response.RetryInfo;
import com.paytm.pgplus.response.ResultInfo;

import java.io.Serializable;
import java.util.Map;

public class NativeJsonResponseBody implements Serializable {

    private final static long serialVersionUID = -1877677097867520196L;

    private ResultInfo resultInfo;
    private Map<String, String> txnInfo;
    private String callBackUrl;
    private Object bankForm;
    private Map<String, String> deepLinkInfo;
    private Boolean resendRetry;
    private Map<String, String> oneClickInfo;
    private RetryInfo retryInfo;
    private RiskContent riskContent;
    private BankResultInfo bankResultInfo;
    private String declineReason;
    private UpiLiteResponseData upiLiteResponseData;

    public UpiLiteResponseData getUpiLiteResponseData() {
        return upiLiteResponseData;
    }

    public void setUpiLiteResponseData(UpiLiteResponseData upiLiteResponseData) {
        this.upiLiteResponseData = upiLiteResponseData;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> displayField;
    /*
     * additionalInfo introduced for AOA as of now, can be utilized in future to
     * populate additional information required along with bankform in response
     * in general
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> additionalInfo;

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

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public Object getBankForm() {
        return bankForm;
    }

    public void setBankForm(Object bankForm) {
        this.bankForm = bankForm;
    }

    public Map<String, String> getDeepLinkInfo() {
        return deepLinkInfo;
    }

    public void setDeepLinkInfo(Map<String, String> deepLinkInfo) {
        this.deepLinkInfo = deepLinkInfo;
    }

    public Boolean getResendRetry() {
        return resendRetry;
    }

    public Map<String, String> getOneClickInfo() {
        return oneClickInfo;
    }

    public void setOneClickInfo(Map<String, String> oneClickInfo) {
        this.oneClickInfo = oneClickInfo;
    }

    public void setResendRetry(Boolean resendRetry) {
        this.resendRetry = resendRetry;
    }

    public RetryInfo getRetryInfo() {
        return retryInfo;
    }

    public void setRetryInfo(RetryInfo retryInfo) {
        this.retryInfo = retryInfo;
    }

    public RiskContent getRiskContent() {
        return riskContent;
    }

    public void setRiskContent(RiskContent riskContent) {
        this.riskContent = riskContent;
    }

    public Map<String, String> getDisplayField() {
        return displayField;
    }

    public void setDisplayField(Map<String, String> displayField) {
        this.displayField = displayField;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public BankResultInfo getBankResultInfo() {
        return bankResultInfo;
    }

    public void setBankResultInfo(BankResultInfo bankResultInfo) {
        this.bankResultInfo = bankResultInfo;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    @Override
    public String toString() {
        return "NativeJsonResponseBody{" + "resultInfo=" + resultInfo + ", bankResultInfo=" + bankResultInfo
                + ", txnInfo=" + txnInfo + ", callBackUrl='" + callBackUrl + '\'' + ", deepLinkInfo=" + deepLinkInfo
                + ", resendRetry=" + resendRetry + ", retryInfo=" + retryInfo + ", declineReason=" + declineReason
                + '}';
    }
}
