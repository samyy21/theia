package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.RiskContent;
import com.paytm.pgplus.response.BaseResponseBody;

import java.util.Map;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankRedirectionDetail extends BaseResponseBody {

    private static final long serialVersionUID = -4218958030611582746L;

    private String callbackUrl;

    private String method;

    private Map<String, String> content;

    private Map<String, String> metaData;

    private String directHtml;

    private BankForm bankForm;

    private RiskContent riskContent;

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String url) {
        this.callbackUrl = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public String getDirectHtml() {
        return directHtml;
    }

    public void setDirectHtml(String directHtml) {
        this.directHtml = directHtml;
    }

    public BankForm getBankForm() {
        return bankForm;
    }

    public void setBankForm(BankForm bankForm) {
        this.bankForm = bankForm;
    }

    public RiskContent getRiskContent() {
        return riskContent;
    }

    public void setRiskContent(RiskContent riskContent) {
        this.riskContent = riskContent;
    }

    @Override
    public String toString() {
        return "BankRedirectionDetail{" + "url='" + callbackUrl + '\'' + ", method='" + method + '\'' + ", content="
                + content + ", metaData=" + metaData + '}';
    }
}
