/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

import java.io.Serializable;

/**
 * @author namanjain
 *
 */
public class BizCancelOrderRequest implements Serializable {

    private static final long serialVersionUID = 7L;
    private String merchantId;
    private String acquirementId;
    private String closeReason;
    private boolean fromAoaMerchant;
    private String paytmMerchantId;
    private Routes route;

    public BizCancelOrderRequest(final String merchantId, final String acquirementId, final String closeReason) {
        this.merchantId = merchantId;
        this.acquirementId = acquirementId;
        this.closeReason = closeReason;
    }

    public BizCancelOrderRequest(final String merchantId, final String acquirementId, final String closeReason,
            boolean fromAoaMerchant) {
        this.merchantId = merchantId;
        this.acquirementId = acquirementId;
        this.closeReason = closeReason;
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public BizCancelOrderRequest(final String merchantId, final String acquirementId, final String closeReason,
            boolean fromAoaMerchant, String paytmMerchantId, Routes route) {
        this.merchantId = merchantId;
        this.acquirementId = acquirementId;
        this.closeReason = closeReason;
        this.fromAoaMerchant = fromAoaMerchant;
        this.paytmMerchantId = paytmMerchantId;
        this.route = route;
    }

    public BizCancelOrderRequest(final String merchantId, final String acquirementId) {
        this.merchantId = merchantId;
        this.acquirementId = acquirementId;
    }

    public BizCancelOrderRequest(final String merchantId, final String acquirementId, boolean fromAoaMerchant) {
        this.merchantId = merchantId;
        this.acquirementId = acquirementId;
        this.fromAoaMerchant = fromAoaMerchant;
    }

    /**
     * @return the merchantId
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * @param merchantId
     *            the merchantId to set
     */
    public void setMerchantId(final String merchantId) {
        this.merchantId = merchantId;
    }

    /**
     * @return the acquirementId
     */
    public String getAcquirementId() {
        return acquirementId;
    }

    /**
     * @param acquirementId
     *            the acquirementId to set
     */
    public void setAcquirementId(final String acquirementId) {
        this.acquirementId = acquirementId;
    }

    /**
     * @return the closeReason
     */
    public String getCloseReason() {
        return closeReason;
    }

    /**
     * @param closeReason
     *            the closeReason to set
     */
    public void setCloseReason(final String closeReason) {
        this.closeReason = closeReason;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizCancelOrderRequest{");
        sb.append("acquirementId='").append(acquirementId).append('\'');
        sb.append(", closeReason='").append(closeReason).append('\'');
        sb.append(", fromAoaMerchant=").append(fromAoaMerchant);
        sb.append(", merchantId='").append(merchantId).append('\'');
        sb.append(", paytmMerchantId='").append(paytmMerchantId).append('\'');
        sb.append(", route='").append(route).append('\'');
        sb.append('}');
        return sb.toString();
    }
}