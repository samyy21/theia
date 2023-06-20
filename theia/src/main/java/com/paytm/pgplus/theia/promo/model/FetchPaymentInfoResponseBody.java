package com.paytm.pgplus.theia.promo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.cache.model.BankDetails;
import com.paytm.pgplus.cache.model.CardNetworkDetails;
import com.paytm.pgplus.cache.model.PayMethodDetails;
import com.paytm.pgplus.common.model.ResultInfo;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

public class FetchPaymentInfoResponseBody implements Serializable {

    @JsonProperty("paymethodInfo")
    private List<PayMethodDetails> payMethodDetails;

    @JsonProperty("cardNetworkInfo")
    private List<CardNetworkDetails> cardNetworkDetails;

    @JsonProperty("bankDetails")
    private List<BankDetails> bankDetails;

    @JsonProperty("resultInfo")
    private ResultInfo resultInfo;

    public FetchPaymentInfoResponseBody() {
    }

    public List<PayMethodDetails> getPayMethodDetails() {
        return payMethodDetails;
    }

    public void setPayMethodDetails(List<PayMethodDetails> payMethodDetails) {
        this.payMethodDetails = payMethodDetails;
    }

    public List<CardNetworkDetails> getCardNetworkDetails() {
        return cardNetworkDetails;
    }

    public void setCardNetworkDetails(List<CardNetworkDetails> cardNetworkDetails) {
        this.cardNetworkDetails = cardNetworkDetails;
    }

    public List<BankDetails> getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(List<BankDetails> bankDetails) {
        this.bankDetails = bankDetails;
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
