package com.paytm.pgplus.theia.models.upiAccount.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.customaspects.mapper.MaskToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchUpiOptionsRequestBody {
    @NotBlank
    private String mid;

    public FetchUpiOptionsRequestBody() {
    }

    public FetchUpiOptionsRequestBody(String mid) {
        this.mid = mid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
