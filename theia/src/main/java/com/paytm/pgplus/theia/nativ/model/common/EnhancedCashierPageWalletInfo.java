package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.biz.workflow.model.WalletLimits;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.models.TwoFARespData;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.payview.response.ActiveInfo;
import com.paytm.pgplus.theia.nativ.model.payview.response.SubWalletDetails;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierPageWalletInfo extends EnhancedCashierPagePayModeBase implements Serializable {

    private static final long serialVersionUID = -4842373370855945008L;

    private Double walletBalance;

    @JsonProperty("isWalletOnly")
    private boolean walletOnly;

    // checked and unchecked
    @JsonProperty("isUsed")
    private boolean used;

    // disable or enable
    @JsonProperty("isEnabled")
    private boolean enabled;

    @JsonProperty("isDisplay")
    private boolean display;

    @LocaleField
    private String insufficientBalanceMsg;
    @LocaleField
    private String isHybridDisabledMsg;

    private List<SubWalletDetails> subWalletDetails;

    private boolean onTheFlyKYCRequired;

    @LocaleField
    private String displayName;

    @JsonProperty("pcf")
    private PCFFeeCharges pcfFeeCharges;

    @JsonProperty("showOnlyWallet")
    private boolean showOnlyWallet;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("isActive")
    private ActiveInfo isActive;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean merchantAccept;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("walletLimits")
    private WalletLimits walletLimits;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remainingLimit;

    private TwoFARespData twoFAConfig;

    public PCFFeeCharges getPcfFeeCharges() {
        return pcfFeeCharges;
    }

    public void setPcfFeeCharges(PCFFeeCharges pcfFeeCharges) {
        this.pcfFeeCharges = pcfFeeCharges;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public boolean isWalletOnly() {
        return walletOnly;
    }

    public void setWalletOnly(boolean walletOnly) {
        this.walletOnly = walletOnly;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public void setInsufficientBalanceMsg(String insufficientBalanceMsg) {
        this.insufficientBalanceMsg = insufficientBalanceMsg;
    }

    public void setIsHybridDisabledMsg(String isHybridDisabledMsg) {
        this.isHybridDisabledMsg = isHybridDisabledMsg;
    }

    public List<SubWalletDetails> getSubWalletDetails() {
        return subWalletDetails;
    }

    public void setSubWalletDetails(List<SubWalletDetails> subWalletDetails) {
        this.subWalletDetails = subWalletDetails;
    }

    public boolean isOnTheFlyKYCRequired() {
        return onTheFlyKYCRequired;
    }

    public void setOnTheFlyKYCRequired(boolean onTheFlyKYCRequired) {
        this.onTheFlyKYCRequired = onTheFlyKYCRequired;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isShowOnlyWallet() {
        return showOnlyWallet;
    }

    public void setShowOnlyWallet(boolean showOnlyWallet) {
        this.showOnlyWallet = showOnlyWallet;
    }

    public ActiveInfo getIsActive() {
        return isActive;
    }

    public void setIsActive(ActiveInfo isActive) {
        this.isActive = isActive;
    }

    public Boolean getMerchantAccept() {
        return merchantAccept;
    }

    public void setMerchantAccept(Boolean merchantAccept) {
        this.merchantAccept = merchantAccept;
    }

    public WalletLimits getWalletLimits() {
        return walletLimits;
    }

    public void setWalletLimits(WalletLimits walletLimits) {
        this.walletLimits = walletLimits;
    }

    public String getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(String remainingLimit) {
        this.remainingLimit = remainingLimit;
    }

    public TwoFARespData getTwoFAConfig() {
        return twoFAConfig;
    }

    public void setTwoFAConfig(TwoFARespData twoFAConfig) {
        this.twoFAConfig = twoFAConfig;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnhancedCashierPageWalletInfo [walletBalance=").append(walletBalance).append(", walletOnly=")
                .append(walletOnly).append(", used=").append(used).append(", enabled=").append(enabled)
                .append(", subWalletDetails=").append(subWalletDetails).append(", display=").append(display)
                .append(", onTheFlyKYCRequired=").append(onTheFlyKYCRequired).append(", displayName=")
                .append(displayName).append(", pcfFeeCharges=").append(showOnlyWallet).append(", showOnlyWallet=")
                .append(pcfFeeCharges).append(", isActive=").append(isActive).append(", merchantAccept=")
                .append(merchantAccept).append(", walletLimits=").append(walletLimits).append(", remainingLimit=")
                .append(remainingLimit).append(", twoFAConfig=").append(twoFAConfig).append("]");
        return builder.toString();
    }
}
