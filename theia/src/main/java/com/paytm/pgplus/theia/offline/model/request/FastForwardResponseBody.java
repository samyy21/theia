package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.payloadvault.theia.response.MerchantInfo;
import com.paytm.pgplus.payloadvault.theia.response.RetryInfo;
import com.paytm.pgplus.theia.offline.model.response.ResponseBody;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FastForwardResponseBody extends ResponseBody {

    private static final long serialVersionUID = -3271229420660687189L;

    private String txnId;
    private String orderId;
    private String txnAmount;
    private String responseCode;
    private String responseMessage;
    private String status;
    private String paymentMode;
    private String bankName;
    private String custId;
    private String prn;
    private MerchantInfo merchantInfo;
    private String bankTxnId;
    private RetryInfo retryInfo;
    private String chargeAmount;
    private String customPaymentFailureMessage;
    private String customPaymentSuccessMessage;
    private String redirectionUrlSuccess;
    private String redirectionUrlFailure;

    public String getCustomPaymentFailureMessage() {
        return customPaymentFailureMessage;
    }

    public void setCustomPaymentFailureMessage(String customPaymentFailureMessage) {
        this.customPaymentFailureMessage = customPaymentFailureMessage;
    }

    public String getCustomPaymentSuccessMessage() {
        return customPaymentSuccessMessage;
    }

    public void setCustomPaymentSuccessMessage(String customPaymentSuccessMessage) {
        this.customPaymentSuccessMessage = customPaymentSuccessMessage;
    }

    public String getRedirectionUrlSuccess() {
        return redirectionUrlSuccess;
    }

    public void setRedirectionUrlSuccess(String redirectionUrlSuccess) {
        this.redirectionUrlSuccess = redirectionUrlSuccess;
    }

    public String getRedirectionUrlFailure() {
        return redirectionUrlFailure;
    }

    public void setRedirectionUrlFailure(String redirectionUrlFailure) {
        this.redirectionUrlFailure = redirectionUrlFailure;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public MerchantInfo getMerchantInfo() {
        return merchantInfo;
    }

    public void setMerchantInfo(MerchantInfo merchantInfo) {
        this.merchantInfo = merchantInfo;
    }

    public String getBankTxnId() {
        return bankTxnId;
    }

    public void setBankTxnId(String bankTxnId) {
        this.bankTxnId = bankTxnId;
    }

    public RetryInfo getRetryInfo() {
        return retryInfo;
    }

    public void setRetryInfo(RetryInfo retryInfo) {
        this.retryInfo = retryInfo;
    }

    public String getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(String chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FastForwardResponseBody{");
        sb.append("txnId='").append(txnId).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", txnAmount='").append(txnAmount).append('\'');
        sb.append(", responseCode='").append(responseCode).append('\'');
        sb.append(", responseMessage='").append(responseMessage).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", paymentMode='").append(paymentMode).append('\'');
        sb.append(", bankName='").append(bankName).append('\'');
        sb.append(", custId='").append(custId).append('\'');
        sb.append(", prn='").append(prn).append('\'');
        sb.append(", merchantInfo=").append(merchantInfo);
        sb.append(", bankTxnId=").append(bankTxnId);
        sb.append(", retryInfo=").append(retryInfo);
        sb.append(", chargeAmount=").append(chargeAmount);
        sb.append('}');
        return sb.toString();
    }
}
