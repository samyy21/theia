package com.paytm.pgplus.cashier.refund.model;

/**
 * @author amit.dubey
 */
public class RefundRequest extends BaseRequest {

    /**
     * serial version uid
     */
    private static final long serialVersionUID = 5182110538142902764L;

    private String txnId;
    private String refundAmount;
    private String txnType;
    private String comments;
    private String checkSum;

    public RefundRequest(String mid, String orderId, String refId) {
        super(mid, orderId, refId);
    }

    /**
     * @return the txnId
     */
    public String getTxnId() {
        return txnId;
    }

    /**
     * @param txnId
     *            the txnId to set
     */
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    /**
     * @return the refundAmount
     */
    public String getRefundAmount() {
        return refundAmount;
    }

    /**
     * @param refundAmount
     *            the refundAmount to set
     */
    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    /**
     * @return the txnType
     */
    public String getTxnType() {
        return txnType;
    }

    /**
     * @param txnType
     *            the txnType to set
     */
    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * @param comments
     *            the comments to set
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * @return the checkSum
     */
    public String getCheckSum() {
        return checkSum;
    }

    /**
     * @param checkSum
     *            the checkSum to set
     */
    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

}
