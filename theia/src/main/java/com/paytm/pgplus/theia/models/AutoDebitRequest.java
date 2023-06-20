package com.paytm.pgplus.theia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.payloadvault.theia.enums.UserSubWalletType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoDebitRequest implements Serializable {
    private static final long serialVersionUID = -3271229420660696160L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDebitRequest.class);

    private static final TypeReference<HashMap<UserSubWalletType, BigDecimal>> SUBWALLET_TYPE_REF = new TypeReference<HashMap<UserSubWalletType, BigDecimal>>() {
    };

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty("ReqType")
    private String reqType;
    @JsonProperty("MId")
    private String mid;
    @JsonProperty("OrderId")
    private String orderId;
    @JsonProperty("TxnAmount")
    private String txnAmount;
    @JsonProperty("CustomerId")
    private String customerId;
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("DeviceId")
    private String deviceId;
    @JsonProperty("SSOToken")
    @Mask(prefixNoMaskLen = 6, maskStr = "*", suffixNoMaskLen = 4)
    private String ssoToken;
    @JsonProperty("PaymentMode")
    private String paymentMode;
    @JsonProperty("IndustryType")
    private String industryType;
    @JsonProperty("CheckSum")
    private String checkSum;
    @JsonProperty("AppIP")
    private String appIP;
    @JsonProperty("AuthMode")
    private String authMode;
    @JsonProperty("Channel")
    private String channel;
    @JsonProperty("TokenType")
    private String tokenType;
    private String refund;
    @JsonProperty("ClientId")
    private String clientId;
    @JsonProperty("subwalletAmount")
    private Map<UserSubWalletType, BigDecimal> subwalletAmount;
    @JsonProperty("ExchangeRate")
    private String exchangeRate;
    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;
    @JsonProperty("loyaltyPointRootUserId")
    private String loyaltyPointRootUserId;
    @JsonProperty("rootUserPGMid")
    private String loyaltyPointRootUserPGMid;

    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
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

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getAppIP() {
        return appIP;
    }

    public void setAppIP(String appIP) {
        this.appIP = appIP;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefund() {
        return refund;
    }

    public void setRefund(String refund) {
        this.refund = refund;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<UserSubWalletType, BigDecimal> getSubwalletAmount() {
        return subwalletAmount;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setSubwalletAmount(String subwalletAmount) {

        try {

            if (StringUtils.isNotBlank(subwalletAmount))
                this.subwalletAmount = OBJECT_MAPPER.readValue(subwalletAmount, SUBWALLET_TYPE_REF);

        } catch (IOException exception) {

            LOGGER.error("Exception occurred while Mapping Subwallet Map :: {}", exception);
        }
    }

    public String getLoyaltyPointRootUserId() {
        return loyaltyPointRootUserId;
    }

    public void setLoyaltyPointRootUserId(String loyaltyPointRootUserId) {
        this.loyaltyPointRootUserId = loyaltyPointRootUserId;
    }

    public String getLoyaltyPointRootUserPGMid() {
        return loyaltyPointRootUserPGMid;
    }

    public void setLoyaltyPointRootUserPGMid(String loyaltyPointRootUserPGMid) {
        this.loyaltyPointRootUserPGMid = loyaltyPointRootUserPGMid;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
