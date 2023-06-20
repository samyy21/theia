package com.paytm.pgplus.biz.core.model;

import java.io.Serializable;

/**
 * @author kartik
 * @date 07-07-2017
 */
public class QueryByMerchantRequestIdResponseBizBean implements Serializable {

    private static final long serialVersionUID = 2781831333488740471L;

    private String fundOrderId;
    private String requestId;

    public String getFundOrderId() {
        return fundOrderId;
    }

    public void setFundOrderId(String fundOrderId) {
        this.fundOrderId = fundOrderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("QueryByMerchantRequestIdResponseBizBean [fundOrderId=");
        builder.append(fundOrderId);
        builder.append(", requestId=");
        builder.append(requestId);
        builder.append("]");
        return builder.toString();
    }

}