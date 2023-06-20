/**
 * 
 */
package com.paytm.pgplus.biz.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paytm.pgplus.cache.annotation.RedisEncrypt;
import com.paytm.pgplus.cache.annotation.RedisEncryptAttribute;
import com.paytm.pgplus.facade.emisubvention.models.CardIndexDetail;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
@RedisEncrypt
public class CardBeanBiz implements Serializable {

    private static final long serialVersionUID = 33L;
    private Long cardId;
    @RedisEncryptAttribute
    private String cardNumber;
    @RedisEncryptAttribute
    private String expiryDate;
    private Long firstSixDigit;
    private Long lastFourDigit;
    private String cardIndexNo;
    private String instNetworkType;
    private String instNetworkCode;
    private String cardType;
    private String holderName;
    private String holderMobileNo;
    private String instId;
    private String instBranchId;
    @RedisEncryptAttribute
    private String cvv2;
    private String otp;
    private String cardScheme;
    private Short expiryYear;
    private Short expiryMonth;
    private String vpaAccNumber;
    private String mId;
    private String custId;
    private String eightDigitBinHash;

    /*
     * Card Active or De-active Status
     */
    private Integer status;

    /*
     * Paytm User ID
     */
    private String userId;
    private String displayName;
    private Boolean oneClickSupported;
    private CardIndexDetail savedCardEmiSubventionDetails;
    private boolean isPrepaidCard;
    private boolean indian;
    private boolean isDisabled;
    private String disabledReason;

    private String zeroSuccessRate;
    private String instName;
    private String countryCode;
    private String country;
    private String countryCodeIso;
    private String currency;
    private String currencyCode;
    private String currencyCodeIso;
    private String symbol;
    private String currencyPrecision;
    private String category;

    private boolean isCardCoft;
    private boolean isEligibleForCoft;
    private boolean isCoftPaymentSupported;
    private String par;
    private String tokenStatus;
    private boolean isActive;
    private String fingerPrint;
    private String gcin;
    private String remainingLimit;

