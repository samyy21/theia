package com.paytm.pgplus.biz.core.model;

import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import java.io.Serializable;

/**
 * @author kartik
 * @date 07-07-2017
 */
public class QueryByMerchantRequestIdBizBean implements Serializable {

    private static final long serialVersionUID = -4037053971432320910L;

    private String requestId;
    private String merchantId;
    private Routes route;

    public QueryByMerchantRequestIdBizBean(String requestId, String merchantId) {
        this.requestId = requestId;
        this.merchantId = merchantId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("QueryByMerchantRequestIdBizBean [requestId=");
        builder.append(requestId);
        builder.append(", merchantId=");
        builder.append(merchantId);
        builder.append(", route=");
        builder.append(route);
        builder.append("]");
        return builder.toString();
    }

}
