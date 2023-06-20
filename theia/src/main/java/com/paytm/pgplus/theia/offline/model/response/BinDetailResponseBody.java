/*
 * {user}
 * {date}
 */

package com.paytm.pgplus.theia.offline.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.payview.StatusInfo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinDetailResponseBody extends ResponseBody {

    private static final long serialVersionUID = -843819383612819650L;

    @JsonProperty("binDetail")
    private BinData binDetail;

    @JsonProperty("hasLowSuccessRate")
    private StatusInfo hasLowSuccessRate;

    @JsonProperty("iconUrl")
    private String iconUrl;

    @JsonProperty("authModes")
    private List<String> authModes;

    @JsonProperty("errorMsg")
    private String errorMsg;

    @JsonProperty("pcf")
    private PCFFeeCharges pcfFeeCharges;

    public PCFFeeCharges getPcfFeeCharges() {
        return pcfFeeCharges;
    }

    public void setPcfFeeCharges(PCFFeeCharges pcfFeeCharges) {
        this.pcfFeeCharges = pcfFeeCharges;
    }

    @JsonProperty("isSubscriptionAvailable")
    private Boolean isSubscriptionAvailable;

    @JsonProperty("oneClickSupported")
    private boolean oneClickSupported;

    @JsonProperty("oneClickMaxAmount")
    private String oneClickMaxAmount;

    @JsonProperty("prepaidCard")
    private Boolean prepaidCard;

    @JsonProperty("prepaidCardMaxAmount")
    private String prepaidCardMaxAmount;

    @JsonProperty("remainingLimit")
    private String remainingLimit;

    @JsonIgnore
    private boolean isZeroSuccessRate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> supportedCardSubTypes;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean isCorporateCardFail;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PreAuthDetails> preAuthDetails;

    public boolean isZeroSuccessRate() {
        return isZeroSuccessRate;
    }

    public void setZeroSuccessRate(boolean zeroSuccessRate) {
        isZeroSuccessRate = zeroSuccessRate;
    }

    public Boolean getIsSubscriptionAvailable() {
        return isSubscriptionAvailable;
    }

    public void setIsSubscriptionAvailable(Boolean isSubscriptionAvailable) {
        this.isSubscriptionAvailable = isSubscriptionAvailable;
    }

    public BinDetailResponseBody() {

    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public BinDetailResponseBody(BinData binDetail) {
        this.binDetail = binDetail;
    }

    public BinData getBinDetail() {
        return binDetail;
    }

    public void setBinDetail(BinData binDetail) {
        this.binDetail = binDetail;
    }

    public StatusInfo getHasLowSuccessRate() {
        return hasLowSuccessRate;
    }

    public void setHasLowSuccessRate(StatusInfo hasLowSuccessRate) {
        this.hasLowSuccessRate = hasLowSuccessRate;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<String> getAuthModes() {
        return authModes;
    }

    public void setAuthModes(List<String> authModes) {
        this.authModes = authModes;
    }

    public boolean isOneClickSupported() {
        return oneClickSupported;
    }

    public void setOneClickSupported(boolean oneClickSupported) {
        this.oneClickSupported = oneClickSupported;
    }

    public String getOneClickMaxAmount() {
        return oneClickMaxAmount;
    }

    public void setOneClickMaxAmount(String oneClickMaxAmount) {
        this.oneClickMaxAmount = oneClickMaxAmount;
    }

    public Boolean getPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(Boolean prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public String getPrepaidCardMaxAmount() {
        return prepaidCardMaxAmount;
    }

    public void setPrepaidCardMaxAmount(String prepaidCardMaxAmount) {
        this.prepaidCardMaxAmount = prepaidCardMaxAmount;
    }

    public List<String> getSupportedCardSubTypes() {
        return supportedCardSubTypes;
    }

    public void setSupportedCardSubTypes(List<String> supportedCardSubTypes) {
        this.supportedCardSubTypes = supportedCardSubTypes;
    }

    public boolean isCorporateCardFail() {
        return isCorporateCardFail;
    }

    public void setCorporateCardFail(boolean corporateCardFail) {
        isCorporateCardFail = corporateCardFail;
    }

    public List<PreAuthDetails> getPreAuthDetails() {
        return preAuthDetails;
    }

    public void setPreAuthDetails(List<PreAuthDetails> preAuthDetails) {
        this.preAuthDetails = preAuthDetails;
    }

    public String getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(String remainingLimit) {
        this.remainingLimit = remainingLimit;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BinDetailResponseBody [binDetail=");
        builder.append(binDetail);
        builder.append(", hasLowSuccessRate=");
        builder.append(hasLowSuccessRate);
        builder.append(", iconUrl=");
        builder.append(iconUrl);
        builder.append(", authModes=");
        builder.append(authModes);
        builder.append(", pcfFeeCharges=");
        builder.append(pcfFeeCharges);
        builder.append(", oneClickSupported=");
        builder.append(oneClickSupported);
        builder.append(", oneClickMaxAmount=");
        builder.append(oneClickMaxAmount);
        builder.append(", remainingLimit=");
        builder.append(remainingLimit);
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
