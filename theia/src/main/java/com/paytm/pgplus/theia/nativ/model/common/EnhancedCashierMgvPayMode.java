package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.facade.payment.models.PayOptionRemainingLimits;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierMgvPayMode extends EnhancedCashierPagePayModeBase {

    @JsonProperty("isEnabled")
    private boolean enabled;

    private Double availableBalance;

    private String inSufficientBalanceMsg;

    private boolean insufficientBalance;

    private String templateId;

    public EnhancedCashierMgvPayMode(int generateDisplayId, String displayName, String type, boolean hybridDisabled,
            boolean enabled, String remainingLimit, List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        super(generateDisplayId, displayName, type, hybridDisabled, remainingLimit, payOptionRemainingLimits);
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Double availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getInSufficientBalanceMsg() {
        return inSufficientBalanceMsg;
    }

    public void setInSufficientBalanceMsg(String inSufficientBalanceMsg) {
        this.inSufficientBalanceMsg = inSufficientBalanceMsg;
    }

    public boolean isInsufficientBalance() {
        return insufficientBalance;
    }

    public void setInsufficientBalance(boolean insufficientBalance) {
        this.insufficientBalance = insufficientBalance;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
