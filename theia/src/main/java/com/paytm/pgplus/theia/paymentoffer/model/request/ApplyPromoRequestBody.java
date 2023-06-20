package com.paytm.pgplus.theia.paymentoffer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.models.PromoCartDetails;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyPromoRequestBody implements Serializable {

    private static final long serialVersionUID = 6104978415829652989L;
    @NotBlank
    private String mid;

    private String custId;

    private String promocode;
    @NotEmpty
    @Valid
    private List<PromoPaymentOption> paymentOptions;
    @NotBlank(message = "amount cannot be blank")
    @Length(max = 16, message = "Amount cannot be longer thann 16 characters")
    @Pattern(regexp = "[0-9]+([.][0-9]{1,2})?")
    private String totalTransactionAmount;

    private String orderId;

    @JsonIgnore
    private String paytmUserId;

    @JsonIgnore
    private boolean promoForPCFMerchant;

    private Map<String, String> promoContext;

    // To be populated in case merchant sends multiple promocodes to be applied
    // on an order
    private List<PromoDetails> promoInfo;

    private String encUserId; // to be used for upi promo

    @JsonIgnore
    private Boolean changeRedemptionTypetoCashBack;

    private PromoCartDetails cartDetails;

    public String getPromocode() {
        return promocode;
    }

    public void setPromocode(String promocode) {
        this.promocode = promocode;
    }

    public List<PromoPaymentOption> getPaymentOptions() {
        return paymentOptions;
    }

    public void setPaymentOptions(List<PromoPaymentOption> paymentOptions) {
        this.paymentOptions = paymentOptions;
    }

    public String getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    public void setTotalTransactionAmount(String totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaytmUserId() {
        return paytmUserId;
    }

    public void setPaytmUserId(String paytmUserId) {
        this.paytmUserId = paytmUserId;
    }

    public boolean isPromoForPCFMerchant() {
        return promoForPCFMerchant;
    }

    public void setPromoForPCFMerchant(boolean promoForPCFMerchant) {
        this.promoForPCFMerchant = promoForPCFMerchant;
    }

    public Map<String, String> getPromoContext() {
        return promoContext;
    }

    public void setPromoContext(Map<String, String> promoContext) {
        this.promoContext = promoContext;
    }

    public List<PromoDetails> getPromoInfo() {
        return promoInfo;
    }

    public void setPromoInfo(List<PromoDetails> promoInfo) {
        this.promoInfo = promoInfo;
    }

    public String getEncUserId() {
        return encUserId;
    }

    public void setEncUserId(String encUserId) {
        this.encUserId = encUserId;
    }

    @Override
    public String toString() {
        return MaskToStringBuilder.toString(this);
    }

    public Boolean getChangeRedemptionTypetoCashBack() {
        return changeRedemptionTypetoCashBack;
    }

    public void setChangeRedemptionTypetoCashBack(Boolean changeRedemptionTypetoCashBack) {
        this.changeRedemptionTypetoCashBack = changeRedemptionTypetoCashBack;
    }

    public PromoCartDetails getCartDetails() {
        return cartDetails;
    }

    public void setCartDetails(PromoCartDetails cartDetails) {
        this.cartDetails = cartDetails;
    }

}
