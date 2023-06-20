package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;
import java.util.List;

public class EmiDetails implements Serializable {

    private static final long serialVersionUID = 1365234658221374136L;

    private BankOffer bankOffer;
    private List<EmiChannelInfos> emiChannelInfos;
    private String templateBaseUrl;
    private String payMethod;

    @Override
    public String toString() {
        return "EmiDetails{" + "bankOffer=" + bankOffer + ", emiChannelInfos=" + emiChannelInfos
                + ", templateBaseUrl='" + templateBaseUrl + '\'' + ", payMethod='" + payMethod + '\'' + '}';
    }

    public String getTemplateBaseUrl() {
        return templateBaseUrl;
    }

    public void setTemplateBaseUrl(String templateBaseUrl) {
        this.templateBaseUrl = templateBaseUrl;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public BankOffer getBankOffer() {
        return bankOffer;
    }

    public void setBankOffer(BankOffer bankOffer) {
        this.bankOffer = bankOffer;
    }

    public List<EmiChannelInfos> getEmiChannelInfos() {
        return emiChannelInfos;
    }

    public void setEmiChannelInfos(List<EmiChannelInfos> emiChannelInfos) {
        this.emiChannelInfos = emiChannelInfos;
    }
}
