package com.paytm.pgplus.theia.s2s.models.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.facade.payment.models.EMIChannelInfo;
import com.paytm.pgplus.models.PaymentOffer;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentS2SRequestBody implements Serializable {

    private static final long serialVersionUID = -1385359211834910767L;

    @NotEmpty(message = "request type cannot be blank")
    private String requestType;

    @NotEmpty(message = "mid cannot be blank")
    private String mid;

    @NotEmpty(message = "order Id cannot be blank")
    private String orderId;

    @NotEmpty(message = "cust id cannot be blank")
    private String custId;

    @NotEmpty(message = "txn amount cannot be blank")
    private String txnAmount;

    @JsonProperty("industryType")
    @NotEmpty(message = "industry type id cannot be blank")
    private String industryTypeId;

    @NotEmpty(message = "website cannot be blank")
    private String website;

    @JsonProperty("paymentMode")
    @NotEmpty(message = "payment mode cannot be blank")
    private String paymentTypeId;

    private String bankCode;

    private String paymentDetails;

    private String mobileNo;

    private String email;

    private EMIChannelInfo emiChannelInfo;

    @JsonProperty(UDF_1)
    private String udf1;

    @JsonProperty(UDF_2)
    private String udf2;

    @JsonProperty(UDF_3)
    private String udf3;

    private String additionalInfo;

    private String callBackURL;

    private String refUrl;
    private String txnNote;
    private String osType;
    private String pspApp;
    private String accountNumber;
    private String fromAOARequest;

    private String cardInfo;
    private CardTokenInfo cardTokenInfo;

    private PaymentOffer paymentOffersApplied;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public String getIndustryTypeId() {
        return industryTypeId;
    }

    public void setIndustryTypeId(String industryTypeId) {
        this.industryTypeId = industryTypeId;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EMIChannelInfo getEmiChannelInfo() {
        return emiChannelInfo;
    }

    public void setEmiChannelInfo(EMIChannelInfo emiChannelInfo) {
        this.emiChannelInfo = emiChannelInfo;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getRefUrl() {
        return refUrl;
    }

    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    public String getTxnNote() {
        return txnNote;
    }

    public void setTxnNote(String txnNote) {
        this.txnNote = txnNote;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getPspApp() {
        return pspApp;
    }

    public void setPspApp(String pspApp) {
        this.pspApp = pspApp;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFromAOARequest() {
        return fromAOARequest;
    }

    public void setFromAOARequest(String fromAOARequest) {
        this.fromAOARequest = fromAOARequest;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PaymentS2SRequestBody{");
        sb.append("additionalInfo='").append(additionalInfo).append('\'');
        sb.append(", bankCode='").append(bankCode).append('\'');
        sb.append(", callBackURL='").append(callBackURL).append('\'');
        sb.append(", custId='").append(custId).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", industryTypeId='").append(industryTypeId).append('\'');
        sb.append(", mid='").append(mid).append('\'');
        sb.append(", emiChannelInfo='").append(emiChannelInfo).append('\'');
        sb.append(", mobileNo='").append(mobileNo).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", paymentDetails='").append("********").append('\'');
        sb.append(", paymentTypeId='").append(paymentTypeId).append('\'');
        sb.append(", requestType='").append(requestType).append('\'');
        sb.append(", txnAmount='").append(txnAmount).append('\'');
        sb.append(", udf1='").append(udf1).append('\'');
        sb.append(", udf2='").append(udf2).append('\'');
        sb.append(", udf3='").append(udf3).append('\'');
        sb.append(", website='").append(website).append('\'');
        sb.append(", fromAOARequest='").append(fromAOARequest).append('\'');
        sb.append(", paymentOffersApplied='").append(paymentOffersApplied).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public void setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
    }

    public String getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(String cardInfo) {
        this.cardInfo = cardInfo;
    }

    public PaymentOffer getPaymentOffersApplied() {
        return paymentOffersApplied;
    }

    public void setPaymentOffersApplied(PaymentOffer paymentOffersApplied) {
        this.paymentOffersApplied = paymentOffersApplied;
    }
}
