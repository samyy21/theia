package com.paytm.pgplus.theia.models;

import java.io.Serializable;

import com.paytm.pgplus.biz.workflow.model.UPIPSPResponseBody;

/**
 * @author Santosh chourasia
 *
 */
public class UPIPSPResponse implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3634728011842871891L;
    private UPIPSPResponseHeader head;
    private UPIPSPResponseBody body;

    public UPIPSPResponseHeader getHead() {
        return head;
    }

    public void setHead(UPIPSPResponseHeader head) {
        this.head = head;
    }

    public UPIPSPResponseBody getBody() {
        return body;
    }

    public void setBody(UPIPSPResponseBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UPIPSPResponse [head=").append(head).append(", body=").append(body).append("]");
        return builder.toString();
    }

}
