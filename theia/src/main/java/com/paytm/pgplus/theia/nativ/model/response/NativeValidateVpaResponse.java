package com.paytm.pgplus.theia.nativ.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.response.BaseResponseBody;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NativeValidateVpaResponse extends BaseResponseBody {

    private static final long serialVersionUID = -6039833732179128574L;
    private String vpa;
    private boolean isValid;
    private RecurringDetails recurringDetails;
    private FeatureDetails featureDetails;
    @JsonIgnore
    private String custId;
    private String cmId;

    public NativeValidateVpaResponse(String vpa) {
        this.vpa = vpa;
    }

    public NativeValidateVpaResponse() {

    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public RecurringDetails getRecurringDetails() {
        return recurringDetails;
    }

    public void setRecurringDetails(RecurringDetails recurringDetails) {
        this.recurringDetails = recurringDetails;
    }

    public FeatureDetails getFeatureDetails() {
        return featureDetails;
    }

    public void setFeatureDetails(FeatureDetails featureDetails) {
        this.featureDetails = featureDetails;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getCmId() {
        return cmId;
    }

    public void setCmId(String cmId) {
        this.cmId = cmId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeValidateVpaResponse{");
        sb.append("vpa='").append(vpa).append('\'');
        sb.append(", isValid=").append(isValid);
        sb.append(", recurringDetails=").append(recurringDetails);
        sb.append(", featureDetails=").append(featureDetails);
        sb.append(", cmId=").append(cmId);
        sb.append('}');
        return sb.toString();
    }
}
