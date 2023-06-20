/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class ConsultFeeRequest implements Serializable {

    private static final long serialVersionUID = -5344801287503396517L;

    private BigDecimal transactionAmount;
    private String merchantId;
    private ERequestType transactionType;
    private List<EPayMethod> payMethods;
    private BigDecimal walletBalance;
    private String transCreatedTime;
    private boolean slabBasedMDR;
    private List<String> instId;
    private boolean dynamicFeeMerchant;
    private List<FeeRateFactors> feeRateFactors;
    private ProductCodes productCode;
    private Routes route;
    private String userId;
    private boolean isAddMoneyPcfEnabled;
    private BigDecimal addMoneyAmount;
    private String feeRateCode;
    private String txnType;
    private String addNPayProductCode;
    private boolean isAddNPay;

    /**
     * @return the transactionAmount
     */
    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    /**
     * @param transactionAmount
     *            the transactionAmount to set
     */
    public void setTransactionAmount(final BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    /**
     * @return the merchantId
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * @param merchantId
     *            the merchantId to set
     */
    public void setMerchantId(final String merchantId) {
        this.merchantId = merchantId;
    }

    /**
     * @return the transactionType
     */
    public ERequestType getTransactionType() {
        return transactionType;
    }

    /**
     * @param transactionType
     *            the transactionType to set
     */
    public void setTransactionType(final ERequestType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * @return the payMethods
     */
    public List<EPayMethod> getPayMethods() {
        return payMethods;
    }

    public void setPayMethods(List<EPayMethod> payMethods) {
        this.payMethods = payMethods;
    }

    public String getTransCreatedTime() {
        return transCreatedTime;
    }

    public void setTransCreatedTime(String transCreatedTime) {
        this.transCreatedTime = transCreatedTime;
    }

    public boolean isSlabBasedMDR() {
        return slabBasedMDR;
    }

    public void setSlabBasedMDR(boolean slabBasedMDR) {
        this.slabBasedMDR = slabBasedMDR;
    }

    public List<String> getInstId() {
        return instId;
    }

    public void setInstId(List<String> instId) {
        this.instId = instId;
    }

    public boolean isDynamicFeeMerchant() {
        return dynamicFeeMerchant;
    }

    public void setDynamicFeeMerchant(boolean dynamicFeeMerchant) {
        this.dynamicFeeMerchant = dynamicFeeMerchant;
    }

    public List<FeeRateFactors> getFeeRateFactors() {
        return feeRateFactors;
    }

    public void setFeeRateFactors(List<FeeRateFactors> feeRateFactors) {
        this.feeRateFactors = feeRateFactors;
    }

    public ProductCodes getProductCode() {
        return productCode;
    }

    public void setProductCode(ProductCodes productCode) {
        this.productCode = productCode;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAddMoneyPcfEnabled() {
        return isAddMoneyPcfEnabled;
    }

    public void setAddMoneyPcfEnabled(boolean addMoneyPcfEnabled) {
        isAddMoneyPcfEnabled = addMoneyPcfEnabled;
    }

    public BigDecimal getAddMoneyAmount() {
        return addMoneyAmount;
    }

    public void setAddMoneyAmount(BigDecimal addMoneyAmount) {
        this.addMoneyAmount = addMoneyAmount;
    }

    public String getFeeRateCode() {
        return feeRateCode;
    }

    public void setFeeRateCode(String feeRateCode) {
        this.feeRateCode = feeRateCode;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getAddNPayProductCode() {
        return addNPayProductCode;
    }

    public void setAddNPayProductCode(String addNPayProductCode) {
        this.addNPayProductCode = addNPayProductCode;
    }

    public boolean isAddNPay() {
        return isAddNPay;
    }

    public void setAddNPay(boolean addNPay) {
        isAddNPay = addNPay;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConsultFeeRequest [transactionAmount=").append(transactionAmount).append(", merchantId=")
                .append(merchantId).append(", transactionType=").append(transactionType).append(", payMethods=")
                .append(payMethods).append(", walletBalance=").append(walletBalance).append(", transCreatedTime=")
                .append(transCreatedTime).append(", slabBasedMDR=").append(slabBasedMDR)
                .append(", dynamicFeeMerchant=").append(dynamicFeeMerchant).append(", addMoneyAmount=")
                .append(addMoneyAmount).append(", userId=").append(userId).append(", isAddMoneyPcfEnabled=")
                .append(isAddMoneyPcfEnabled).append(", feeRateCode=").append(feeRateCode).append(", txnType=")
                .append(txnType).append(", addNPayProductCode=").append(addNPayProductCode).append(", isAddNPay=")
                .append(isAddNPay).append("]");
        return builder.toString();
    }

}