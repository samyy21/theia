package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UPIPushResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 8423925294843716264L;

    private String callbackUrl;
    private String method;
    private Map<String, String> content;

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

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public UPIPushResponseBody() {
        ResultInfo resultInfo = new ResultInfo("S", "SUCCESS", "Success", true);
        super.setResultInfo(resultInfo);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UPIPushResponseBody{");
        sb.append("content=").append(content);
        sb.append(", callbackUrl='").append(callbackUrl).append('\'');
        sb.append(", resultInfo='").append(super.getResultInfo());
        sb.append('}');
        return sb.toString();
    }
}
