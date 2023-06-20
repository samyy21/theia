package com.paytm.pgplus.theia.nativ.model.bin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.nativ.model.payview.response.EmiChannel;
import com.paytm.pgplus.theia.nativ.model.preauth.PreAuthDetails;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.payview.StatusInfo;
import com.paytm.pgplus.theia.offline.model.response.BinDetailResponseBody;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NativeBinDetailResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -629833383612819650L;

    @JsonProperty("binDetail")
    private BinData binDetail;

    @JsonProperty("authModes")
    private List<String> authModes;

    @JsonProperty("hasLowSuccessRate")
    private StatusInfo hasLowSuccessRate;

    @JsonProperty("iconUrl")
    private String iconUrl;

    @JsonProperty("emiDetail")
    @JsonIgnore
    private EmiChannel emiChannel;

    private boolean isEmiAvailable = false;

    @JsonProperty("errorMessage")
    @LocaleField
    private String errorMessage;

    @JsonProperty("isHybridDisabled")
    private boolean hybridDisabled;

    @JsonProperty("pcf")
    private PCFFeeCharges pcfFeeCharges;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> supportedCardSubTypes;

    @JsonProperty("remainingLimit")
    private String remainingLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PreAuthDetails> preAuthDetails;

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

    public PCFFeeCharges getPcfFeeCharges() {
        return pcfFeeCharges;
    }

    public void setPcfFeeCharges(PCFFeeCharges pcfFeeCharges) {
        this.pcfFeeCharges = pcfFeeCharges;
    }

    public Boolean getIsSubscriptionAvailable() {
        return isSubscriptionAvailable;
    }

    public void setIsSubscriptionAvailable(Boolean subscriptionAvailable) {
        isSubscriptionAvailable = subscriptionAvailable;
    }

    public NativeBinDetailResponseBody() {
    }

    public NativeBinDetailResponseBody(BinDetailResponseBody binDetailResponseBody) {
        this.binDetail = binDetailResponseBody.getBinDetail();
        this.hasLowSuccessRate = binDetailResponseBody.getHasLowSuccessRate();
        this.iconUrl = binDetailResponseBody.getIconUrl();
        this.errorMessage = binDetailResponseBody.getErrorMsg();
        this.isSubscriptionAvailable = binDetailResponseBody.getIsSubscriptionAvailable();
        this.oneClickSupported = binDetailResponseBody.isOneClickSupported();
        this.oneClickMaxAmount = binDetailResponseBody.getOneClickMaxAmount();
    }

    public BinData getBinDetail() {
        return binDetail;
    }

    public void setBinDetail(BinData binDetail) {
        this.binDetail = binDetail;
    }

    public List<String> getAuthModes() {
        return authModes;
    }

    public void setAuthModes(List<String> authModes) {
        this.authModes = authModes;
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

    public EmiChannel getEmiChannel() {
        return emiChannel;
    }

    public void setEmiChannel(EmiChannel emiChannel) {
        this.emiChannel = emiChannel;
    }

    public boolean getIsEmiAvailable() {
        return isEmiAvailable;
    }

    public void setIsEmiAvailable(boolean isEmiAvailable) {
        this.isEmiAvailable = isEmiAvailable;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isHybridDisabled() {
        return hybridDisabled;
    }

    public void setHybridDisabled(boolean hybridDisabled) {
        this.hybridDisabled = hybridDisabled;
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
        final StringBuilder sb = new StringBuilder("NativeBinDetailResponseBody{");
        sb.append("binDetail=").append(binDetail);
        sb.append(", authModes=").append(authModes);
        sb.append(", hasLowSuccessRate=").append(hasLowSuccessRate);
        sb.append(", iconUrl='").append(iconUrl).append('\'');
        sb.append(", emiChannel=").append(emiChannel);
        sb.append(", isEmiAvailable=").append(isEmiAvailable);
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append(", hybridDisabled='").append(hybridDisabled).append('\'');
        sb.append(", pcfFeeCharges='").append(pcfFeeCharges).append('\'');
        sb.append(", oneClickSupported='").append(oneClickSupported).append('\'');
        sb.append(", oneClickMaxAmount='").append(oneClickMaxAmount).append('\'');
        sb.append(", prepaidCard='").append(prepaidCard).append('\'');
        sb.append(", prepaidCardMaxAmount='").append(prepaidCardMaxAmount).append('\'');
        sb.append(", supportedCardSubTypes='").append(supportedCardSubTypes).append('\'');
        sb.append(", preAuthDetails='").append(preAuthDetails).append('\'');
        sb.append(", remainingLimit='").append(remainingLimit).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
