package com.paytm.pgplus.biz.core.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.enums.Route;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryPayResultRequestBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cashierRequestId;

    private boolean fromAoaMerchant;

    @JsonIgnore
    private Routes route;

    public QueryPayResultRequestBean(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public QueryPayResultRequestBean(String cashierRequestId, boolean fromAoaMerchant) {
        this.cashierRequestId = cashierRequestId;
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public QueryPayResultRequestBean(String cashierRequestId, boolean fromAoaMerchant, Routes route) {
        this.cashierRequestId = cashierRequestId;
        this.fromAoaMerchant = fromAoaMerchant;
        this.route = route;
    }

    public String getCashierRequestId() {
        return cashierRequestId;
    }

    public void setCashierRequestId(String cashierRequestId) {
        this.cashierRequestId = cashierRequestId;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }
}
