package com.paytm.pgplus.theia.nativ.model.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.models.GoodsInfo;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.models.ShippingInfo;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTransactionDetailRequestBody implements Serializable {

    private static final long serialVersionUID = -5805652659651506957L;

    @JsonProperty("txnAmount")
    private Money txnAmount;

    @JsonProperty("goods")
    private List<GoodsInfo> goods = null;

    @JsonProperty("shippingInfo")
    private List<ShippingInfo> shippingInfo = null;

    @JsonProperty("extendInfo")
    private ExtendInfo extendInfo;

    public Money getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(Money txnAmount) {
        this.txnAmount = txnAmount;
    }

    public List<GoodsInfo> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsInfo> goods) {
        this.goods = goods;
    }

    public List<ShippingInfo> getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(List<ShippingInfo> shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateTransactionDetailRequestBody{");
        sb.append(", txnAmount=").append(txnAmount);
        sb.append(", goods=").append(goods);
        sb.append(", shippingInfo=").append(shippingInfo);
        sb.append(", extendInfo=").append(extendInfo);
        sb.append('}');
        return sb.toString();
    }
}
