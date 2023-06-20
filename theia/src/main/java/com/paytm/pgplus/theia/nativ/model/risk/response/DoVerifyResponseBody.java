package com.paytm.pgplus.theia.nativ.model.risk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoVerifyResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 8847483783579025663L;

    private String isFinish;

    private String canRetry;

    private String nextMethod;

    private Map<String, String> renderData;

    public DoVerifyResponseBody() {
    }

    public DoVerifyResponseBody(String isFinish, String canRetry, String nextMethod, Map<String, String> renderData) {
        this.isFinish = isFinish;
        this.canRetry = canRetry;
        this.nextMethod = nextMethod;
        this.renderData = renderData;
    }

    public String getIsFinish() {
        return isFinish;
    }

    public String getCanRetry() {
        return canRetry;
    }

    public String getNextMethod() {
        return nextMethod;
    }

    public Map<String, String> getRenderData() {
        return renderData;
    }

    public void setIsFinish(String isFinish) {
        this.isFinish = isFinish;
    }

    public void setCanRetry(String canRetry) {
        this.canRetry = canRetry;
    }

    public void setNextMethod(String nextMethod) {
        this.nextMethod = nextMethod;
    }

    public void setRenderData(Map<String, String> renderData) {
        this.renderData = renderData;
    }

    @Override
    public String toString() {
        return "DoVerifyResponseBody{" + "isFinish='" + isFinish + '\'' + ", canRetry='" + canRetry + '\''
                + ", nextMethod='" + nextMethod + '\'' + ", renderData=" + renderData + '}';
    }
}
