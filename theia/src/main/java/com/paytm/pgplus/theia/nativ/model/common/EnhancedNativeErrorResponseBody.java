package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.ResultInfo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

public class EnhancedNativeErrorResponseBody implements Serializable {

    private static final long serialVersionUID = 6165425441252598979L;

    @NotNull
    @JsonProperty
    private ResultInfo resultInfo;

    @NotNull
    @JsonProperty
    private NativeRetryInfo retryInfo;

    @NotNull
    @JsonProperty
    private String callbackUrl;

    @NotNull
    @JsonProperty
    private Map<String, String> content;

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public NativeRetryInfo getRetryInfo() {
        return retryInfo;
    }

    public void setRetryInfo(NativeRetryInfo retryInfo) {
        this.retryInfo = retryInfo;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }
}
