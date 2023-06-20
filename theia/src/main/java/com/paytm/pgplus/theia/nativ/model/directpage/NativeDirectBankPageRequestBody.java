package com.paytm.pgplus.theia.nativ.model.directpage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.BooleanUtils;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeDirectBankPageRequestBody implements Serializable {

    private static final long serialVersionUID = 3046213366569287915L;

    private String otp;
    private String requestType;
    private String apiRequestOrigin;
    private String isForceResendOtp;
    private String acquirementId;
    private String maxOtpRetryCount;
    private String maxOtpResendCount;

    public NativeDirectBankPageRequestBody() {
    }

    public String getOtp() {
        return otp;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getApiRequestOrigin() {
        return apiRequestOrigin;
    }

    public void setApiRequestOrigin(String apiRequestSource) {
        this.apiRequestOrigin = apiRequestSource;
    }

    public boolean isForceResendOtp() {
        return BooleanUtils.toBoolean(this.isForceResendOtp);
    }

    public void setIsForceResendOtp(String isForceResendOtp) {
        this.isForceResendOtp = isForceResendOtp;
    }

    public String getAcquirementId() {
        return acquirementId;
    }

    public void setAcquirementId(String acqId) {
        this.acquirementId = acqId;
    }

    public String getMaxOtpRetryCount() {
        return maxOtpRetryCount;
    }

    public void setMaxOtpRetryCount(String maxOtpRetryCount) {
        this.maxOtpRetryCount = maxOtpRetryCount;
    }

    public String getMaxOtpResendCount() {
        return maxOtpResendCount;
    }

    public void setMaxOtpResendCount(String maxOtpResendCount) {
        this.maxOtpResendCount = maxOtpResendCount;
    }

    @Override
    public String toString() {
        return "NativeDirectBankPageRequestBody{" + "otp='" + otp + '\'' + ", requestType='" + requestType + '\''
                + ", apiRequestOrigin='" + apiRequestOrigin + '\'' + ", isForceResendOtp='" + isForceResendOtp + '\''
                + ", acquirementId='" + acquirementId + '\'' + ", maxOtpRetryCount='" + maxOtpRetryCount + '\''
                + ", maxOtpResendCount='" + maxOtpResendCount + '\'' + '}';
    }
}
