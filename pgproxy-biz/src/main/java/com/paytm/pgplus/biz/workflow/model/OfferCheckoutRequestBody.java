package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * @author harshwardhan
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferCheckoutRequestBody implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1174880381274318322L;

    private String amount;
    private String mid;
    private String bin;
    private String userMobile;
    private String validationKey;
    private String validationValue;
    private String planId;
    private String pgPlanId;
    private String months;
    private String emiAmount;
    private String emiTotalAmount;
    private String interestRate;
    private String brandId;
    private String brandName;
    private String categoryId;
    private String categoryName;
    private String productId;
    private String productName;
    private String model;
    private String modelName;
    private String ean;
    private String quantity;
    private String verticalId;
    private String isEmiEnabled;
    private String offerId;
    private String skuCode;
    private String offerAmount;
    private String validationMode;
    private String tenure;
    private String tid;
    private String time;
    private String year;
    private String date;
    private String invoiceNumber;
    @Mask(prefixNoMaskLen = 2, suffixNoMaskLen = 2)
    private String cardIndexNumber;
    private String productCode;
    private String orderID;
    private String bankVerificationCode;
    private String brandVerificationCode;
    private List<String> velocityOfferId;
    private String clientOrderCreatedAt;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getValidationKey() {
        return validationKey;
    }

    public void setValidationKey(String validationKey) {
        this.validationKey = validationKey;
    }

    public String getValidationValue() {
        return validationValue;
    }

    public void setValidationValue(String validationValue) {
        this.validationValue = validationValue;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPgPlanId() {
        return pgPlanId;
    }

    public void setPgPlanId(String pgPlanId) {
        this.pgPlanId = pgPlanId;
    }

    public String getMonths() {
        return months;
    }

    public void setMonths(String months) {
        this.months = months;
    }

    public String getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(String emiAmount) {
        this.emiAmount = emiAmount;
    }

    public String getEmiTotalAmount() {
        return emiTotalAmount;
    }

    public void setEmiTotalAmount(String emiTotalAmount) {
        this.emiTotalAmount = emiTotalAmount;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getVerticalId() {
        return verticalId;
    }

    public void setVerticalId(String verticalId) {
        this.verticalId = verticalId;
    }

    public String getIsEmiEnabled() {
        return isEmiEnabled;
    }

    public void setIsEmiEnabled(String isEmiEnabled) {
        this.isEmiEnabled = isEmiEnabled;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(String offerAmount) {
        this.offerAmount = offerAmount;
    }

    public String getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(String validationMode) {
        this.validationMode = validationMode;
    }

    public String getTenure() {
        return tenure;
    }

    public void setTenure(String tenure) {
        this.tenure = tenure;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getCardIndexNumber() {
        return cardIndexNumber;
    }

    public void setCardIndexNumber(String cardIndexNumber) {
        this.cardIndexNumber = cardIndexNumber;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getBankVerificationCode() {
        return bankVerificationCode;
    }

    public void setBankVerificationCode(String bankVerificationCode) {
        this.bankVerificationCode = bankVerificationCode;
    }

    public String getBrandVerificationCode() {
        return brandVerificationCode;
    }

    public void setBrandVerificationCode(String brandVerificationCode) {
        this.brandVerificationCode = brandVerificationCode;
    }

    public List<String> getVelocityOfferId() {
        return velocityOfferId;
    }

    public void setVelocityOfferId(List<String> velocityOfferId) {
        this.velocityOfferId = velocityOfferId;
    }

    public String getClientOrderCreatedAt() {
        return clientOrderCreatedAt;
    }

    public void setClientOrderCreatedAt(String clientOrderCreatedAt) {
        this.clientOrderCreatedAt = clientOrderCreatedAt;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
