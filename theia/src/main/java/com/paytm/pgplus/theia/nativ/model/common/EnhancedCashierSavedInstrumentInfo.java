package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierSavedInstrumentInfo implements Serializable {

    private static final long serialVersionUID = 56584289297005225L;

    private List<SavedVPAInstrument> savedVPAs;

    private List<SavedInstrument> savedCards;

    private boolean savedVPASelected;

    private boolean savedCardSelected;

    public List<SavedInstrument> getSavedCards() {
        return savedCards;
    }

    public void setSavedCards(List<SavedInstrument> savedCards) {
        this.savedCards = savedCards;
    }

    public List<SavedVPAInstrument> getSavedVPAs() {
        return savedVPAs;
    }

    public void setSavedVPAs(List<SavedVPAInstrument> savedVPAs) {
        this.savedVPAs = savedVPAs;
    }

    public boolean isSavedVPASelected() {
        return savedVPASelected;
    }

    public void setSavedVPASelected(boolean savedVPASelected) {
        this.savedVPASelected = savedVPASelected;
    }

    public boolean isSavedCardSelected() {
        return savedCardSelected;
    }

    public void setSavedCardSelected(boolean savedCardSelected) {
        this.savedCardSelected = savedCardSelected;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnhancedCashierSavedInstrumentInfo{");
        sb.append("savedVPAs=").append(savedVPAs);
        sb.append(", savedCards=").append(savedCards);
        sb.append(", savedVPASelected=").append(savedVPASelected);
        sb.append(", savedCardSelected=").append(savedCardSelected);
        sb.append('}');
        return sb.toString();
    }

}
