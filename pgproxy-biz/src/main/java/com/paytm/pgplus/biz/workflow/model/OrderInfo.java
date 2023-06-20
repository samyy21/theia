package com.paytm.pgplus.biz.workflow.model;

public class OrderInfo {
    private String mid;
    private String orderId;
    private String alipayMid;
    private String acquirementId;
    private String payMethod;
    private String payOption;
    private Long orderExpiry;
    private String status;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAlipayMid() {
        return alipayMid;
    }

    public void setAlipayMid(String alipayMid) {
        this.alipayMid = alipayMid;
    }

    public String getAcquirementId() {
        return acquirementId;
    }

    public void setAcquirementId(String acquirementId) {
        this.acquirementId = acquirementId;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPayOption() {
        return payOption;
    }

    public void setPayOption(String payOption) {
        this.payOption = payOption;
    }

    public Long getOrderExpiry() {
        return orderExpiry;
    }

    public void setOrderExpiry(Long orderExpiry) {
        this.orderExpiry = orderExpiry;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OrderInfo{" + "mid='" + mid + '\'' + ", orderId='" + orderId + '\'' + ", alipayMid='" + alipayMid
                + '\'' + ", acquirementId='" + acquirementId + '\'' + ", payMethod='" + payMethod + '\''
                + ", payOption='" + payOption + '\'' + ", orderExpiry='" + orderExpiry + '\'' + ", status='" + status
                + '\'' + '}';
    }
}
