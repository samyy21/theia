package com.paytm.pgplus.theia.nativ.model.common;

import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.customaspects.annotations.Mask;

import java.io.Serializable;

public class LinkAppPushData implements Serializable {

    private static final long serialVersionUID = -102459632296863918L;

    private String description;
    private String logoutUrl;
    private String linkType;
    @Mask
    private String accountNumber;
    private boolean displayWarningMessage;
    private Boolean isLocationPopUpDisabled;
    private EdcEmiLinkPaymentDetails edcEmiLinkPaymentDetails;
    private String linkId;
    private String linkName;

    public LinkAppPushData() {
    }

    public LinkAppPushData(String description, String logoutUrl, String linkType) {
        this.description = description;
        this.logoutUrl = logoutUrl;
        this.linkType = linkType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public boolean isDisplayWarningMessage() {
        return displayWarningMessage;
    }

    public void setDisplayWarningMessage(boolean displayWarningMessage) {
        this.displayWarningMessage = displayWarningMessage;
    }

    public Boolean getLocationPopUpDisabled() {
        return isLocationPopUpDisabled;
    }

    public void setLocationPopUpDisabled(Boolean locationPopUpDisabled) {
        isLocationPopUpDisabled = locationPopUpDisabled;
    }

    public EdcEmiLinkPaymentDetails getEdcEmiLinkPaymentDetails() {
        return edcEmiLinkPaymentDetails;
    }

    public void setEdcEmiLinkPaymentDetails(EdcEmiLinkPaymentDetails edcEmiLinkPaymentDetails) {
        this.edcEmiLinkPaymentDetails = edcEmiLinkPaymentDetails;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
