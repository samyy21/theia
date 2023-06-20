package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.payment.models.PayOptionRemainingLimits;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierEmiPayMode extends EnhancedCashierPagePayModeBase {

    private EnhancedCashierBankData data;

    private EnhancedCashierSavedCard savedCard;

    @JsonProperty("isEnabled")
    private boolean enabled;

    public EnhancedCashierEmiPayMode(int id, String name, String type, EnhancedCashierBankData data,
            EnhancedCashierSavedCard savedCard, boolean enabled, boolean hybridDisabled, String remainingLimit,
            List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        super(id, name, type, hybridDisabled, remainingLimit, payOptionRemainingLimits);
        this.data = data;
        this.savedCard = savedCard;
        this.enabled = enabled;
    }

    public EnhancedCashierBankData getData() {
        return data;
    }

    public void setData(EnhancedCashierBankData data) {
        this.data = data;
    }

    public EnhancedCashierSavedCard getSavedCard() {
        return savedCard;
    }

    public void setSavedCard(EnhancedCashierSavedCard savedCard) {
        this.savedCard = savedCard;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "EnhancedCashierEmiPayMode{" + "data=" + data + ", savedCard=" + savedCard + ", enabled=" + enabled
                + '}';
    }

}
