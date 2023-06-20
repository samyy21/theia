package com.paytm.pgplus.theia.nativ.model.payview.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.models.TwoFADetails;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchQRPaymentDetailsRequestBody implements Serializable {

    private final static long serialVersionUID = -8125572972603083508L;

    @JsonProperty("qrCodeId")
    private String qrCodeId;

    @JsonProperty("orderId")
    private String orderId;

    private boolean mlvSupported;

    private String appVersion;

    private Map<String, String> queryParams;

    @JsonProperty("isLiteEligible")
    private boolean isLiteEligible;

    public boolean getIsLiteEligible() {
        return isLiteEligible;
    }

    public void setIsLiteEligible(boolean liteEligible) {
        isLiteEligible = liteEligible;
    }

    private boolean fetchPaytmInstrumentsBalance;

    private String supportedPayModesForAddNPay;

    @JsonProperty("twoFADetails")
    private TwoFADetails twoFADetails;

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isMlvSupported() {
        return mlvSupported;
    }

    public void setMlvSupported(boolean mlvSupported) {
        this.mlvSupported = mlvSupported;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isFetchPaytmInstrumentsBalance() {
        return fetchPaytmInstrumentsBalance;
    }

    public void setFetchPaytmInstrumentsBalance(boolean fetchPaytmInstrumentsBalance) {
        this.fetchPaytmInstrumentsBalance = fetchPaytmInstrumentsBalance;
    }

    public String getSupportedPayModesForAddNPay() {
        return supportedPayModesForAddNPay;
    }

    public void setSupportedPayModesForAddNPay(String supportedPayModesForAddNPay) {
        this.supportedPayModesForAddNPay = supportedPayModesForAddNPay;
    }

    public TwoFADetails getTwoFADetails() {
        return twoFADetails;
    }

    public void setTwoFADetails(TwoFADetails twoFADetails) {
        this.twoFADetails = twoFADetails;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
