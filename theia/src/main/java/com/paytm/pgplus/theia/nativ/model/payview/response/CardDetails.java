package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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

    @ApiModelProperty(required = true)
    @NotBlank
    private String cvvLength;

    @ApiModelProperty(required = true)
    @NotBlank
    private Boolean cvvRequired;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cardHash;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String firstEightDigit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean indian;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String zeroSuccessRate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String instName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countryCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String country;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countryCodeIso;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currency;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currencyCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currencyCodeIso;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String symbol;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currencyPrecision;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cardScheme;

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

    public String getCvvLength() {
        return cvvLength;
    }

    public void setCvvLength(String cvvLength) {
        this.cvvLength = cvvLength;
    }

    public Boolean getCvvRequired() {
        return cvvRequired;
    }

    public void setCvvRequired(Boolean cvvRequired) {
        this.cvvRequired = cvvRequired;
    }

    public String getcardHash() {
        return cardHash;
    }

    public void setcardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public String getFirstEightDigit() {
        return firstEightDigit;
    }

    public void setFirstEightDigit(String firstEightDigit) {
        this.firstEightDigit = firstEightDigit;
    }

    public Boolean getIndian() {
        return indian;
    }

    public void setIndian(Boolean indian) {
        this.indian = indian;
    }

    public String getZeroSuccessRate() {
        return zeroSuccessRate;
    }

    public void setZeroSuccessRate(String zeroSuccessRate) {
        this.zeroSuccessRate = zeroSuccessRate;
    }

    public String getInstName() {
        return instName;
    }

    public void setInstName(String instName) {
        this.instName = instName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCodeIso() {
        return countryCodeIso;
    }

    public void setCountryCodeIso(String countryCodeIso) {
        this.countryCodeIso = countryCodeIso;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCodeIso() {
        return currencyCodeIso;
    }

    public void setCurrencyCodeIso(String currencyCodeIso) {
        this.currencyCodeIso = currencyCodeIso;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCurrencyPrecision() {
        return currencyPrecision;
    }

    public void setCurrencyPrecision(String currencyPrecision) {
        this.currencyPrecision = currencyPrecision;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CardDetails{");
        sb.append("cardId='").append(cardId).append('\'');
        sb.append(", cardType='").append(cardType).append('\'');
        sb.append(", expiryDate='").append(expiryDate).append('\'');
        sb.append(", firstSixDigit='").append(firstSixDigit).append('\'');
        sb.append(", lastFourDigit='").append(lastFourDigit).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", updated_on='").append(updated_on).append('\'');
        sb.append(", created_on='").append(created_on).append('\'');
        sb.append(", cvvLength='").append(cvvLength).append('\'');
        sb.append(", cvvRequired='").append(cvvRequired).append('\'');
        sb.append(", indian='").append(indian).append('\'');
        sb.append(", zeroSuccessRate='").append(zeroSuccessRate).append('\'');
        sb.append(", instName='").append(instName).append('\'');
        sb.append(", countryCode='").append(countryCode).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", countryCodeIso='").append(countryCodeIso).append('\'');
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", currencyCode='").append(currencyCode).append('\'');
        sb.append(", currencyCodeIso='").append(currencyCodeIso).append('\'');
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", currencyPrecision='").append(currencyPrecision).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", cardScheme='").append(cardScheme).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
