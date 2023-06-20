package com.paytm.pgplus.theia.models.banktransfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.common.model.AccountDetails;
import com.paytm.pgplus.common.model.VanInfo;
import com.paytm.pgplus.theia.models.NativeJsonRequestBody;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalTransactionRequestBody extends NativeJsonRequestBody implements Serializable {

    private final static long serialVersionUID = 5069250610762917887L;

    /*
     * private String mid; private String orderId; private String paymentMode;
     * private String channelCode; private String payerAccount; private String
     * txnAmount; private Map<String, String> extendInfo;
     */
    private String paymentOption;
    private String orderExpiry;
    private String utr;
    private String transactionRequestId;
    private String transactionDate;
    private String transferMode;
    private VanInfo vanInfo;
    private AccountDetails sourceAccountInfo;
    private String tpvErrorCode;
    private String tpvDescription;
    private boolean isTPVFailure;
    private String feesAmount;

    private String errorCode;

    private boolean failure;

    private String errorDescription;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public boolean isFailure() {
        return failure;
    }

    public void setFailure(boolean failure) {
        this.failure = failure;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getOrderExpiry() {
        return orderExpiry;
    }

    public void setOrderExpiry(String orderExpiry) {
        this.orderExpiry = orderExpiry;
    }

    public String getUtr() {
        return utr;
    }

    public void setUtr(String utr) {
        this.utr = utr;
    }

    public String getTransactionRequestId() {
        return transactionRequestId;
    }

    public void setTransactionRequestId(String transactionRequestId) {
        this.transactionRequestId = transactionRequestId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransferMode() {
        return transferMode;
    }

    public void setTransferMode(String transferMode) {
        this.transferMode = transferMode;
    }

    public VanInfo getVanInfo() {
        return vanInfo;
    }

    public void setVanInfo(VanInfo vanInfo) {
        this.vanInfo = vanInfo;
    }

    public AccountDetails getSourceAccountInfo() {
        return sourceAccountInfo;
    }

    public void setSourceAccountInfo(AccountDetails sourceAccountInfo) {
        this.sourceAccountInfo = sourceAccountInfo;
    }

    public String getTpvErrorCode() {
        return tpvErrorCode;
    }

    public void setTpvErrorCode(String tpvErrorCode) {
        this.tpvErrorCode = tpvErrorCode;
    }

    public String getTpvDescription() {
        return tpvDescription;
    }

    public void setTpvDescription(String tpvDescription) {
        this.tpvDescription = tpvDescription;
    }

    public boolean isTPVFailure() {
        return isTPVFailure;
    }

    public void setTPVFailure(boolean TPVFailure) {
        isTPVFailure = TPVFailure;
    }

    public String getFeesAmount() {
        return feesAmount;
    }

    public void setFeesAmount(String feesAmount) {
        this.feesAmount = feesAmount;
    }

    public InternalTransactionRequestBody() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InternalTransactionRequestBody{");
        sb.append("paymentOption='").append(paymentOption).append('\'');
        sb.append(", orderExpiry='").append(orderExpiry).append('\'');
        sb.append(", utr='").append(utr).append('\'');
        sb.append(", transactionRequestId='").append(transactionRequestId).append('\'');
        sb.append(", transactionDate='").append(transactionDate).append('\'');
        sb.append(", transferMode='").append(transferMode).append('\'');
        sb.append(", vanInfo=").append(vanInfo);
        sb.append(", sourceAccountInfo=").append(sourceAccountInfo);
        sb.append(", tpvErrorCode='").append(tpvErrorCode).append('\'');
        sb.append(", tpvDescription='").append(tpvDescription).append('\'');
        sb.append(", isTPVFailure=").append(isTPVFailure).append('\'');
        sb.append(", feesAmount=").append(feesAmount);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}