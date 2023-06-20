package com.paytm.pgplus.theia.nativ.model.payview.emi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.model.payview.response.BrandEmiDetail;
import com.paytm.pgplus.theia.nativ.model.payview.response.MerchantEmiDetail;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantEmiDetailResponseBody implements Serializable {

    private static final long serialVersionUID = -8997069141925469857L;

    private List<MerchantEmiDetail> emiDetails;

    private ResultInfo resultInfo;

    private List<BrandEmiDetail> brandEmiDetails;

    public List<BrandEmiDetail> getBrandEmiDetails() {
        return brandEmiDetails;
    }

    public void setBrandEmiDetails(List<BrandEmiDetail> brandEmiDetails) {
        this.brandEmiDetails = brandEmiDetails;
    }

    public List<MerchantEmiDetail> getEmiDetails() {
        return emiDetails;
    }

    public void setEmiDetails(List<MerchantEmiDetail> emiDetails) {
        this.emiDetails = emiDetails;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
