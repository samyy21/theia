package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierPageMerchantInfo implements Serializable {

    private static final long serialVersionUID = 771315761453785551L;

    private String mid;

    private String name;

    private String logo;

    private String merchantVpa;

    private String addMoneyMerchantVpa;

    @JsonProperty("isOnus")
    private boolean onus;

    @JsonProperty("isAppInvokeAllowed")
    private boolean appInvokeAllowed;

    @JsonProperty("isLocalStorageAllowedForLastPayMode")
    private boolean localStorageAllowedForLastPayMode;

    @JsonProperty("enableCustomerFeedback")
    private boolean customerFeedbackEnabled;

    public EnhancedCashierPageMerchantInfo(String mid, String name, String logo) {
        this.mid = mid;
        this.name = name;
        this.logo = logo;
    }

    public EnhancedCashierPageMerchantInfo(String mid, String name, String logo, String merchantVpa) {
        this.mid = mid;
        this.name = name;
        this.logo = logo;
        this.merchantVpa = merchantVpa;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getMerchantVpa() {
        return merchantVpa;
    }

    public void setMerchantVpa(String merchantVpa) {
        this.merchantVpa = merchantVpa;
    }

    public boolean isOnus() {
        return onus;
    }

    public void setOnus(boolean onus) {
        this.onus = onus;
    }

    public String getAddMoneyMerchantVpa() {
        return addMoneyMerchantVpa;
    }

    public void setAddMoneyMerchantVpa(String addMoneyMerchantVpa) {
        this.addMoneyMerchantVpa = addMoneyMerchantVpa;
    }

    public boolean isAppInvokeAllowed() {
        return appInvokeAllowed;
    }

    public void setAppInvokeAllowed(boolean appInvokeAllowed) {
        this.appInvokeAllowed = appInvokeAllowed;
    }

    public boolean isLocalStorageAllowedForLastPayMode() {
        return localStorageAllowedForLastPayMode;
    }

    public void setLocalStorageAllowedForLastPayMode(boolean localStorageAllowedForLastPayMode) {
        this.localStorageAllowedForLastPayMode = localStorageAllowedForLastPayMode;
    }

    public boolean isCustomerFeedbackEnabled() {
        return customerFeedbackEnabled;
    }

    public void setCustomerFeedbackEnabled(boolean customerFeedbackEnabled) {
        this.customerFeedbackEnabled = customerFeedbackEnabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnhancedCashierPageMerchantInfo [mid=").append(mid).append(", name=").append(name)
                .append(", logo=").append(logo).append(", merchantVpa=").append(merchantVpa).append(", onus=")
                .append(onus).append(", addMoneyMerchantVpa=").append(addMoneyMerchantVpa)
                .append(", appInvokeEnabled=").append(appInvokeAllowed)
                .append(", isLocalStorageAllowedForLastPayMode=").append(localStorageAllowedForLastPayMode).append("]");
        return builder.toString();
    }

}
