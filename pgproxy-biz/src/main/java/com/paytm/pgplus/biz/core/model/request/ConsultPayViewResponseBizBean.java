/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.common.enums.ERequestType;

/**
 * @author namanjain
 *
 */
public class ConsultPayViewResponseBizBean implements Serializable {

    private static final long serialVersionUID = 12L;

    private String transDesc;
    private String currency = "INR";
    private String transAmount;
    private ERequestType productCode;
    private String securityId;
    private boolean chargePayer;
    private boolean loginMandatory;
    private Map<String, String> extendInfo;
    private boolean walletFailed;
    private List<PayMethodViewsBiz> payMethodViews;
    private boolean walletOnly;
    private String transCreatedTime;
    private boolean paymentsBankSupported;

    public String getTransDesc() {
        return transDesc;
    }

    public void setTransDesc(final String transDesc) {
        this.transDesc = transDesc;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(final String transAmount) {
        this.transAmount = transAmount;
    }

    public ERequestType getProductCode() {
        return productCode;
    }

    public void setProductCode(final ERequestType productCode) {
        this.productCode = productCode;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(final String securityId) {
        this.securityId = securityId;
    }

    public boolean isChargePayer() {
        return chargePayer;
    }

    public void setChargePayer(final boolean chargePayer) {
        this.chargePayer = chargePayer;
    }

    public boolean isLoginMandatory() {
        return loginMandatory;
    }

    public void setLoginMandatory(final boolean loginMandatory) {
        this.loginMandatory = loginMandatory;
    }

    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(final Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public List<PayMethodViewsBiz> getPayMethodViews() {
        return payMethodViews != null ? payMethodViews : Collections.emptyList();
    }

    public void setPayMethodViews(final List<PayMethodViewsBiz> payMethodViews) {
        this.payMethodViews = payMethodViews;
    }

    /**
     * @return the walletOnly
     */
    public boolean isWalletOnly() {
        return walletOnly;
    }

    /**
     * @param walletOnly
     *            the walletOnly to set
     */
    public void setWalletOnly(final boolean walletOnly) {
        this.walletOnly = walletOnly;
    }

    /**
     * @return the walletFailed
     */
    public boolean isWalletFailed() {
        return walletFailed;
    }

    /**
     * @param walletFailed
     *            the walletFailed to set
     */
    public void setWalletFailed(final boolean walletFailed) {
        this.walletFailed = walletFailed;
    }

    public String getTransCreatedTime() {
        return transCreatedTime;
    }

    public void setTransCreatedTime(String transCreatedTime) {
        this.transCreatedTime = transCreatedTime;
    }

    public boolean isPaymentsBankSupported() {
        return paymentsBankSupported;
    }

    public void setPaymentsBankSupported(boolean paymentsBankSupported) {
        this.paymentsBankSupported = paymentsBankSupported;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConsultPayViewResponseBizBean [transDesc=").append(transDesc).append(", currency=")
                .append(currency).append(", transAmount=").append(transAmount).append(", productCode=")
                .append(productCode).append(", securityId=").append(securityId).append(", chargePayer=")
                .append(chargePayer).append(", loginMandatory=").append(loginMandatory).append(", extendInfo=")
                .append(extendInfo).append(", walletFailed=").append(walletFailed).append(", payMethodViews=")
                .append(payMethodViews).append(", walletOnly=").append(walletOnly).append(", transCreatedTime=")
                .append(transCreatedTime).append(", paymentsBankSupported=").append(paymentsBankSupported).append("]");
        return builder.toString();
    }
}