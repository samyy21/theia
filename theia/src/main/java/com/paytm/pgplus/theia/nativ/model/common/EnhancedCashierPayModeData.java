package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.enums.MandateAuthMode;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.theia.nativ.enums.BankDisplayNames;
import com.paytm.pgplus.theia.nativ.model.payview.response.StatusInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierPayModeData implements Serializable {

    private static final long serialVersionUID = 4997866828766586585L;

    private String channelCode;

    @LocaleField(localeEnum = BankDisplayNames.class, methodName = "getBankName")
    private String channelName;

    private String iconUrl;

    private String bankLogoUrl;

    private StatusInfo hasLowSuccess;

    private boolean selected;

    private List<EnhancedCashierEmiPlanData> emiPlan;

    private String emiType;

    @LocaleField
    private String displayName;

    private Boolean cardDetailsNotRequired;

    private String selectedText;

    private String minEMIAmount;

    private String maxEMIAmount;

    @JsonProperty("isHybridDisabled")
    private boolean hybridDisabled;

    private MandateMode mandateMode;

    private List<MandateAuthMode> mandateAuthMode;

    private String mandateBankCode;

    private StatusInfo isDisabled;

    private String channelDisplayName;

    public EnhancedCashierPayModeData(String channelCode, String channelName, String iconUrl, StatusInfo hasLowSuccess,
            List<EnhancedCashierEmiPlanData> emiPlan) {
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.iconUrl = iconUrl;
        this.hasLowSuccess = hasLowSuccess;
        this.emiPlan = emiPlan;
    }

    public EnhancedCashierPayModeData(String channelCode, String channelName, String iconUrl, StatusInfo hasLowSuccess) {
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.iconUrl = iconUrl;
        this.hasLowSuccess = hasLowSuccess;
    }

    public EnhancedCashierPayModeData(String channelCode, String channelName, String iconUrl, StatusInfo hasLowSuccess,
            boolean hybridDisabled) {
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.iconUrl = iconUrl;
        this.hasLowSuccess = hasLowSuccess;
        this.hybridDisabled = hybridDisabled;
    }

    public EnhancedCashierPayModeData(MandateMode mandateMode, List<MandateAuthMode> mandateAuthMode,
            String mandateBankCode, String channelCode, String channelName, String iconUrl, StatusInfo hasLowSuccess,
            StatusInfo isDisabled, boolean hybridDisabled) {
        this.mandateMode = mandateMode;
        this.mandateAuthMode = mandateAuthMode;
        this.mandateBankCode = mandateBankCode;
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.iconUrl = iconUrl;
        this.hasLowSuccess = hasLowSuccess;
        this.isDisabled = isDisabled;
        this.hybridDisabled = hybridDisabled;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getBankLogoUrl() {
        return bankLogoUrl;
    }

    public void setBankLogoUrl(String bankLogoUrl) {
        this.bankLogoUrl = bankLogoUrl;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getCardDetailsNotRequired() {
        return cardDetailsNotRequired;
    }

    public void setCardDetailsNotRequired(Boolean cardDetailsNotRequired) {
        this.cardDetailsNotRequired = cardDetailsNotRequired;
    }

    public StatusInfo getHasLowSuccess() {
        return hasLowSuccess;
    }

    public void setHasLowSuccess(StatusInfo hasLowSuccess) {
        this.hasLowSuccess = hasLowSuccess;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<EnhancedCashierEmiPlanData> getEmiPlan() {
        return emiPlan;
    }

    public void setEmiPlan(List<EnhancedCashierEmiPlanData> emiPlan) {
        this.emiPlan = emiPlan;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
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

    public MandateMode getMandateMode() {
        return mandateMode;
    }

    public void setMandateMode(MandateMode mandateMode) {
        this.mandateMode = mandateMode;
    }

    public List<MandateAuthMode> getMandateAuthMode() {
        return mandateAuthMode;
    }

    public void setMandateAuthMode(List<MandateAuthMode> mandateAuthMode) {
        this.mandateAuthMode = mandateAuthMode;
    }

    public String getMandateBankCode() {
        return mandateBankCode;
    }

    public void setMandateBankCode(String mandateBankCode) {
        this.mandateBankCode = mandateBankCode;
    }

    public StatusInfo getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(StatusInfo isDisabled) {
        this.isDisabled = isDisabled;
    }

    public String getChannelDisplayName() {
        return channelDisplayName;
    }

    public void setChannelDisplayName(String channelDisplayName) {
        this.channelDisplayName = channelDisplayName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnhancedCashierPayModeData{");
        sb.append("channelCode='").append(channelCode).append('\'');
        sb.append(", channelName='").append(channelName).append('\'');
        sb.append(", iconUrl='").append(iconUrl).append('\'');
        sb.append(", hasLowSuccess=").append(hasLowSuccess);
        sb.append(", selected=").append(selected);
        sb.append(", emiPlan=").append(emiPlan);
        sb.append(", emiType='").append(emiType).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", cardDetailsNotRequired=").append(cardDetailsNotRequired);
        sb.append(", selectedText='").append(selectedText).append('\'');
        sb.append(", minEMIAmount='").append(minEMIAmount).append('\'');
        sb.append(", maxEMIAmount='").append(maxEMIAmount).append('\'');
        sb.append(", hybridDisabled='").append(hybridDisabled).append('\'');
        sb.append(", mandateAuthMode='").append(mandateAuthMode).append('\'');
        sb.append(", mandateBankCode='").append(mandateBankCode).append('\'');
        sb.append(", mandateMode='").append(mandateMode).append('\'');
        sb.append(", isDisabled='").append(isDisabled).append('\'');
        sb.append("channelDisplayName='").append(channelDisplayName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
