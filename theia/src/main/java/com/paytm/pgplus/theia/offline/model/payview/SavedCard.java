package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavedCard extends CreditCard {

    private static final long serialVersionUID = 7305134116997915974L;
    @NotNull
    @Valid
    private CardDetails cardDetails;

    private String issuingBank;

    private EmiChannel emiDetails;

    private List<String> authModes;

    private int priority;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean prepaidCard;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public SavedCard(CardDetails cardDetails) {
        this.cardDetails = cardDetails;
    }

    public SavedCard() {
    }

    public SavedCard(CardDetails cardDetails, String issuingBank) {
        this.cardDetails = cardDetails;
        this.issuingBank = issuingBank;
    }

    public CardDetails getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(CardDetails cardDetails) {
        this.cardDetails = cardDetails;
    }

    public String getIssuingBank() {
        return issuingBank;
    }

    public void setIssuingBank(String issuingBank) {
        this.issuingBank = issuingBank;
    }

    public EmiChannel getEmiDetails() {
        return emiDetails;
    }

    public void setEmiDetails(EmiChannel emiDetails) {
        this.emiDetails = emiDetails;
    }

    public List<String> getAuthModes() {
        return authModes;
    }

    public void setAuthModes(List<String> authModes) {
        this.authModes = authModes;
    }

    public Boolean getPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(Boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SavedCard [cardDetails=");
        builder.append(cardDetails);
        builder.append(", issuingBank=");
        builder.append(issuingBank);
        builder.append(", emiDetails=");
        builder.append(emiDetails);
        builder.append(", authModes=");
        builder.append(authModes);
        builder.append("]");
        return builder.toString();
    }
}
