package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.payment.models.PayOptionRemainingLimits;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.models.Money;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierCODPayMode extends EnhancedCashierPagePayModeBase {

    private static final long serialVersionUID = -7333840791463771253L;

    @JsonProperty("isEnabled")
    private boolean enabled;

    private Money minAmount;

    @LocaleField
    private String codHybridErrMsg;

    @LocaleField
    private String codMessage;

    public EnhancedCashierCODPayMode(int id, String name, String type, boolean enabled, Money minAmount) {
        super(id, name, type);
        this.enabled = enabled;
        this.minAmount = minAmount;
    }

    public EnhancedCashierCODPayMode(int id, String name, String type, boolean enabled, Money minAmount,
            String codHybridErrMsg, String codDetailedMsg, boolean isHybridDisabled, String remainingLimit,
            List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        super(id, name, type, isHybridDisabled, remainingLimit, payOptionRemainingLimits);
        this.enabled = enabled;
        this.minAmount = minAmount;
        this.codHybridErrMsg = codHybridErrMsg;
        this.codMessage = codDetailedMsg;
    }

    public Money getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Money minAmount) {
        this.minAmount = minAmount;
    }

    public String getCodHybridErrMsg() {
        return codHybridErrMsg;
    }

    public void setCodHybridErrMsg(String codHybridErrMsg) {
        this.codHybridErrMsg = codHybridErrMsg;
    }

    public String getCodMessage() {
        return codMessage;
    }

    public void setCodMessage(String codMessage) {
        this.codMessage = codMessage;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "EnhancedCashierCODPayMode{" + "enabled=" + enabled + ", minAmount=" + minAmount + ", codHybridErrMsg='"
                + codHybridErrMsg + '\'' + ", codMessage='" + codMessage + '\'' + '}';
    }
}
