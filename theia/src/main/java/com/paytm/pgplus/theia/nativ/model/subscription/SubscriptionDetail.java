package com.paytm.pgplus.theia.nativ.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SubscriptionDetail implements Serializable {

    @JsonProperty("isEnabled")
    private boolean enabled;

    private String subsType;
    private String subsId;
    @LocaleField
    private String renewMessage;
    private boolean saveCardMandatoryAddNPay = true;
    private boolean nonSpecificPayMode;
    private String amount;
    private String infoMessage;
    private boolean showDetails;
    private Map<String, String> details;
    private List<String> infoMessageList;
    private String subscriptionPurpose;
    private String startDate;
    private String endDate;
    private String maxAmount;
    private String subsfrequency;
    private String subsMaxAmount;
    private boolean isAutoRefund;

    public List<String> getInfoMessageList() {
        return infoMessageList;
    }

    public void setInfoMessageList(List<String> infoMessageList) {
        this.infoMessageList = infoMessageList;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSubsType() {
        return subsType;
    }

    public void setSubsType(String subsType) {
        this.subsType = subsType;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public String getSubsId() {
        return subsId;
    }

    public void setSubsId(String subsId) {
        this.subsId = subsId;
    }

    public String getRenewMessage() {
        return renewMessage;
    }

    public void setRenewMessage(String renewMessage) {
        this.renewMessage = renewMessage;
    }

    public boolean isSaveCardMandatoryAddNPay() {
        return saveCardMandatoryAddNPay;
    }

    public void setSaveCardMandatoryAddNPay(boolean saveCardMandatoryAddNPay) {
        this.saveCardMandatoryAddNPay = saveCardMandatoryAddNPay;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }

    public boolean isNonSpecificPayMode() {
        return nonSpecificPayMode;
    }

    public void setNonSpecificPayMode(boolean nonSpecificPayMode) {
        this.nonSpecificPayMode = nonSpecificPayMode;
    }

    public String getSubscriptionPurpose() {
        return subscriptionPurpose;
    }

    public void setSubscriptionPurpose(String subscriptionPurpose) {
        this.subscriptionPurpose = subscriptionPurpose;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(String maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getSubsfrequency() {
        return subsfrequency;
    }

    public void setSubsfrequency(String subsfrequency) {
        this.subsfrequency = subsfrequency;
    }

    public String getSubsMaxAmount() {
        return subsMaxAmount;
    }

    public void setSubsMaxAmount(String subsMaxAmount) {
        this.subsMaxAmount = subsMaxAmount;
    }

    @JsonProperty("isAutoRefund")
    public boolean isAutoRefund() {
        return isAutoRefund;
    }

    public void setAutoRefund(boolean isAutoRefund) {
        this.isAutoRefund = isAutoRefund;
    }

    public SubscriptionDetail() {
        this.setRenewMessage(StringUtils.EMPTY);
        this.setInfoMessage(StringUtils.EMPTY);
        this.setSubsType(StringUtils.EMPTY);
        this.setSubsId(StringUtils.EMPTY);
        this.setAmount(StringUtils.EMPTY);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
