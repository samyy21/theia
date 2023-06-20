package com.paytm.pgplus.theia.nativ.model.payview.response;

import java.io.Serializable;
import java.util.List;

public class BrandEmiDetail implements Serializable {

    private static final long serialVersionUID = 726718433080669262L;

    private String brandCode;
    private List<MerchantEmiDetail> brandEmiDetailInfo;

    public String getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public List<MerchantEmiDetail> getBrandEmiDetailInfo() {
        return brandEmiDetailInfo;
    }

    public void setBrandEmiDetailInfo(List<MerchantEmiDetail> brandEmiDetailInfo) {
        this.brandEmiDetailInfo = brandEmiDetailInfo;
    }

    @Override
    public String toString() {
        return "BrandEmiDetail{" + "brandCode='" + brandCode + '\'' + ", brandEmiDetailInfo=" + brandEmiDetailInfo
                + '}';
    }
}
