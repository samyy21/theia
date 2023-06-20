package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.payment.models.PayOptionRemainingLimits;
import com.paytm.pgplus.facade.user.models.LrnDetail;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayMethod implements Serializable {

    private static final long serialVersionUID = 4853871828367608617L;

    @ApiModelProperty(required = true)
    @NotBlank
    @JsonProperty("paymentMode")
    private String payMethod;

    @ApiModelProperty(required = true)
    @NotBlank
    @LocaleField(localeEnum = EPayMethod.class, methodName = "getNewDisplayName")
    private String displayName;

    @Valid
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = StatusInfoFilter.class)
    private StatusInfo isDisabled;

    @Valid
    private List<PayChannelBase> payChannelOptions;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1Filter.class)
    private BigDecimal feeAmount;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1Filter.class)
    private BigDecimal taxAmount;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1Filter.class)
    private BigDecimal totalTransactionAmount;

    @Valid
    @JsonProperty("isHybridDisabled")
    private boolean hybridDisabled;

    private boolean isOnboarding;

    private String priority;

    @JsonIgnore
    private List<String> enabledBanks;

    @JsonIgnore
    private List<String> enabledPayChannels;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String storeFrontUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean prepaidCardSupported;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("isNewUser")
    private Boolean newUser;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ActiveInfo isActive;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LrnDetail lrnDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remainingLimit;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PayOptionRemainingLimits> payOptionRemainingLimits;

    public PayMethod() {
    }

    public String getPayMethod() {
        return this.payMethod;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public StatusInfo getIsDisabled() {
        return this.isDisabled;
    }

    public List<PayChannelBase> getPayChannelOptions() {
        return this.payChannelOptions;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setIsDisabled(StatusInfo isDisabled) {
        this.isDisabled = isDisabled;
    }

    public void setPayChannelOptions(List<PayChannelBase> payChannelOptions) {
        this.payChannelOptions = payChannelOptions;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    public void setTotalTransactionAmount(BigDecimal totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
    }

    public boolean isHybridDisabled() {
        return hybridDisabled;
    }

    public void setHybridDisabled(boolean hybridDisabled) {
        this.hybridDisabled = hybridDisabled;
    }

    public boolean isOnboarding() {
        return isOnboarding;
    }

    public void setOnboarding(boolean onboarding) {
        isOnboarding = onboarding;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getEnabledBanks() {
        return enabledBanks;
    }

    public void setEnabledBanks(List<String> enabledBanks) {
        this.enabledBanks = enabledBanks;
    }

    public List<String> getEnabledPayChannels() {
        return enabledPayChannels;
    }

    public void setEnabledPayChannels(List<String> enablePayChannels) {
        this.enabledPayChannels = enablePayChannels;
    }

    public String getStoreFrontUrl() {
        return storeFrontUrl;
    }

    public void setStoreFrontUrl(String storeFrontUrl) {
        this.storeFrontUrl = storeFrontUrl;
    }

    public Boolean getPrepaidCardSupported() {
        return prepaidCardSupported;
    }

    public void setPrepaidCardSupported(Boolean prepaidCardSupported) {
        this.prepaidCardSupported = prepaidCardSupported;
    }

    public Boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }

    public ActiveInfo getIsActive() {
        return isActive;
    }

    public void setIsActive(ActiveInfo isActive) {
        this.isActive = isActive;
    }

    public LrnDetail getLrnDetails() {
        return lrnDetails;
    }

    public void setLrnDetails(LrnDetail lrnDetails) {
        this.lrnDetails = lrnDetails;
    }

    public String getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(String remainingLimit) {
        this.remainingLimit = remainingLimit;
    }

    public List<PayOptionRemainingLimits> getPayOptionRemainingLimits() {
        return payOptionRemainingLimits;
    }

    public void setPayOptionRemainingLimits(List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        this.payOptionRemainingLimits = payOptionRemainingLimits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayMethod{");
        sb.append("payMethod='").append(payMethod).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", isDisabled=").append(isDisabled);
        sb.append(", payChannelOptions=").append(payChannelOptions);
        sb.append(", hybridDisabled=").append(hybridDisabled);
        sb.append(", isOnboarding=").append(isOnboarding);
        sb.append(", priority=").append(priority);
        sb.append(", storeFrontUrl=").append(storeFrontUrl);
        sb.append(", isNewUser=").append(newUser);
        sb.append(", isActive=").append(isActive);
        sb.append(",lrnDetails=").append(lrnDetails);
        sb.append(", remainingLimit=").append(remainingLimit);
        sb.append(", payOptionRemainingLimits=").append(payOptionRemainingLimits);
        sb.append('}');
        return sb.toString();
    }
}
