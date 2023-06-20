package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.facade.payment.models.PayOptionRemainingLimits;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EnhancedCashierPagePayModeBase implements Serializable {

    private static final long serialVersionUID = 1097852499479493305L;

    private int id;
    @LocaleField
    private String name;
    private String type;
    private boolean selected;
    @JsonProperty("isHybridDisabled")
    private boolean hybridDisabled;

    @JsonProperty("pcf")
    private PCFFeeCharges pcfFeeCharges;

    private String payMethod;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remainingLimit;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PayOptionRemainingLimits> payOptionRemainingLimits;

    public PCFFeeCharges getPcfFeeCharges() {
        return pcfFeeCharges;
    }

    public void setPcfFeeCharges(PCFFeeCharges pcfFeeCharges) {
        this.pcfFeeCharges = pcfFeeCharges;
    }

    @JsonProperty("isOnboarding")
    private boolean onboarding;

    public EnhancedCashierPagePayModeBase(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public EnhancedCashierPagePayModeBase(int id, String name, String type, boolean hybridDisabled,
            String remainingLimit, List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.hybridDisabled = hybridDisabled;
        this.remainingLimit = remainingLimit;
        this.payOptionRemainingLimits = payOptionRemainingLimits;
    }

    public EnhancedCashierPagePayModeBase(int id, String name, String type, boolean hybridDisabled, boolean onboarding,
            String remainingLimit, List<PayOptionRemainingLimits> payOptionRemainingLimits) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.hybridDisabled = hybridDisabled;
        this.onboarding = onboarding;
        this.remainingLimit = remainingLimit;
        this.payOptionRemainingLimits = payOptionRemainingLimits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isHybridDisabled() {
        return hybridDisabled;
    }

    public void setHybridDisabled(boolean hybridDisabled) {
        this.hybridDisabled = hybridDisabled;
    }

    public boolean isOnboarding() {
        return onboarding;
    }

    public void setOnboarding(boolean onboarding) {
        this.onboarding = onboarding;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
