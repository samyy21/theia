package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantGiftVoucher extends BalanceChannel {
    private static final long serialVersionUID = -3393784555189654167L;

    private String templateId;

    public MerchantGiftVoucher(AccountInfo balanceInfo) {
        super(balanceInfo);
    }

    public MerchantGiftVoucher() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MerchantGiftVoucher{");
        sb.append("templateId='").append(templateId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
