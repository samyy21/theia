package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.response.BaseResponseBody;
import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantInfoResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -3551361706770883483L;

    @JsonProperty("merchantInfoResp")
    private MerchantInfoResp merchantInfoResp;

    @JsonProperty("txnAmount")
    private Money txnAmount;

    @JsonProperty("promoCodeApplied")
    private String promoCodeApplied;

    private boolean appInvokeAllowed;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String callbackUrl;

    public MerchantInfoResponseBody() {
        super();
    }

    public void setMerchantInfoResp(MerchantInfoResp merchantInfoResp) {
        this.merchantInfoResp = merchantInfoResp;
    }

    public void setTxnAmount(Money txnAmount) {
        this.txnAmount = txnAmount;
    }

    public void setPromoCodeApplied(String promoCodeApplied) {
        this.promoCodeApplied = promoCodeApplied;
    }

    public boolean isAppInvokeAllowed() {
        return appInvokeAllowed;
    }

    public void setAppInvokeAllowed(boolean appInvokeAllowed) {
        this.appInvokeAllowed = appInvokeAllowed;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MerchantInfoResponseBody{");
        sb.append("merchantInfoResp=").append(merchantInfoResp);
        sb.append(", txnAmount=").append(txnAmount);
        sb.append(", promoCodeApplied='").append(promoCodeApplied).append('\'');
        sb.append(", appInvokeAllowed=").append(appInvokeAllowed);
        sb.append(", callbackUrl='").append(callbackUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
