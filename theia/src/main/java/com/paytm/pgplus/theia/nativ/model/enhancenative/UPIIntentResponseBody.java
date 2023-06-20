package com.paytm.pgplus.theia.nativ.model.enhancenative;

import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.response.ResultInfo;

import java.util.Map;

public class UPIIntentResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 4466700578143983236L;
    private String callbackUrl;
    private Map<String, String> content;

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

    public UPIIntentResponseBody() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UPIIntentResponseBody{");
        sb.append("content=").append(content);
        sb.append(", callbackUrl='").append(callbackUrl).append('\'');
        sb.append(", resultInfo='").append(super.getResultInfo());
        sb.append('}');
        return sb.toString();
    }
}
