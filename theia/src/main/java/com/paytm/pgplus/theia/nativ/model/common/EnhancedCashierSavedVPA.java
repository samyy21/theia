package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierSavedVPA extends EnhancedCashierPagePayModeBase {

    private static final long serialVersionUID = -7668246997812136348L;

    private List<SavedVPAInstrument> savedVPAs;

    public List<SavedVPAInstrument> getSavedVPAs() {
        return savedVPAs;
    }

    public EnhancedCashierSavedVPA(int id, String name, String type, List<SavedVPAInstrument> savedVPAs) {
        super(id, name, type);
        this.savedVPAs = savedVPAs;
    }

    public void setSavedVPAs(List<SavedVPAInstrument> savedVPAs) {
        this.savedVPAs = savedVPAs;
    }

    @Override
    public String toString() {
        return "EnhancedCashierSavedVPA{" + "savedVPAs=" + savedVPAs + '}';
    }
}
