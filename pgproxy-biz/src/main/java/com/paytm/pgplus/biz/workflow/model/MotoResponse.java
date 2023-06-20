package com.paytm.pgplus.biz.workflow.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by shubham singh on 13/12/17.
 */
public class MotoResponse implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = -1613342512151991619L;

    @JsonProperty("TXNID")
    private String txnId;

    @JsonProperty("ORDERID")
    private String orderId;

    @JsonProperty("TXNAMOUNT")
    private String txnAmount;

    @JsonProperty("RESPCODE")
    private String respCode;

    @JsonProperty("RESPMSG")
    private String respMsg;

    @JsonProperty("MID")
    private String mid;

    @JsonProperty("STATUS")
    private String status;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MotoResponse [txnId=").append(txnId).append(", orderId=").append(orderId)
                .append(", txnAmount=").append(txnAmount).append(", respCode=").append(respCode).append(", respMsg=")
                .append(respMsg).append(", mid=").append(mid).append(", status=").append(status).append("]");
        return builder.toString();
    }

}