    @JsonIgnore
    private String accountRangeCardBin;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }

    private boolean corporateCard;

    public Boolean getOneClickSupported() {
        return oneClickSupported;
    }

    public void setOneClickSupported(Boolean oneClickSupported) {
        this.oneClickSupported = oneClickSupported;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public void setCardIndexNo(final String cardIndexNo) {
        this.cardIndexNo = cardIndexNo;
    }

    public String getInstNetworkType() {
        return instNetworkType;
    }

    public void setInstNetworkType(final String instNetworkType) {
        this.instNetworkType = instNetworkType;
    }

    public String getInstNetworkCode() {
        return instNetworkCode;
    }

    public void setInstNetworkCode(final String instNetworkCode) {
        this.instNetworkCode = instNetworkCode;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(final String holderName) {
        this.holderName = holderName;
    }

    public String getHolderMobileNo() {
        return holderMobileNo;
    }

    public void setHolderMobileNo(final String holderMobileNo) {
        this.holderMobileNo = holderMobileNo;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(final String instId) {
        this.instId = instId;
    }

    public String getInstBranchId() {
        return instBranchId;
    }

    public void setInstBranchId(final String instBranchId) {
        this.instBranchId = instBranchId;
    }

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(final String cvv2) {
        this.cvv2 = cvv2;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(final String otp) {
        this.otp = otp;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(final String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public Short getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(final Short expiryYear) {
        this.expiryYear = expiryYear;
    }

    public Short getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(final Short expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(final Long cardId) {
        this.cardId = cardId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(final String cardType) {
        this.cardType = cardType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Long getFirstSixDigit() {
        return firstSixDigit;
    }

    public void setFirstSixDigit(final Long firstSixDigit) {
        this.firstSixDigit = firstSixDigit;
    }

    public Long getLastFourDigit() {
        return lastFourDigit;
    }

    public void setLastFourDigit(final Long lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getVpaAccNumber() {
        return vpaAccNumber;
    }

    public void setVpaAccNumber(String vpaAccNumber) {
        this.vpaAccNumber = vpaAccNumber;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public CardIndexDetail getSavedCardEmiSubventionDetails() {
        return savedCardEmiSubventionDetails;
    }

    public void setSavedCardEmiSubventionDetails(CardIndexDetail savedCardEmiSubventionDetails) {
        this.savedCardEmiSubventionDetails = savedCardEmiSubventionDetails;
    }

    public String getEightDigitBinHash() {
        return eightDigitBinHash;
    }

    public void setEightDigitBinHash(String eightDigitBinHash) {
        this.eightDigitBinHash = eightDigitBinHash;
    }

    public boolean isPrepaidCard() {
        return isPrepaidCard;
    }

    public void setPrepaidCard(boolean prepaidCard) {
        isPrepaidCard = prepaidCard;
    }

    public boolean isIndian() {
        return indian;
    }

    public void setIndian(boolean indian) {
        this.indian = indian;
    }

    public boolean isCorporateCard() {
        return corporateCard;
    }

    public void setCorporateCard(boolean corporateCard) {
        this.corporateCard = corporateCard;
    }

    public String getZeroSuccessRate() {
        return zeroSuccessRate;
    }

    public void setZeroSuccessRate(String zeroSuccessRate) {
        this.zeroSuccessRate = zeroSuccessRate;
    }

    public String getInstName() {
        return instName;
    }

    public void setInstName(String instName) {
        this.instName = instName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCodeIso() {
        return countryCodeIso;
    }

    public void setCountryCodeIso(String countryCodeIso) {
        this.countryCodeIso = countryCodeIso;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCodeIso() {
        return currencyCodeIso;
    }

    public void setCurrencyCodeIso(String currencyCodeIso) {
        this.currencyCodeIso = currencyCodeIso;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCurrencyPrecision() {
        return currencyPrecision;
    }

    public void setCurrencyPrecision(String currencyPrecision) {
        this.currencyPrecision = currencyPrecision;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getAccountRangeCardBin() {
        return accountRangeCardBin;
    }

    public void setAccountRangeCardBin(String accountRangeCardBin) {
        this.accountRangeCardBin = accountRangeCardBin;
    }

    public String getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(String remainingLimit) {
        this.remainingLimit = remainingLimit;
    }

    public CardBeanBiz() {

    }

    public CardBeanBiz(final Long cardId, final String cardNumber, final String cardScheme, final String expiryDate,
            final Long firstSixDigit, final Long lastFourDigit, final Integer status, final String userId,
            final String mId, final String custId) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.cardScheme = cardScheme;
        this.expiryDate = expiryDate;
        this.firstSixDigit = firstSixDigit;
        this.lastFourDigit = lastFourDigit;
        this.status = status;
        this.userId = userId;
        this.mId = mId;
        this.custId = custId;
    }

    public CardBeanBiz(final Long cardId, final String cardNumber, final String cardScheme, final String expiryDate,
            final Long firstSixDigit, final Long lastFourDigit, final Integer status, final String userId,
            final String instId, final String custId, final String mId) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.cardScheme = cardScheme;
        this.expiryDate = expiryDate;
        this.firstSixDigit = firstSixDigit;
        this.lastFourDigit = lastFourDigit;
        this.status = status;
        this.userId = userId;
        this.instId = instId;
        this.custId = custId;
        this.mId = mId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CardBeanBiz [cardId=").append(cardId).append(", firstSixDigit=").append(firstSixDigit)
                .append(", lastFourDigit=").append(lastFourDigit).append(", cardIndexNo=").append(cardIndexNo)
                .append(", instNetworkType=").append(instNetworkType).append(", instNetworkCode=")
                .append(instNetworkCode).append(", cardType=").append(cardType).append(", instId=").append(instId)
                .append(", instBranchId=").append(instBranchId).append(", otp=").append(otp).append(", cardScheme=")
                .append(cardScheme).append(", status=").append(status).append(", userId=").append(userId)
                .append(", vpaAccNumber=").append(vpaAccNumber).append(", displayName=").append(displayName)
                .append(", savedCardEmiSubventionDetails=").append(savedCardEmiSubventionDetails).append("]")
                .append(", oneClickSupported=").append(oneClickSupported).append(", zeroSuccessRate=")
                .append(zeroSuccessRate).append(", instName=").append(instName).append(countryCode)
                .append(", countryCode=").append(country).append(", country=").append(countryCodeIso)
                .append(", countryCodeIso=").append(currency).append(", currency=").append(currencyCode)
                .append(", currencyCode=").append(currencyCodeIso).append(", currencyCodeIso=").append(symbol)
                .append(", symbol=").append(currencyPrecision).append(", currencyPrecision=").append(category)
                .append(", category=").append(", isCardCoft=").append(isCardCoft).append(", isEligibleForCoft=")
                .append(isEligibleForCoft).append(", isCoftPaymentSupported=").append(isCoftPaymentSupported)
                .append(", par=").append(par).append(", tokenStatus=").append(tokenStatus).append(", fingerPrint=")
                .append(fingerPrint).append(", accountRangeCardBin=").append(accountRangeCardBin)
                .append(", remainingLimit=").append(remainingLimit).append("]");

        return builder.toString();
    }
}
