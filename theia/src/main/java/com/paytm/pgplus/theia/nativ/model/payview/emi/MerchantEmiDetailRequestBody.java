package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.response.ResultInfo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantEmiDetailRequestBody implements Serializable {

    private static final long serialVersionUID = -7012803773321784022L;

    @JsonProperty("mid")
    private String mid;

    private String productCode;

    @NotNull
    private ResultInfo resultInfo;

    private List<String> brandCode;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public List<String> getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(List<String> brandCode) {
        this.brandCode = brandCode;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
