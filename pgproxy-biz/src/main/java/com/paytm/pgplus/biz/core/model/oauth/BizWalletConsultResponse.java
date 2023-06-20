package com.paytm.pgplus.biz.core.model.oauth;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BizWalletConsultResponse implements Serializable {

    private static final long serialVersionUID = 742292480193510L;

    private String statusCode;
    private boolean limitApplicable;
    private String addMoneyDestination;
    private String walletRbiType;
    private String trustFactor;
    private List<Map<String, Object>> feeDetails;
    private String limitMessage;
    private String message;

    public BizWalletConsultResponse() {
    }

    public BizWalletConsultResponse(String statusCode) {
        this.statusCode = statusCode;
    }

    public BizWalletConsultResponse(String statusCode, boolean limitApplicable, String addMoneyDestination,
            String walletRbiType) {
        this.statusCode = statusCode;
        this.limitApplicable = limitApplicable;
        this.addMoneyDestination = addMoneyDestination;
        this.walletRbiType = walletRbiType;
    }

    public String getAddMoneyDestination() {
        return addMoneyDestination;
    }

    public void setAddMoneyDestination(String addMoneyDestination) {
        this.addMoneyDestination = addMoneyDestination;
    }

    public boolean isLimitApplicable() {
        return limitApplicable;
    }

    public void setLimitApplicable(boolean limitApplicable) {
        this.limitApplicable = limitApplicable;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getWalletRbiType() {
        return walletRbiType;
    }

    public String getTrustFactor() {
        return trustFactor;
    }

    public void setTrustFactor(String trustFactor) {
        this.trustFactor = trustFactor;
    }

    public BizWalletConsultResponse setWalletRbiType(String walletRbiType) {
        this.walletRbiType = walletRbiType;
        return this;
    }

    public List<Map<String, Object>> getFeeDetails() {
        return feeDetails;
    }

    public void setFeeDetails(List<Map<String, Object>> feeDetails) {
        this.feeDetails = feeDetails;
    }

    public String getLimitMessage() {
        return limitMessage;
    }

    public void setLimitMessage(String limitMessage) {
        this.limitMessage = limitMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizWalletConsultResponse{");
        sb.append("addMoneyDestination='").append(addMoneyDestination).append('\'');
        sb.append(", limitApplicable=").append(limitApplicable);
        sb.append(", statusCode='").append(statusCode).append('\'');
        sb.append(", walletRbiType='").append(walletRbiType).append('\'');
        sb.append(", trustFactor='").append(trustFactor).append('\'');
        sb.append(", feeDetails='").append(feeDetails).append('\'');
        sb.append(", limitMessage='").append(limitMessage).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
