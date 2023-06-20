package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedInstruments implements Serializable {

    private static final long serialVersionUID = -3597200419411647987L;
    private List<SavedCard> savedCards;
    private List<SavedVPA> savedVPAs;

    public SavedInstruments(List<SavedCard> savedCards, List<SavedVPA> savedVPAs) {
        this.savedCards = savedCards;
        this.savedVPAs = savedVPAs;
    }

    public SavedInstruments() {
    }

    public List<SavedCard> getSavedCards() {
        return savedCards;
    }

    public void setSavedCards(List<SavedCard> savedCards) {
        this.savedCards = savedCards;
    }

    public List<SavedVPA> getSavedVPAs() {
        return savedVPAs;
    }

    public void setSavedVPAs(List<SavedVPA> savedVPAs) {
        this.savedVPAs = savedVPAs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SavedInstruments{");
        sb.append("savedCards=").append(savedCards);
        sb.append(", savedVPAs=").append(savedVPAs);
        sb.append('}');
        return sb.toString();
    }
}
