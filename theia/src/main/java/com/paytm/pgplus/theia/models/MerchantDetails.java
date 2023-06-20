package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import com.paytm.pgplus.theia.nativ.model.payview.response.V1NullFilter;

import java.io.Serializable;

/**
 * @author Vivek Kumar
 * @since 10-Jan-2018
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantDetails implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 9000513770791749269L;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1NullFilter.class)
    private String mcc;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1NullFilter.class)
    private String merchantVpa;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1NullFilter.class)
    private String merchantName;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String merchantDisplayName;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = V1NullFilter.class)
    private String merchantLogo;
    private boolean isAppInvokeAllowed;
    private boolean isAutoAppInvokeAllowed;
    private boolean isVerifiedMerchant;
    private String merchantBankName;

    @JsonProperty("isAOAMerchant")
    @JsonInclude(Include.NON_NULL)
    private Boolean aoaMerchant;

    public MerchantDetails() {

    }

    public MerchantDetails(String mcc, String merchantVpa, String merchantName) {
        this.mcc = mcc;
        this.merchantVpa = merchantVpa;
        this.merchantName = merchantName;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMerchantVpa() {
        return merchantVpa;
    }

    public void setMerchantVpa(String merchantVpa) {
        this.merchantVpa = merchantVpa;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantLogo() {
        return merchantLogo;
    }

    public void setMerchantLogo(String merchantLogo) {
        this.merchantLogo = merchantLogo;
    }

    public boolean getIsAppInvokeAllowed() {
        return isAppInvokeAllowed;
    }

    public void setIsAppInvokeAllowed(boolean isAppInvokeAllowed) {
        this.isAppInvokeAllowed = isAppInvokeAllowed;
    }

    public String getMerchantDisplayName() {
        return merchantDisplayName;
    }

    public void setMerchantDisplayName(String merchantDisplayName) {
        this.merchantDisplayName = merchantDisplayName;
    }

    public boolean isVerifiedMerchant() {
        return isVerifiedMerchant;
    }

    public void setVerifiedMerchant(boolean verifiedMerchant) {
        isVerifiedMerchant = verifiedMerchant;
    }

    public String getMerchantBankName() {
        return merchantBankName;
    }

    public void setMerchantBankName(String merchantBankName) {
        this.merchantBankName = merchantBankName;
    }

    public boolean isAutoAppInvokeAllowed() {
        return isAutoAppInvokeAllowed;
    }

    public void setAutoAppInvokeAllowed(boolean autoAppInvokeAllowed) {
        isAutoAppInvokeAllowed = autoAppInvokeAllowed;
    }

    @JsonProperty("isAOAMerchant")
    public Boolean getAoaMerchant() {
        return aoaMerchant;
    }

    public void setAoaMerchant(Boolean aoaMerchant) {
        this.aoaMerchant = aoaMerchant;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
