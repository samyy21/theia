/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

import com.paytm.pgplus.biz.utils.ECreateOrderResponses;

public class FailureDescription implements Serializable {

    private static final long serialVersionUID = 16L;

    private ECreateOrderResponses createOrderResponses;
    private String internalFailureMsg;

    /**
     * @return the createOrderResponses
     */
    public ECreateOrderResponses getCreateOrderResponses() {
        return createOrderResponses;
    }

    /**
     * @param createOrderResponses
     *            the createOrderResponses to set
     */
    public void setCreateOrderResponses(final ECreateOrderResponses createOrderResponses) {
        this.createOrderResponses = createOrderResponses;
    }

    /**
     * @return the internalFailureMsg
     */
    public String getInternalFailureMsg() {
        return internalFailureMsg;
    }

    /**
     * @param internalFailureMsg
     *            the internalFailureMsg to set
     */
    public void setInternalFailureMsg(final String internalFailureMsg) {
        this.internalFailureMsg = internalFailureMsg;
    }

    public FailureDescription(final ECreateOrderResponses createOrderResponses, final String internalFailureMsg) {
        this.createOrderResponses = createOrderResponses;
        this.internalFailureMsg = internalFailureMsg;
    }
}