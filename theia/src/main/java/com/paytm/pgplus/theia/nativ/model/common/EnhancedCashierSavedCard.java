package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierSavedCard extends EnhancedCashierPagePayModeBase {
    private static final long serialVersionUID = -1255882685722720550L;

    public EnhancedCashierSavedCard(int id, String name, String type, List<SavedInstrument> savedCards) {
        super(id, name, type);
        this.savedCards = savedCards;
    }

    private List<SavedInstrument> savedCards;

    public List<SavedInstrument> getSavedCards() {
        return savedCards;
    }

    public void setSavedCards(List<SavedInstrument> savedCards) {
        this.savedCards = savedCards;
    }

    @Override
    public String toString() {
        return "EnhancedCashierSavedCard{" + "savedCards=" + savedCards + '}';
    }
}
