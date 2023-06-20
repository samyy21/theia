package com.paytm.pgplus.theia.nativ.model.kyc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeKYCDetailRequestBody implements Serializable {

    private static final long serialVersionUID = 806098674966271572L;

    @NotBlank(message = "kycNameOnDoc cannot be blank")
    @JsonProperty("kycNameOnDoc")
    private String kycNameOnDoc;

    @NotBlank(message = "kycDocCode cannot be blank")
    @JsonProperty("kycDocCode")
    private String kycDocCode;

    @NotBlank(message = "kycDocValue cannot be blank")
    @JsonProperty("kycDocValue")
    private String kycDocValue;

    public NativeKYCDetailRequestBody() {
    }

    public NativeKYCDetailRequestBody(String kycNameOnDoc, String kycDocCode, String kycDocValue) {
        this.kycNameOnDoc = kycNameOnDoc;
        this.kycDocCode = kycDocCode;
        this.kycDocValue = kycDocValue;
    }

    public String getKycNameOnDoc() {
        return kycNameOnDoc;
    }

    public void setKycNameOnDoc(String kycNameOnDoc) {
        this.kycNameOnDoc = kycNameOnDoc;
    }

    public String getKycDocCode() {
        return kycDocCode;
    }

    public void setKycDocCode(String kycDocCode) {
        this.kycDocCode = kycDocCode;
    }

    public String getKycDocValue() {
        return kycDocValue;
    }

    public void setKycDocValue(String kycDocValue) {
        this.kycDocValue = kycDocValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeKYCDetailRequestBody{");
        sb.append("kycDocCode='").append(kycDocCode).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
