/**
 * 
 */
package com.paytm.pgplus.theia.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author namanjain
 *
 */
public class InvoiveBORequestBean {

    @JsonProperty("REQUEST_TYPE")
    String requestType;

    @JsonProperty("MID")
    String mID;

    @JsonProperty("ORDER_ID")
    String orderID;

    @JsonProperty("CUST_ID")
    String custID;

    @JsonProperty("TXN_AMOUNT")
    String txnAmount;

    @JsonProperty("CHANNEL_ID")
    String channelID;

    @JsonProperty("INDUSTRY_TYPE_ID")
    String industryType;

    @JsonProperty("WEBSITE")
    String website;

    @JsonProperty("CHECKSUMHASH")
    String checksumHash;

    @JsonProperty("EMAIL")
    String email;

    @JsonProperty("MOBILE_NO")
    String mobileNo;

    @JsonProperty("MERCH_UNQ_REF")
    String merchantUniqueRef;

    @JsonProperty("CALLBACK_URL")
    String callBackURL;

    @JsonProperty("PEON_URL")
    String peonURL;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getmID() {
        return mID;
    }

    public void setmID(String mID) {
        this.mID = mID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(String custID) {
        this.custID = custID;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getChecksumHash() {
        return checksumHash;
    }

    public void setChecksumHash(String checksumHash) {
        this.checksumHash = checksumHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getMerchantUniqueRef() {
        return merchantUniqueRef;
    }

    public void setMerchantUniqueRef(String merchantUniqueRef) {
        this.merchantUniqueRef = merchantUniqueRef;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getPeonURL() {
        return peonURL;
    }

    public void setPeonURL(String peonURL) {
        this.peonURL = peonURL;
    }
}
