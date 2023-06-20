package com.paytm.pgplus.theia.sessiondata;

import com.dyuproject.protostuff.Tag;

import java.io.Serializable;

/**
 * @author manojpal
 *
 */
public class RetryPaymentInfo implements Serializable {

    private static final long serialVersionUID = -8307965049652467727L;

    @Tag(value = 1)
    private String retryCardNumber;
    @Tag(value = 2)
    private String paymentMode;
    @Tag(value = 3)
    private String cardNumberWithoutFormatting;
    @Tag(value = 4)
    private String errorMessage;
    @Tag(value = 5)
    private String emiPlanId;
    @Tag(value = 6)
    private String emiBankName;

    public String getEmiPlanId() {
        return emiPlanId;
    }

    public void setEmiPlanId(String emiPlanId) {
        this.emiPlanId = emiPlanId;
    }

    public String getEmiBankName() {
        return emiBankName;
    }

    public void setEmiBankName(String emiBankName) {
        this.emiBankName = emiBankName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCardNumberWithoutFormatting() {
        return cardNumberWithoutFormatting;
    }

    public void setCardNumberWithoutFormatting(String cardNumberWithoutFormatting) {
        this.cardNumberWithoutFormatting = cardNumberWithoutFormatting;
    }

    public String getRetryCardNumber() {
        return retryCardNumber;
    }

    public void setRetryCardNumber(String retryCardNumber) {
        this.retryCardNumber = retryCardNumber;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RetryPaymentInfo [retryCardNumber=").append(retryCardNumber).append(", paymentMode=")
                .append(paymentMode).append(", cardNumberWithoutFormatting=").append(cardNumberWithoutFormatting)
                .append(", errorMessage=").append(errorMessage).append(", emiPlanId=").append(emiPlanId)
                .append(", emiBankName=").append(emiBankName).append("]");
        return builder.toString();
    }

}
