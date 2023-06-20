package com.paytm.pgplus.biz.core.model.request;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class CreateTopUpResponseBizBean implements Serializable {

    private static final long serialVersionUID = 13L;

    private String fundOrderId;
    private String requestID;

    public String getFundOrderId() {
        return fundOrderId;
    }

    public void setFundOrderId(String fundOrderId) {
        this.fundOrderId = fundOrderId;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public CreateTopUpResponseBizBean(String fundOrderId, String requestID) {
        this.fundOrderId = fundOrderId;
        this.requestID = requestID;
    }

}
