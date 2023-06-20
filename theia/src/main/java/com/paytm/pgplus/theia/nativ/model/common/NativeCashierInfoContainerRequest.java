package com.paytm.pgplus.theia.nativ.model.common;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;

public class NativeCashierInfoContainerRequest {

    private NativeCashierInfoRequest nativeCashierInfoRequest;

    private PaymentRequestBean paymentRequestBean;

    private NativePersistData nativePersistData;

    private boolean superGwApiHit;

    private boolean fetchQRDetailsRequest;

    public NativeCashierInfoContainerRequest(NativeCashierInfoRequest nativeCashierInfoRequest,
            PaymentRequestBean paymentRequestBean) {
        this.nativeCashierInfoRequest = nativeCashierInfoRequest;
        this.paymentRequestBean = paymentRequestBean;
    }

    public NativeCashierInfoContainerRequest(NativeCashierInfoRequest nativeCashierInfoRequest) {
        this.nativeCashierInfoRequest = nativeCashierInfoRequest;
    }

    public NativeCashierInfoRequest getNativeCashierInfoRequest() {
        return nativeCashierInfoRequest;
    }

    public void setNativeCashierInfoRequest(NativeCashierInfoRequest nativeCashierInfoRequest) {
        this.nativeCashierInfoRequest = nativeCashierInfoRequest;
    }

    public NativeCashierInfoContainerRequest(NativeCashierInfoRequest nativeCashierInfoRequest,
            NativePersistData nativePersistData) {
        this.nativeCashierInfoRequest = nativeCashierInfoRequest;
        this.nativePersistData = nativePersistData;
    }

    public boolean isSuperGwApiHit() {
        return superGwApiHit;
    }

    public void setSuperGwApiHit(boolean superGwApiHit) {
        this.superGwApiHit = superGwApiHit;
    }

    public PaymentRequestBean getPaymentRequestBean() {
        return paymentRequestBean;
    }

    public void setPaymentRequestBean(PaymentRequestBean paymentRequestBean) {
        this.paymentRequestBean = paymentRequestBean;
    }

    public NativePersistData getNativePersistData() {
        return nativePersistData;
    }

    public void setNativePersistData(NativePersistData nativePersistData) {
        this.nativePersistData = nativePersistData;
    }

    public boolean isFetchQRDetailsRequest() {
        return fetchQRDetailsRequest;
    }

    public void setFetchQRDetailsRequest(boolean fetchQRDetailsRequest) {
        this.fetchQRDetailsRequest = fetchQRDetailsRequest;
    }

    @Override
    public String toString() {
        return "NativeCashierInfoContainerRequest{" + "nativeCashierInfoRequest=" + nativeCashierInfoRequest
                + ", paymentRequestBean=" + paymentRequestBean + ", nativePersistData=" + nativePersistData
                + ", superGwApiHit=" + superGwApiHit + '}';
    }
}
