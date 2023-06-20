/**
 * 
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author vaishakhnair
 *
 */
public class BizCancelTransactionRequest implements Serializable {

    private static final long serialVersionUID = -3190066335800418043L;

    private String internalMid;
    private String transId;
    private String cancelReason;

    /**
     * @param internalMid
     * @param transId
     * @param cancelReason
     */
    public BizCancelTransactionRequest(String internalMid, String transId, String cancelReason) {
        this.internalMid = internalMid;
        this.transId = transId;
        this.cancelReason = cancelReason;
    }

    /**
     * @param internalMid
     * @param transId
     */
    public BizCancelTransactionRequest(String internalMid, String transId) {
        this.internalMid = internalMid;
        this.transId = transId;
    }

    /**
     * @return the internalMid
     */
    public String getInternalMid() {
        return internalMid;
    }

    /**
     * @return the transId
     */
    public String getTransId() {
        return transId;
    }

    /**
     * @return the cancelReason
     */
    public String getCancelReason() {
        return cancelReason;
    }

}
