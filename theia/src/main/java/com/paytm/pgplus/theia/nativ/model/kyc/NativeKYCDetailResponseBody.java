package com.paytm.pgplus.theia.nativ.model.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeKYCDetailResponseBody extends BaseResponseBody {

    @JsonProperty("kycSuccessful")
    private boolean kycSuccessful;

    @JsonProperty("kycErrorMsg")
    private String kycErrorMsg;

    @JsonProperty("kycRetryCount")
    private int kycRetryCount;

    public NativeKYCDetailResponseBody() {
    }

    public NativeKYCDetailResponseBody(NativeKYCDetailResponseBody nativeKYCDetailResponseBody) {
        this.kycSuccessful = nativeKYCDetailResponseBody.isKycSuccessful();
    }

    public NativeKYCDetailResponseBody(boolean kycSuccessful, String kycErrorMsg, int kycRetryCount,
            ResultInfo resultInfo) {
        this.kycSuccessful = kycSuccessful;
        this.kycErrorMsg = kycErrorMsg;
        this.kycRetryCount = kycRetryCount;
        this.setResultInfo(resultInfo);
    }

    public boolean isKycSuccessful() {
        return kycSuccessful;
    }

    public void setKycSuccessful(boolean kycSuccessful) {
        this.kycSuccessful = kycSuccessful;
    }

    public String getKycErrorMsg() {
        return kycErrorMsg;
    }

    public void setKycErrorMsg(String kycErrorMsg) {
        this.kycErrorMsg = kycErrorMsg;
    }

    public int getKycRetryCount() {
        return kycRetryCount;
    }

    public void setKycRetryCount(int kycRetryCount) {
        this.kycRetryCount = kycRetryCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeKYCDetailResponseBody{");
        sb.append(", kycSuccessful=").append(kycSuccessful);
        sb.append(", kycErrorMsg=").append(kycErrorMsg);
        sb.append('}');
        return sb.toString();
    }
}
