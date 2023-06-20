package com.paytm.pgplus.theia.nativ.model.pcfDetails;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.payment.models.PayChannelOptionView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchPcfDetailRequestBody implements Serializable {

    private static final long serialVersionUID = -6399229634106555756L;
    private String txnAmount;
    private String merchantId;
    private List<PayChannelOptionView> payMethods;
    private boolean slabBasedMDR;
    private String mid;
    private boolean dynamicFeeMerchant;
    private Map<EPayMethod, Map<String, String>> payMethodFeeRateFactors;
    private String referenceId;
    private String bin;

    private boolean useAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    private String txnType;

    private String ssoToken;

    private String productCode;

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public List<PayChannelOptionView> getPayMethods() {
        return payMethods;
    }

    public void setPayMethods(List<PayChannelOptionView> payMethods) {
        this.payMethods = payMethods;
    }

    public boolean isSlabBasedMDR() {
        return slabBasedMDR;
    }

    public void setSlabBasedMDR(boolean slabBasedMDR) {
        this.slabBasedMDR = slabBasedMDR;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public boolean isDynamicFeeMerchant() {
        return dynamicFeeMerchant;
    }

    public void setDynamicFeeMerchant(boolean dynamicFeeMerchant) {
        this.dynamicFeeMerchant = dynamicFeeMerchant;
    }

    public Map<EPayMethod, Map<String, String>> getPayMethodFeeRateFactors() {
        return payMethodFeeRateFactors;
    }

    public void setPayMethodFeeRateFactors(Map<EPayMethod, Map<String, String>> payMethodFeeRateFactors) {
        this.payMethodFeeRateFactors = payMethodFeeRateFactors;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isUseAmount() {
        return useAmount;
    }

    public void setUseAmount(boolean useAmount) {
        this.useAmount = useAmount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    @Override
    public String toString() {
        return "FetchPcfDetailRequestBody{" + "txnAmount=" + txnAmount + ", merchantId='" + merchantId + '\''
                + ", payMethods=" + payMethods + ", slabBasedMDR=" + slabBasedMDR + ", dynamicFeeMerchant="
                + dynamicFeeMerchant + ", userId=" + userId + ", txnType=" + txnType + ", ssoToken=" + ssoToken
                + ", productCode=" + productCode + '}';
    }

}
