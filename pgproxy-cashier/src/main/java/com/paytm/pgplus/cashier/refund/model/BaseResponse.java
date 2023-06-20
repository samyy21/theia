package com.paytm.pgplus.cashier.refund.model;

import com.paytm.pgplus.cashier.enums.TxnStatus;

/**
 * @author amit.dubey
 */
public class BaseResponse {

    private String mid;
    private String txnId;
    private String orderId;
    private String txnAmount;
    private String refundAmount;
    private String txnDate;
    private String respCode;
    private String respMsg;
    private TxnStatus status;
    private String refId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

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

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
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

    public TxnStatus getStatus() {
        return status;
    }

    public void setStatus(TxnStatus status) {
        this.status = status;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    @Override
    public String toString() {
        return "BaseResponse{" + "mid='" + mid + '\'' + ", txnId='" + txnId + '\'' + ", orderId='" + orderId + '\''
                + ", txnAmount='" + txnAmount + '\'' + ", refundAmount='" + refundAmount + '\'' + ", txnDate='"
                + txnDate + '\'' + ", respCode='" + respCode + '\'' + ", respMsg='" + respMsg + '\'' + ", status="
                + status + ", refId='" + refId + '\'' + '}';
    }
}
