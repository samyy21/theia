package com.paytm.pgplus.biz.core.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

import java.io.Serializable;

/**
 * @author manojpal
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryByAcquirementIdRequestBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String merchantId;
    private String acquirementId;
    private boolean fromAoaMerchant;
    @JsonIgnore
    private Routes routes;

    private String paytmMerchantId;

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getAcquirementId() {
        return acquirementId;
    }

    public void setAcquirementId(String acquirementId) {
        this.acquirementId = acquirementId;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public Routes getRoutes() {
        return routes;
    }

    public void setRoutes(Routes routes) {
        this.routes = routes;
    }
}
