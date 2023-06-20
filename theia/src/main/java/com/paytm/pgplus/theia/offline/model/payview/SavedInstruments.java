package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.user.models.response.UserProfileSarvatra;

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
    private List<String> sarvatraVpa;
    private UserProfileSarvatra sarvatraUserProfile;
    private int priority;

    public int getSarvatraVpaPriority() {
        return priority;
    }

    public void setSarvatraVpaPriority(int sarvatraVpaPriority) {
        this.priority = sarvatraVpaPriority;
    }

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

    public UserProfileSarvatra getSarvatraUserProfile() {
        return sarvatraUserProfile;
    }

    public void setSarvatraUserProfile(UserProfileSarvatra sarvatraUserProfile) {
        this.sarvatraUserProfile = sarvatraUserProfile;
    }

    public List<String> getSarvatraVpa() {
        return sarvatraVpa;
    }

    public void setSarvatraVpa(List<String> sarvatraVpa) {
        this.sarvatraVpa = sarvatraVpa;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SavedInstruments{");
        sb.append("savedCards=").append(savedCards);
        sb.append(", savedVPAs=").append(savedVPAs);
        sb.append(", sarvatraVpa=").append(sarvatraVpa);
        sb.append(", sarvatraUserProfile=").append(sarvatraUserProfile);
        sb.append('}');
        return sb.toString();
    }
}
