package com.paytm.pgplus.theia.nativ.model.validateCard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.enums.CardType;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.theia.nativ.model.PCF.PCFFeeCharges;
import com.paytm.pgplus.theia.offline.model.common.BinData;
import com.paytm.pgplus.theia.offline.model.payview.StatusInfo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateCardResponseBody extends BaseResponseBody {

    private String cardType;
    private String cardName;
    private String cardScheme;
    private String bankName;
    private String performanceStatus;
    private String InstId;
    @JsonProperty("binDetail")
    private BinData binDetail;

    @JsonProperty("authModes")
    private List<String> authModes;

    @JsonProperty("hasLowSuccessRate")
    private StatusInfo hasLowSuccessRate;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("iconUrl")
    private String iconUrl;

    public String getInstId() {
        return InstId;
    }

    public void setInstId(String instId) {
        InstId = instId;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public StatusInfo isHasLowSuccessRate() {
        return hasLowSuccessRate;
    }

    public void setHasLowSuccessRate(StatusInfo hasLowSuccessRate) {
        this.hasLowSuccessRate = hasLowSuccessRate;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getPerformanceStatus() {
        return performanceStatus;
    }

    public void setPerformanceStatus(String performanceStatus) {
        this.performanceStatus = performanceStatus;
    }

}
