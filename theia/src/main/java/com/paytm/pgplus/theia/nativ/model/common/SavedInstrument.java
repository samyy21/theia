package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.emisubvention.models.CardIndexDetail;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.models.PaymentOfferDetails;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.payview.response.StatusInfo;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedInstrument implements Serializable {

    private static final long serialVersionUID = 2017802264230427543L;

    private String id;
    private String name;
    private String desc;
    private String firstSix;
    private String lastFour;
    private String cardScheme;
    private String instId;
    private String type;
    private boolean idebit;
    private String cvvLength;
    private String subType;
    private Map<String, String> extra;
    private Boolean cvvRequired;
    private boolean emiAvailable;
    @LocaleField
    private String displayName;
    private String savedInstrumentId;
    private String minEMIAmount;
    private String maxEMIAmount;
    @JsonProperty("isHybridDisabled")
    private boolean hybridDisabled;

    @JsonProperty("pcf")
    private PCFFeeCharges pcfFeeCharges;

    private boolean selected;
    private Boolean prepaidCardSupported;

    public PCFFeeCharges getPcfFeeCharges() {
        return pcfFeeCharges;
    }

    @JsonProperty("isEmiHybridDisabled")
    private boolean emiHybridDisabled;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaymentOfferDetails paymentOfferDetails;

    @JsonProperty("isDisabled")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private StatusInfo isDisabled;

    private CardIndexDetail savedCardEmisubventionDetail;

    private String bankLogoUrl;
    @LocaleField
    private String bankName;
    @LocaleField
    private String cardType;

    @JsonProperty(value = "isCardCoft")
    private boolean isCardCoft;
    @JsonProperty(value = "isEligibleForCoft")
    private boolean isEligibleForCoft;
    @JsonProperty(value = "isCoftPaymentSupported")
    private boolean isCoftPaymentSupported;
    private String par;
    private String tokenStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean prepaidCard;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean corporateCard;

    public String getBankLogoUrl() {
        return bankLogoUrl;
    }

    public void setBankLogoUrl(String bankLogoUrl) {
        this.bankLogoUrl = bankLogoUrl;
    }

    public StatusInfo getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(StatusInfo isDisabled) {
        this.isDisabled = isDisabled;
    }

    public void setPcfFeeCharges(PCFFeeCharges pcfFeeCharges) {
        this.pcfFeeCharges = pcfFeeCharges;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFirstSix() {
        return firstSix;
    }

    public void setFirstSix(String firstSix) {
        this.firstSix = firstSix;
    }

    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIdebit() {
        return idebit;
    }

    public void setIdebit(boolean idebit) {
        this.idebit = idebit;
    }

    public String getCvvLength() {
        return cvvLength;
    }

    public void setCvvLength(String cvvLength) {
        this.cvvLength = cvvLength;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }

    public Boolean getCvvRequired() {
        return cvvRequired;
    }

    public void setCvvRequired(Boolean cvvRequired) {
        this.cvvRequired = cvvRequired;
    }

    public boolean isEmiAvailable() {
        return emiAvailable;
    }

    public void setEmiAvailable(boolean emiAvailable) {
        this.emiAvailable = emiAvailable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSavedInstrumentId() {
        return savedInstrumentId;
    }

    public void setSavedInstrumentId(String savedInstrumentId) {
        this.savedInstrumentId = savedInstrumentId;
    }

    public String getMinEMIAmount() {
        return minEMIAmount;
    }

    public void setMinEMIAmount(String minEMIAmount) {
        this.minEMIAmount = minEMIAmount;
    }

    public String getMaxEMIAmount() {
        return maxEMIAmount;
    }

    public void setMaxEMIAmount(String maxEMIAmount) {
        this.maxEMIAmount = maxEMIAmount;
    }

    public boolean isHybridDisabled() {
        return hybridDisabled;
    }

    public void setHybridDisabled(boolean hybridDisabled) {
        this.hybridDisabled = hybridDisabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isEmiHybridDisabled() {
        return emiHybridDisabled;
    }

    public void setEmiHybridDisabled(boolean emiHybridDisabled) {
        this.emiHybridDisabled = emiHybridDisabled;
    }

    public Boolean getPrepaidCardSupported() {
        return prepaidCardSupported;
    }

    public void setPrepaidCardSupported(Boolean prepaidCardSupported) {
        this.prepaidCardSupported = prepaidCardSupported;
    }

    public PaymentOfferDetails getPaymentOfferDetails() {
        return paymentOfferDetails;
    }

    public void setPaymentOfferDetails(PaymentOfferDetails paymentOfferDetails) {
        this.paymentOfferDetails = paymentOfferDetails;
    }

    public CardIndexDetail getSavedCardEmisubventionDetail() {
        return savedCardEmisubventionDetail;
    }

    public void setSavedCardEmisubventionDetail(CardIndexDetail savedCardEmisubventionDetail) {
        this.savedCardEmisubventionDetail = savedCardEmisubventionDetail;
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

    public Boolean getPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(Boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public Boolean getCorporateCard() {
        return corporateCard;
    }

    public void setCorporateCard(Boolean corporateCard) {
        this.corporateCard = corporateCard;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SavedInstrument{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", desc='").append(desc).append('\'');
        sb.append(", firstSix='").append(firstSix).append('\'');
        sb.append(", lastFour='").append(lastFour).append('\'');
        sb.append(", cardScheme='").append(cardScheme).append('\'');
        sb.append(", instId='").append(instId).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", idebit=").append(idebit);
        sb.append(", cvvLength='").append(cvvLength).append('\'');
        sb.append(", subType='").append(subType).append('\'');
        sb.append(", extra=").append(extra);
        sb.append(", cvvRequired=").append(cvvRequired);
        sb.append(", emiAvailable=").append(emiAvailable);
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", savedInstrumentId='").append(savedInstrumentId).append('\'');
        sb.append(", minEMIAmount='").append(minEMIAmount).append('\'');
        sb.append(", maxEMIAmount='").append(maxEMIAmount).append('\'');
        sb.append(", hybridDisabled='").append(hybridDisabled).append('\'');
        sb.append(", selected='").append(selected).append('\'');
        sb.append(", emiHybridDisabled='").append(emiHybridDisabled).append('\'');
        sb.append(", paymentOfferDetails='").append(paymentOfferDetails).append('\'');
        sb.append(", savedCardEmisubventionDetail='").append(savedCardEmisubventionDetail).append('\'');
        sb.append(", bankName='").append(bankName).append('\'');
        sb.append(", cardType='").append(cardType).append('\'');
        sb.append(", isCardCoft='").append(isCardCoft).append('\'');
        sb.append(", isEligibleForCoft='").append(isEligibleForCoft).append('\'');
        sb.append(", isCoftPaymentSupported='").append(isCoftPaymentSupported).append('\'');
        sb.append(", par='").append(par).append('\'');
        sb.append(", tokenStatus='").append(tokenStatus).append('\'');
        sb.append(", prepaidCard='").append(prepaidCard).append('\'');
        sb.append(", corporateCard='").append(corporateCard).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
