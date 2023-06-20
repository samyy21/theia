package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.response.BaseResponseBody;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by charu on 07/10/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EMIEligibilityResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 3090103548616638695L;

    @JsonProperty("eligible")
    @NotNull
    private String eligible;

    @NotNull
    @JsonProperty("message")
    private String message;

    // emiSubvention
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EmiEligibility> emiEligibility;

    public String getEligible() {
        return eligible;
    }

    public void setEligible(String eligible) {
        this.eligible = eligible;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<EmiEligibility> getEmiEligibility() {
        return emiEligibility;
    }

    public void setEmiEligibility(List<EmiEligibility> emiEligibility) {
        this.emiEligibility = emiEligibility;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
