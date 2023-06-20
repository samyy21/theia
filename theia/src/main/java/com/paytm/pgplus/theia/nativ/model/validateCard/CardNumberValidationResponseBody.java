package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.response.BaseResponseBody;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardNumberValidationResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 1901770237796274801L;

    private String cardScheme;

    private String bankName;

    private String performanceStatus;

    private String paymentMode;

    private String iconUrl;

    private String operationCode;

    private String reason;

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getPerformanceStatus() {
        return performanceStatus;
    }

    public void setPerformanceStatus(String performanceStatus) {
        this.performanceStatus = performanceStatus;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
