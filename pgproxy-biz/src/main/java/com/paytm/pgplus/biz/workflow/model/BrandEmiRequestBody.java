package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

/**
 * @author harshwardhan
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrandEmiRequestBody implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -7937449700413835399L;

    private String amount;
    private String bin;
    private String date;
    private String encryptedPan;
    private String mid;
    private String tid;
    private String time;
    private String year;
    private String brandId;
    private String categoryId;
    private String productId;
    private String model;
    private String ean;
    private String quantity;
    private String verticalId;
    private String isEmiEnabled;
    @Mask(prefixNoMaskLen = 2, suffixNoMaskLen = 2)
    private String cardIndexNumber;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBin() {
        return bin;
    }

    public String getCardIndexNumber() {
        return cardIndexNumber;
    }

    public void setCardIndexNumber(String cardIndexNumber) {
        this.cardIndexNumber = cardIndexNumber;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEncryptedPan() {
        return encryptedPan;
    }

    public void setEncryptedPan(String encryptedPan) {
        this.encryptedPan = encryptedPan;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
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

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
