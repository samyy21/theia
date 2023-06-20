package com.paytm.pgplus.theia.nativ.model.vpaValidate;

public class NativeValidateVpaRequest {

    private String mid;
    private String vpaAddress;
    private String orderId;
    private String requestType;
    private String queryParams;
    private boolean preAuth;
    private String numericId;

    public NativeValidateVpaRequest(String mid, String vpaAddress, String orderId) {
        this.mid = mid;
        this.vpaAddress = vpaAddress;
        this.orderId = orderId;
    }

    public NativeValidateVpaRequest(String mid, String vpaAddress, String orderId, String requestType) {
        this.mid = mid;
        this.vpaAddress = vpaAddress;
        this.orderId = orderId;
        this.requestType = requestType;
    }

    public NativeValidateVpaRequest(String mid, String vpaAddress, String orderId, String requestType, boolean preAuth) {
        this.mid = mid;
        this.vpaAddress = vpaAddress;
        this.orderId = orderId;
        this.requestType = requestType;
        this.preAuth = preAuth;
    }

    public NativeValidateVpaRequest(String mid, String vpaAddress, String orderId, String requestType, boolean preAuth,
            String numericId) {
        this.mid = mid;
        this.vpaAddress = vpaAddress;
        this.orderId = orderId;
        this.requestType = requestType;
        this.preAuth = preAuth;
        this.numericId = numericId;
    }

    public NativeValidateVpaRequest(String mid, String vpaAddress) {
        this.mid = mid;
        this.vpaAddress = vpaAddress;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getVpaAddress() {
        return vpaAddress;
    }

    public void setVpaAddress(String vpaAddress) {
        this.vpaAddress = vpaAddress;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public boolean isPreAuth() {
        return preAuth;
    }

    public String getNumericId() {
        return numericId;
    }

    public void setNumericId(String numericId) {
        this.numericId = numericId;
    }

    public void setPreAuth(boolean preAuth) {
        this.preAuth = preAuth;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MerchantRequest{");
        sb.append("mid='").append(mid).append('\'');
        sb.append(", vpaAddress='").append(vpaAddress).append('\'');
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", queryParams='").append(queryParams).append('\'');
        sb.append(", preAuth='").append(preAuth).append('\'');
        sb.append(", numericId='").append(numericId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
