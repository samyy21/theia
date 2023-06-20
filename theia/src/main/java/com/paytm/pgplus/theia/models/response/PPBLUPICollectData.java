package com.paytm.pgplus.theia.models.response;

import com.paytm.pgplus.response.BaseResponseBody;

/**
 * Created by anamika on 7/9/18.
 */
public class PPBLUPICollectData extends BaseResponseBody {

    private static final long serialVersionUID = -7348598205356917554L;
    private String vpa;
    private String mid;
    private boolean isValid;
    private String responseMsg;
    private String orderId;

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PPBLUPICollectData{");
        sb.append("vpa='").append(vpa).append('\'');
        sb.append(", mid='").append(mid).append('\'');
        sb.append(", isValidVPA=").append(isValid);
        sb.append(", orderId=").append(orderId);
        sb.append(", responseMsg='").append(responseMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
