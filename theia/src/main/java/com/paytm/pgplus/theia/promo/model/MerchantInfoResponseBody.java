package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.theia.offline.model.base.BaseRequest;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantInfoResponseBody extends BaseRequest implements Serializable {

    @JsonProperty("merchantBaseInfoList")
    private List<MerchantBaseInfo> merchantBaseInfoList;

    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;

    public MerchantInfoResponseBody() {
    }

    public List<MerchantBaseInfo> getMerchantBaseInfoList() {
        return merchantBaseInfoList;
    }

    public void setMerchantBaseInfoList(List<MerchantBaseInfo> merchantBaseInfoList) {
        this.merchantBaseInfoList = merchantBaseInfoList;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
