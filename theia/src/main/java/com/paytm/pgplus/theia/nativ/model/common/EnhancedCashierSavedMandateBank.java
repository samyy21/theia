package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.enums.MandateAuthMode;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.theia.nativ.model.payview.response.SavedMandateBank;
import com.paytm.pgplus.theia.nativ.model.payview.response.StatusInfo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierSavedMandateBank extends EnhancedCashierPagePayModeBase {
    private static final long serialVersionUID = -8233666939206068324L;

    public EnhancedCashierSavedMandateBank(int id, String name, String type, List<SavedMandateBank> savedMandateBanks) {
        super(id, name, type);
        this.savedMandateBanks = savedMandateBanks;
    }

    List<SavedMandateBank> savedMandateBanks;

    @JsonProperty("isEnabled")
    private boolean enabled;

    public List<SavedMandateBank> getSavedMandateBanks() {
        return savedMandateBanks;
    }

    public void setSavedMandateBanks(List<SavedMandateBank> savedMandateBanks) {
        this.savedMandateBanks = savedMandateBanks;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
