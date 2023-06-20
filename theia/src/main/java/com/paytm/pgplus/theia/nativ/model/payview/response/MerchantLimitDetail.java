package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.mappingserviceclient.model.MerchantLimit;

import java.io.Serializable;
import java.util.List;

public class MerchantLimitDetail implements Serializable {

    private static final long serialVersionUID = -1865374468543252499L;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String remainingLimit;

    private List<MerchantRemainingLimit> merchantRemainingLimits;

    private List<EPayMethod> excludedPaymodes;

    private String message;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Integer merchantLimitType;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private List<MerchantLimit> merchantPaymodesLimits;

    public List<EPayMethod> getExcludedPaymodes() {
        return excludedPaymodes;
    }

    public void setExcludedPaymodes(List<EPayMethod> excludedPaymodes) {
        this.excludedPaymodes = excludedPaymodes;
    }

    public List<MerchantRemainingLimit> getMerchantRemainingLimits() {
        return merchantRemainingLimits;
    }

    public void setMerchantRemainingLimits(List<MerchantRemainingLimit> merchantRemainingLimits) {
        this.merchantRemainingLimits = merchantRemainingLimits;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getMerchantLimitType() {
        return merchantLimitType;
    }

    public void setMerchantLimitType(Integer merchantLimitType) {
        this.merchantLimitType = merchantLimitType;
    }

    public List<MerchantLimit> getMerchantPaymodesLimits() {
        return merchantPaymodesLimits;
    }

    public void setMerchantPaymodesLimits(List<MerchantLimit> merchantPaymodesLimits) {
        this.merchantPaymodesLimits = merchantPaymodesLimits;
    }

    public String getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(String remainingLimit) {
        this.remainingLimit = remainingLimit;
    }

    @Override
    public String toString() {
        return "MerchantLimitDetail{" + "remainingLimit=" + remainingLimit + "merchantRemainingLimits="
                + merchantRemainingLimits + ", excludedPaymodes=" + excludedPaymodes + ", message='" + message
                + ", merchantLimitType='" + merchantLimitType + ", merchantPaymodesLimits='" + merchantPaymodesLimits
                + '\'' + '}';
    }
}
