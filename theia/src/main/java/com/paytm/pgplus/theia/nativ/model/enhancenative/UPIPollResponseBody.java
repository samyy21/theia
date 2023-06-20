package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

import java.util.Map;

/**
 * Created by rahulverma on 6/28/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UPIPollResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -871189165332301625L;

    private String callbackUrl;
    private UPIContent content;

    public UPIPollResponseBody() {
        ResultInfo resultInfo = new ResultInfo("S", "SUCCESS", "Success", true);
        super.setResultInfo(resultInfo);
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public UPIContent getContent() {
        return content;
    }

    public void setContent(UPIContent content) {
        this.content = content;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UPIPollResponseBody{");
        sb.append("content=").append(content);
        sb.append(", callbackUrl='").append(callbackUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
