package com.paytm.pgplus.theia.nativ.model.prn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.response.BaseResponseBody;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NativeValidatePRNResponseBody extends BaseResponseBody {

    private String status;

    private boolean retryAllowed;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRetryAllowed() {
        return retryAllowed;
    }

    public void setRetryAllowed(boolean retryAllowed) {
        this.retryAllowed = retryAllowed;
    }

    public NativeValidatePRNResponseBody() {
    }

    public NativeValidatePRNResponseBody(String status, boolean retryAllowed) {
        this.status = status;
        this.retryAllowed = retryAllowed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NativeValidatePRNResponseBody{");
        sb.append("status='").append(status).append('\'');
        sb.append(", retryAllowed=").append(retryAllowed);
        sb.append('}');
        return sb.toString();
    }
}
