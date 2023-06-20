package com.paytm.pgplus.theia.nativ.model.risk.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoViewResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 2865519982324212763L;

    @JsonProperty("method")
    private String method;

    @JsonProperty("renderData")
    private Map<String, String> renderData;

    public DoViewResponseBody() {
    }

    public DoViewResponseBody(String method, Map<String, String> renderData) {
        super();
        this.method = method;
        this.renderData = renderData;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getRenderData() {
        return renderData;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRenderData(Map<String, String> renderData) {
        this.renderData = renderData;
    }

    @Override
    public String toString() {
        return "DoViewResponseBody{" + "method='" + method + '\'' + ", renderData=" + renderData + '}';
    }
}
