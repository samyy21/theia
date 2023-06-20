package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.payment.models.PayMethodView;
import com.paytm.pgplus.facade.payment.models.response.MerchantRemainingLimit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by rahulverma on 8/9/17.
 */
public class LitePayviewConsultResponseBizBean implements Serializable {

    private static final long serialVersionUID = -5404238790011417229L;

    private List<PayMethodViewsBiz> payMethodViews;

    private boolean chargePayer;

    private List<String> tolerableErrorCodes;

    private Map<String, String> extendInfo;

    private boolean paymentsBankSupported;

    private String currency = "INR";

    private boolean loginMandatory;

    private boolean walletOnly;

    private boolean walletFailed;

    private String pwpEnabled;

    private Map<String, Boolean> channelCoftPayment;

    private String sourceSystem;

    private List<MerchantRemainingLimit> merchantRemainingLimits;

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public List<MerchantRemainingLimit> getMerchantRemainingLimits() {
        return merchantRemainingLimits;
    }

    public void setMerchantRemainingLimits(List<MerchantRemainingLimit> merchantRemainingLimits) {
        this.merchantRemainingLimits = merchantRemainingLimits;
    }

    public LitePayviewConsultResponseBizBean() {
    }

    public LitePayviewConsultResponseBizBean(List<PayMethodViewsBiz> payMethodViews, boolean chargePayer,
            List<String> tolerableErrorCodes, Map<String, String> extendInfo) {
        this.payMethodViews = payMethodViews;
        this.chargePayer = chargePayer;
        this.tolerableErrorCodes = tolerableErrorCodes;
        this.extendInfo = extendInfo;
    }

    public List<PayMethodViewsBiz> getPayMethodViews() {
        return payMethodViews;
    }

    public void setPayMethodViews(List<PayMethodViewsBiz> payMethodViews) {
        this.payMethodViews = payMethodViews;
    }

    public boolean isChargePayer() {
        return chargePayer;
    }

    public void setChargePayer(boolean chargePayer) {
        this.chargePayer = chargePayer;
    }

    public List<String> getTolerableErrorCodes() {
        return tolerableErrorCodes;
    }

    public void setTolerableErrorCodes(List<String> tolerableErrorCodes) {
        this.tolerableErrorCodes = tolerableErrorCodes;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public boolean isPaymentsBankSupported() {
        return paymentsBankSupported;
    }

    public void setPaymentsBankSupported(boolean paymentsBankSupported) {
        this.paymentsBankSupported = paymentsBankSupported;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isLoginMandatory() {
        return loginMandatory;
    }

    public void setLoginMandatory(boolean loginMandatory) {
        this.loginMandatory = loginMandatory;
    }

    public boolean isWalletOnly() {
        return walletOnly;
    }

    public void setWalletOnly(boolean walletOnly) {
        this.walletOnly = walletOnly;
    }

    public boolean isWalletFailed() {
        return walletFailed;
    }

    public void setWalletFailed(boolean walletFailed) {
        this.walletFailed = walletFailed;
    }

    public String getPwpEnabled() {
        return pwpEnabled;
    }

    public void setPwpEnabled(String pwpEnabled) {
        this.pwpEnabled = pwpEnabled;
    }

    public Map<String, Boolean> getChannelCoftPayment() {
        return channelCoftPayment;
    }

    public void setChannelCoftPayment(Map<String, Boolean> channelCoftPayment) {
        this.channelCoftPayment = channelCoftPayment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LitePayviewConsultResponseBizBean that = (LitePayviewConsultResponseBizBean) o;
        return chargePayer == that.chargePayer && Objects.equals(payMethodViews, that.payMethodViews)
                && Objects.equals(tolerableErrorCodes, that.tolerableErrorCodes)
                && Objects.equals(extendInfo, that.extendInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payMethodViews, chargePayer, tolerableErrorCodes, extendInfo);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LitePayviewConsultResponseBizBean{");
        sb.append("payMethodViews=").append(payMethodViews);
        sb.append(", chargePayer=").append(chargePayer);
        sb.append(", tolerableErrorCodes=").append(tolerableErrorCodes);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append(", paymentsBankSupported=").append(paymentsBankSupported);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", loginMandatory=").append(loginMandatory);
        sb.append(", walletOnly=").append(walletOnly);
        sb.append(", walletFailed=").append(walletFailed);
        sb.append(", pwpEnabled=").append(pwpEnabled);
        sb.append(", merchantRemainingLimits=").append(merchantRemainingLimits);
        sb.append('}');
        return sb.toString();
    }
}
