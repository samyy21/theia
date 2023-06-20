/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PayCardOptionViewBiz implements Serializable {

    private static final long serialVersionUID = 17L;

    private String payOption;
    private boolean enableStatus;
    private String disableReason;
    private String payerAccountNo;
    private String instId;
    private String instName;
    private String maskedCardNo;
    private String cardIndexNo;
    private Integer cardNoLength;
    private String cardHolderName;
    private String firstName;
    private String lastName;
    private String cardType;
    private String cardScheme;
    private boolean defaultCard;
    private String payMethod;
    private String assetType;
    private String expiryYear;
    private String expiryMonth;
    private String lastSuccessfulUsedTime;
    private Map<String, String> extendInfo;
    private String cardBin;
    private List<String> assetSubTypes;
    private boolean isCardCoft;
    private boolean isEligibleForCoft;
    private boolean isCoftPaymentSupported;
    private String par;
    private String tokenStatus;
    private boolean isCardTokenized;
    private String fingerPrint;
    private String gcin;
    private String remainingLimit;

    public String getPayOption() {
        return payOption;
    }

    public void setPayOption(final String payOption) {
        this.payOption = payOption;
    }

    public boolean isEnableStatus() {
        return enableStatus;
    }

    public void setEnableStatus(final boolean enableStatus) {
        this.enableStatus = enableStatus;
    }

    public String getDisableReason() {
        return disableReason;
    }

    public void setDisableReason(final String disableReason) {
        this.disableReason = disableReason;
    }

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(final String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(final String instId) {
        this.instId = instId;
    }

    public String getInstName() {
        return instName;
    }

    public void setInstName(final String instName) {
        this.instName = instName;
    }

    public String getMaskedCardNo() {
        return maskedCardNo;
    }

    public void setMaskedCardNo(final String maskedCardNo) {
        this.maskedCardNo = maskedCardNo;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(final String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public Integer getCardNoLength() {
        return cardNoLength;
    }

    public void setCardNoLength(final Integer cardNoLength) {
        this.cardNoLength = cardNoLength;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(final String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(final String cardType) {
        this.cardType = cardType;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(final String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public boolean isDefaultCard() {
        return defaultCard;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public void setDefaultCard(final boolean defaultCard) {
        this.defaultCard = defaultCard;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getLastSuccessfulUsedTime() {
        return lastSuccessfulUsedTime;
    }

    public void setLastSuccessfulUsedTime(String lastSuccessfulUsedTime) {
        this.lastSuccessfulUsedTime = lastSuccessfulUsedTime;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getCardBin() {
        return cardBin;
    }

    public void setCardBin(String cardBin) {
        this.cardBin = cardBin;
    }

    public List<String> getAssetSubTypes() {
        return assetSubTypes;
    }

    public void setAssetSubTypes(List<String> assetSubTypes) {
        this.assetSubTypes = assetSubTypes;
    }

    public boolean isCardCoft() {
        return isCardCoft;
    }

    public void setCardCoft(boolean cardCoft) {
        isCardCoft = cardCoft;
    }

    public boolean isEligibleForCoft() {
        return isEligibleForCoft;
    }

    public void setEligibleForCoft(boolean eligibleForCoft) {
        isEligibleForCoft = eligibleForCoft;
    }

    public boolean isCoftPaymentSupported() {
        return isCoftPaymentSupported;
    }

    public void setCoftPaymentSupported(boolean coftPaymentSupported) {
        isCoftPaymentSupported = coftPaymentSupported;
    }

    public boolean isCardTokenized() {
        return isCardTokenized;
    }

    public void setCardTokenized(boolean cardTokenized) {
        isCardTokenized = cardTokenized;
    }

    public String getPar() {
        return par;
    }

    public void setPar(String par) {
        this.par = par;
    }

    public String getTokenStatus() {
        return tokenStatus;
    }

    public void setTokenStatus(String tokenStatus) {
        this.tokenStatus = tokenStatus;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public String getGcin() {
        return gcin;
    }

    public void setGcin(String gcin) {
        this.gcin = gcin;
    }

    public String getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(String remainingLimit) {
        this.remainingLimit = remainingLimit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PayCardOptionViewBiz [payOption=").append(payOption).append(", payMethod=")
                .append(", enableStatus=").append(enableStatus).append(", disableReason=").append(disableReason)
                .append(", payerAccountNo=").append(payerAccountNo).append(", instId=").append(instId)
                .append(", instName=").append(instName).append(", maskedCardNo=").append(maskedCardNo)
                .append(", cardIndexNo=").append(cardIndexNo).append(", cardNoLength=").append(cardNoLength)
                .append(", cardHolderName=").append(cardHolderName).append(", firstName=").append(firstName)
                .append(", lastName=").append(lastName).append(", cardType=").append(cardType).append(", cardScheme=")
                .append(cardScheme).append(", defaultCard=").append(defaultCard).append(", assetSubTypes=")
                .append(assetSubTypes).append(", isCardCoft=").append(isCardCoft).append(", isEligibleForCoft=")
                .append(isEligibleForCoft).append(", isCoftPaymentSupported=").append(isCoftPaymentSupported)
                .append(", isCardTokenized=").append(isCardTokenized).append(", par=").append(par)
                .append(", tokenStatus=").append(tokenStatus).append(", extendInfo=").append(extendInfo)
                .append(", fingerPrint=").append(fingerPrint).append(", remainingLimit=").append(remainingLimit)
                .append("]");
        return builder.toString();
    }

}
