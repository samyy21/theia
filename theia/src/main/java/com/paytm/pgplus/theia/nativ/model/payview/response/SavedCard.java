package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.emisubvention.models.CardIndexDetail;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.models.ItemLevelPaymentOffer;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.PaymentOfferDetails;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedCard extends CreditCard {

    private static final long serialVersionUID = 7305134116997915974L;
    @NotNull
    @Valid
    private CardDetails cardDetails;

    @LocaleField
    private String issuingBank;

    @JsonIgnore
    private EmiChannel emiDetails;

    private boolean isEmiAvailable = false;

    private boolean isCardCoBranded;

    private List<String> authModes;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Money minAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Money maxAmount;

    @LocaleField
    private String displayName;

    private String priority;

    private PaymentOfferDetails paymentOfferDetails;

    // TODO: Can be renamed as itemlevelpaymentOffer
    private ItemLevelPaymentOffer paymentOfferDetailsV2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("isEmiHybridDisabled")
    private boolean emiHybridDisabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaymentOfferDetails nonHybridPaymentOfferDetails;

    private CardIndexDetail savedCardEmisubventionDetail;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean prepaidCard;

    @LocaleField
    private String bankName;
    @LocaleField
    private String cardType;

    private boolean isCardCoft;
    private boolean isEligibleForCoft;
    private boolean isCoftPaymentSupported;
    private String par;
    private String tokenStatus;
    private String fingerPrint;
    private String gcin;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remainingLimit;

    @JsonIgnore
    private String accountRangeCardBin;

    public SavedCard(CardDetails cardDetails) {
        this.cardDetails = cardDetails;
    }

    public SavedCard() {
    }

    public SavedCard(CardDetails cardDetails, String issuingBank) {
        this.cardDetails = cardDetails;
        this.issuingBank = issuingBank;
    }

    public CardDetails getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(CardDetails cardDetails) {
        this.cardDetails = cardDetails;
    }

    public String getIssuingBank() {
        return issuingBank;
    }

    public void setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
    }

    public EmiChannel getEmiDetails() {
        return emiDetails;
    }

    public void setEmiDetails(EmiChannel emiDetails) {
        this.emiDetails = emiDetails;
    }

    public List<String> getAuthModes() {
        return authModes;
    }

    public void setAuthModes(List<String> authModes) {
        this.authModes = authModes;
    }

    public boolean getIsEmiAvailable() {
        return isEmiAvailable;
    }

    public void setIsEmiAvailable(boolean isEmiAvailable) {
        this.isEmiAvailable = isEmiAvailable;
    }

    public boolean getIsCardCoBranded() {
        return isCardCoBranded;
    }

    public void setIsCardCoBranded(boolean cardCoBranded) {
        isCardCoBranded = cardCoBranded;
    }

    public Money getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Money minAmount) {
        this.minAmount = minAmount;
    }

    public Money getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Money maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public PaymentOfferDetails getPaymentOfferDetails() {
        return paymentOfferDetails;
    }

    public void setPaymentOfferDetails(PaymentOfferDetails paymentOfferDetails) {
        this.paymentOfferDetails = paymentOfferDetails;
    }

    public boolean isEmiHybridDisabled() {
        return emiHybridDisabled;
    }

    public void setEmiHybridDisabled(boolean emiHybridDisabled) {
        this.emiHybridDisabled = emiHybridDisabled;
    }

    public PaymentOfferDetails getNonHybridPaymentOfferDetails() {
        return nonHybridPaymentOfferDetails;
    }

    public void setNonHybridPaymentOfferDetails(PaymentOfferDetails nonHybridPaymentOfferDetails) {
        this.nonHybridPaymentOfferDetails = nonHybridPaymentOfferDetails;
    }

    public ItemLevelPaymentOffer getPaymentOfferDetailsV2() {
        return paymentOfferDetailsV2;
    }

    public void setPaymentOfferDetailsV2(ItemLevelPaymentOffer paymentOfferDetailsV2) {
        this.paymentOfferDetailsV2 = paymentOfferDetailsV2;
    }

    public CardIndexDetail getSavedCardEmisubventionDetail() {
        return savedCardEmisubventionDetail;
    }

    public void setSavedCardEmisubventionDetail(CardIndexDetail savedCardEmisubventionDetail) {
        this.savedCardEmisubventionDetail = savedCardEmisubventionDetail;
    }

    public Boolean getPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(Boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @JsonProperty(value = "isCardCoft")
    public boolean isCardCoft() {
        return isCardCoft;
    }

    public void setCardCoft(boolean cardCoft) {
        isCardCoft = cardCoft;
    }

    @JsonProperty(value = "isEligibleForCoft")
    public boolean isEligibleForCoft() {
        return isEligibleForCoft;
    }

    public void setEligibleForCoft(boolean eligibleForCoft) {
        isEligibleForCoft = eligibleForCoft;
    }

    @JsonProperty(value = "isCoftPaymentSupported")
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SavedCard [cardDetails=");
        builder.append(cardDetails);
        builder.append(", issuingBank=");
        builder.append(issuingBank);
        builder.append(", emiDetails=");
        builder.append(emiDetails);
        builder.append(", isEmiAvailable=");
        builder.append(isEmiAvailable);
        builder.append(", authModes=");
        builder.append(authModes);
        builder.append(", minAmount=");
        builder.append(minAmount);
        builder.append(", maxAmount=");
        builder.append(maxAmount);
        builder.append(", displayName=");
        builder.append(displayName);
        builder.append(", paymentOfferDetails=");
        builder.append(paymentOfferDetails);
        builder.append(", isEmiHybridDisabled=");
        builder.append(emiHybridDisabled);
        builder.append(", savedCardEmisubventionDetail=");
        builder.append(savedCardEmisubventionDetail);
        builder.append(", supportedCardSubTypes=");
        builder.append(this.getSupportedCardSubTypes());
        builder.append(", bankName=");
        builder.append(this.getBankName());
        builder.append(", cardType=");
        builder.append(this.getCardType());
        builder.append(", isCardCoft=");
        builder.append(isCardCoft);
        builder.append(", isEligibleForCoft=");
        builder.append(isEligibleForCoft);
        builder.append(", isCoftPaymentSupported=");
        builder.append(isCoftPaymentSupported);
        builder.append(", par=");
        builder.append(par);
        builder.append(", tokenStatus=");
        builder.append(tokenStatus);
        builder.append(", fingerPrint=");
        builder.append(fingerPrint);
        builder.append(", gcin=");
        builder.append(gcin);
        builder.append(", accountRangeCardBin=");
        builder.append(accountRangeCardBin);
        builder.append(", remainingLimit=");
        builder.append(remainingLimit);
        builder.append("]");
        return builder.toString();
    }

}
