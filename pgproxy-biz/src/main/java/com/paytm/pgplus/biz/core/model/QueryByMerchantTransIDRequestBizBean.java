/**
 * 
 */
package com.paytm.pgplus.biz.core.model;

import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class QueryByMerchantTransIDRequestBizBean implements Serializable {

    private static final long serialVersionUID = -743034453083726539L;

    private String merchantID;

    private String merchantTransID;

    private boolean isNeedFullInfoRequired;

    public boolean isNeedFullInfoRequired() {
        return isNeedFullInfoRequired;
    }

    private String paytmMID;

    private Routes route;

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    public String getPaytmMID() {
        return paytmMID;
    }

    public void setNeedFullInfoRequired(boolean isNeedFullInfoRequired) {
        this.isNeedFullInfoRequired = isNeedFullInfoRequired;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public String getMerchantTransID() {
        return merchantTransID;
    }

    public void setMerchantTransID(String merchantTransID) {
        this.merchantTransID = merchantTransID;
    }

    public QueryByMerchantTransIDRequestBizBean(String merchantID, String merchantTransID,
            boolean isNeedFullInfoRequired, String paytmMID, Routes route) {
        this.merchantID = merchantID;
        this.merchantTransID = merchantTransID;
        this.isNeedFullInfoRequired = isNeedFullInfoRequired;
        this.paytmMID = paytmMID;
        this.route = route;
    }

    @Override
    public String toString() {
        return "QueryByMerchantTransIDRequestBizBean [merchantID=" + merchantID + ", merchantTransID="
                + merchantTransID + ", isNeedFullInfoRequired=" + isNeedFullInfoRequired + "]";
    }

}
