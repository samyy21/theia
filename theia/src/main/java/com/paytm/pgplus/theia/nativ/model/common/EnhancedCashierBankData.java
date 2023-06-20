package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierBankData implements Serializable {

    private static final long serialVersionUID = 1963173967111702185L;

    private List<EnhancedCashierPayModeData> banks;

    public List<EnhancedCashierPayModeData> getBanks() {
        return banks;
    }

    public void setBanks(List<EnhancedCashierPayModeData> banks) {
        this.banks = banks;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnhancedCashierBankData [banks=").append(banks).append("]");
        return builder.toString();
    }

}
