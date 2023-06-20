package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.payment.models.PayOptionRemainingLimits;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierPagePayMode extends EnhancedCashierPagePayModeBase {

    private static final long serialVersionUID = 5497632907771644729L;

    private EnhancedCashierBankData data;
    private List<UpiPspHandlerData> upiDataList;

    @JsonProperty("isEnabled")
    private boolean enabled;

    private String upiPspPriority;

    private String upiBlockedPsp;

    @JsonProperty("isUpiIntentEnabled")
    private Boolean upiIntentEnable;

    @JsonProperty("isUpiAppsPayModeSupported")
    private Boolean upiAppsPayModeSupported;

    @JsonProperty("isUpiCollectBoxEnabled")
    private Boolean upiCollectBoxEnabled;

    private String bankLogoUrl;

    public EnhancedCashierPagePayMode(int id, String name, String type, EnhancedCashierBankData data, boolean enabled,
            boolean hybridDisabled, String remainingLimit, List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        super(id, name, type, hybridDisabled, remainingLimit, payOptionRemainingLimits);
        this.data = data;
        this.enabled = enabled;
    }

    public EnhancedCashierPagePayMode(int id, String name, String type, boolean enabled, boolean hybridDisabled,
            String remainingLimit, List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        super(id, name, type, hybridDisabled, remainingLimit, payOptionRemainingLimits);
        this.enabled = enabled;
    }

    public EnhancedCashierPagePayMode(int id, String name, String type, boolean enabled, boolean hybridDisabled,
            boolean onboarding, String remainingLimit, List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        super(id, name, type, hybridDisabled, onboarding, remainingLimit, payOptionRemainingLimits);
        this.enabled = enabled;
    }

    public EnhancedCashierBankData getData() {
        return data;
    }

    public void setData(EnhancedCashierBankData data) {
        this.data = data;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<UpiPspHandlerData> getUpiDataList() {
        return upiDataList;
    }

    public void setUpiDataList(List<UpiPspHandlerData> upiDataList) {
        this.upiDataList = upiDataList;
    }

    public String getUpiPspPriority() {
        return upiPspPriority;
    }

    public void setUpiPspPriority(String upiPspPriority) {
        this.upiPspPriority = upiPspPriority;
    }

    public String getUpiBlockedPsp() {
        return upiBlockedPsp;
    }

    public void setUpiBlockedPsp(String upiBlockedPsp) {
        this.upiBlockedPsp = upiBlockedPsp;
    }

    public Boolean getUpiIntentEnable() {
        return upiIntentEnable;
    }

    public void setUpiIntentEnable(Boolean upiIntentEnable) {
        this.upiIntentEnable = upiIntentEnable;
    }

    public Boolean getUpiAppsPayModeSupported() {
        return upiAppsPayModeSupported;
    }

    public void setUpiAppsPayModeSupported(Boolean upiAppsPayModeSupported) {
        this.upiAppsPayModeSupported = upiAppsPayModeSupported;
    }

    public Boolean getUpiCollectBoxEnabled() {
        return upiCollectBoxEnabled;
    }

    public void setUpiCollectBoxEnabled(Boolean upiCollectBoxEnabled) {
        this.upiCollectBoxEnabled = upiCollectBoxEnabled;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getBankLogoUrl() {
        return bankLogoUrl;
    }

    public void setBankLogoUrl(String bankLogoUrl) {
        this.bankLogoUrl = bankLogoUrl;
    }
}