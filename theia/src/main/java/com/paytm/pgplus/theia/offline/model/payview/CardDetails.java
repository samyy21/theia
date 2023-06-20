package com.paytm.pgplus.theia.offline.model.payview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by rahulverma on 1/9/17.
 */
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDetails implements Serializable {

    private static final long serialVersionUID = -7713607496084343855L;

    @ApiModelProperty(required = true)
    @NotBlank
    private String cardId;

    @ApiModelProperty(required = true)
    @NotBlank
    private String cardNumber;

    @ApiModelProperty(required = true)
    @NotBlank
    private String cardType;

    @ApiModelProperty(required = true)
    @NotBlank
    private String expiryDate;

    @ApiModelProperty(required = true)
    @NotBlank
    private String firstSixDigit;

    @ApiModelProperty(required = true)
    @NotBlank
    private String lastFourDigit;

    @ApiModelProperty(required = true)
    @NotBlank
    private String status;

    @ApiModelProperty(required = true)
    @NotBlank
    private String userId;

    @ApiModelProperty(required = true)
    @NotBlank
    @JsonIgnore
    private String updated_on;

    @ApiModelProperty(required = true)
    @NotBlank
    @JsonIgnore
    private String created_on;

    public CardDetails() {
    }

    public String getCardId() {
        return this.cardId;
    }

    @JsonIgnore
    public String getCardNumber() {
        return this.cardNumber;
    }

    public String getCardType() {
        return this.cardType;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public String getFirstSixDigit() {
        return this.firstSixDigit;
    }

    public String getLastFourDigit() {
        return this.lastFourDigit;
    }

    public String getStatus() {
        return this.status;
    }

    @JsonIgnore
    public String getUserId() {
        return this.userId;
    }

    public String getUpdated_on() {
        return this.updated_on;
    }

    public String getCreated_on() {
        return this.created_on;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    @JsonProperty
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setFirstSixDigit(String firstSixDigit) {
        this.firstSixDigit = firstSixDigit;
    }

    public void setLastFourDigit(String lastFourDigit) {
        this.lastFourDigit = lastFourDigit;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUpdated_on(String updated_on) {
        this.updated_on = updated_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CardDetails{");
        sb.append("cardId='").append(cardId).append('\'');
        sb.append(", cardNumber='").append(cardNumber).append('\'');
        sb.append(", cardType='").append(cardType).append('\'');
        sb.append(", expiryDate='").append(expiryDate).append('\'');
        sb.append(", firstSixDigit='").append(firstSixDigit).append('\'');
        sb.append(", lastFourDigit='").append(lastFourDigit).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", updated_on='").append(updated_on).append('\'');
        sb.append(", created_on='").append(created_on).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
