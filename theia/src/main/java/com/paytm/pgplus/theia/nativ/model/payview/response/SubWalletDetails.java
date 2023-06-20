package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubWalletDetails implements Serializable {

    private static final long serialVersionUID = 7307821696638048424L;

    private String id;
    @LocaleField
    private String displayName;
    private String balance;
    private String imageUrl;
    private String walletType;
    private String subWalletType;
    private String status;
    private String message;
    private String expiryDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public String getSubWalletType() {
        return subWalletType;
    }

    public void setSubWalletType(String subWalletType) {
        this.subWalletType = subWalletType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubWalletDetailsList [id=").append(id).append(", displayName=").append(displayName)
                .append(", expiryDate=").append(expiryDate).append(", imageUrl=").append(imageUrl).append(", status=")
                .append(status).append(", message=").append(message).append(", walletType=").append(walletType)
                .append(", subWalletType=").append(subWalletType).append(", balance=").append(balance).append("]");
        return builder.toString();
    }
}
