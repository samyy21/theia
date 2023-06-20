/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinData implements Serializable {

    private static final long serialVersionUID = 3019262118015113375L;

    @JsonProperty("bin")
    private String bin;

    @JsonProperty("issuingBank")
    @LocaleField
    private String issuingBank;

    @JsonProperty("issuingBankCode")
    private String issuingBankCode;

    @JsonProperty("paymentMode")
    private String payMethod;

    @JsonProperty("channelName")
    @LocaleField
    private String channelName;

    @JsonProperty("channelCode")
    private String channelCode;

    @JsonProperty("cnMin")
    private String minCardNum;

    @JsonProperty("cnMax")
    private String maxCardNum;

    @JsonProperty("cvvR")
    private String isCVVRequired;

    @JsonProperty("cvvL")
    private String cvvLength;

    @JsonProperty("expR")
    private String isExpiryRequired;

    @JsonProperty("isActive")
    private String isActive;

    @JsonProperty("isIndian")
    private String isIndian;

    @JsonProperty("binTokenization")
    private String binTokenization;

    @JsonProperty("isEligibleForCoft")
    private String isEligibleForCoft;

    @JsonProperty("isCoftPaymentSupported")
    private String isCoftPaymentSupported;

    @JsonProperty("cardPrefix")
    private String cardPrefix;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("countryName")
    private String countryName;

    @JsonProperty("countryNumericCode")
    private String countryNumericCode;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("currencyName")
    private String currencyName;

    @JsonProperty("currencyNumericCode")
    private String currencyNumericCode;

    @JsonProperty("currencySymbol")
    private String currencySymbol;

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getIssuingBank() {
        return issuingBank;
    }

    public void setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
    }

    public String getIssuingBankCode() {
        return issuingBankCode;
    }

    public void setIssuingBankCode(String issuingBankCode) {
        this.issuingBankCode = issuingBankCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getMinCardNum() {
        return minCardNum;
    }

    public void setMinCardNum(String minCardNum) {
        this.minCardNum = minCardNum;
    }

    public String getMaxCardNum() {
        return maxCardNum;
    }

    public void setMaxCardNum(String maxCardNum) {
        this.maxCardNum = maxCardNum;
    }

    public String getIsCVVRequired() {
        return isCVVRequired;
    }

    public void setIsCVVRequired(String isCVVRequired) {
        this.isCVVRequired = isCVVRequired;
    }

    public String getCvvLength() {
        return cvvLength;
    }

    public void setCvvLength(String cvvLength) {
        this.cvvLength = cvvLength;
    }

    public String getIsExpiryRequired() {
        return isExpiryRequired;
    }

    public void setIsExpiryRequired(String isExpiryRequired) {
        this.isExpiryRequired = isExpiryRequired;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getIsIndian() {
        return isIndian;
    }

    public void setIsIndian(String isIndian) {
        this.isIndian = isIndian;
    }

    public String getBinTokenization() {
        return binTokenization;
    }

    public void setBinTokenization(String binTokenization) {
        this.binTokenization = binTokenization;
    }

    public String getIsEligibleForCoft() {
        return isEligibleForCoft;
    }

    public void setIsEligibleForCoft(String isEligibleForCoft) {
        this.isEligibleForCoft = isEligibleForCoft;
    }

    public String getIsCoftPaymentSupported() {
        return isCoftPaymentSupported;
    }

    public void setIsCoftPaymentSupported(String isCoftPaymentSupported) {
        this.isCoftPaymentSupported = isCoftPaymentSupported;
    }

    public String getCardPrefix() {
        return cardPrefix;
    }

    public void setCardPrefix(String cardPrefix) {
        this.cardPrefix = cardPrefix;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryNumericCode() {
        return countryNumericCode;
    }

    public void setCountryNumericCode(String countryNumericCode) {
        this.countryNumericCode = countryNumericCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyNumericCode() {
        return currencyNumericCode;
    }

    public void setCurrencyNumericCode(String currencyNumericCode) {
        this.currencyNumericCode = currencyNumericCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BinData{");
        sb.append("bin='").append(bin).append('\'');
        sb.append(", issuingBank='").append(issuingBank).append('\'');
        sb.append(", issuingBankCode='").append(issuingBankCode).append('\'');
        sb.append(", payMethod='").append(payMethod).append('\'');
        sb.append(", channelName='").append(channelName).append('\'');
        sb.append(", channelCode='").append(channelCode).append('\'');
        sb.append(", minCardNum='").append(minCardNum).append('\'');
        sb.append(", maxCardNum='").append(maxCardNum).append('\'');
        sb.append(", isCVVRequired='").append(isCVVRequired).append('\'');
        sb.append(", cvvLength='").append(cvvLength).append('\'');
        sb.append(", isExpiryRequired='").append(isExpiryRequired).append('\'');
        sb.append(", isActive='").append(isActive).append('\'');
        sb.append(", isIndian='").append(isIndian).append('\'');
        sb.append(", binTokenization='").append(binTokenization).append('\'');
        sb.append(", isEligibleForCoft='").append(isEligibleForCoft).append('\'');
        sb.append(", isCoftPaymentSupported='").append(isCoftPaymentSupported).append('\'');
        sb.append(", cardPrefix='").append(cardPrefix).append('\'');
        sb.append(", countryCode='").append(countryCode).append('\'');
        sb.append(", countryName='").append(countryName).append('\'');
        sb.append(", countryNumericCode='").append(countryNumericCode).append('\'');
        sb.append(", currencyCode='").append(currencyCode).append('\'');
        sb.append(", currencyName='").append(currencyName).append('\'');
        sb.append(", currencyNumericCode='").append(currencyNumericCode).append('\'');
        sb.append(", currencySymbol='").append(currencySymbol).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
