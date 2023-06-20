package com.paytm.pgplus.theia.nativ.model.balanceinfo;

import com.paytm.pgplus.models.Money;

import java.io.Serializable;

public class DetailedBalanceInfo implements Serializable {
    private static final long serialVersionUID = -1047903017151976827L;

    private Money balanceInfo;

    private String templateId;

    private String templateName;

    private String iconUrl;

    public DetailedBalanceInfo() {
    }

    public DetailedBalanceInfo(String templateId, Money balanceInfo, String templateName, String iconUrl) {
        this.balanceInfo = balanceInfo;
        this.templateId = templateId;
        this.templateName = templateName;
        this.iconUrl = iconUrl;
    }

    public Money getBalanceInfo() {
        return balanceInfo;
    }

    public void setBalanceInfo(Money balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DetailedBalanceInfo{");
        sb.append("balanceInfo=").append(balanceInfo);
        sb.append(", templateId='").append(templateId).append('\'');
        sb.append(", templateName='").append(templateName).append('\'');
        sb.append(", iconUrl='").append(iconUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
