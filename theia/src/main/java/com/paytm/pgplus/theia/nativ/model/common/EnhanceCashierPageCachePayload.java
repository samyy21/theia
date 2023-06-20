package com.paytm.pgplus.theia.nativ.model.common;

import com.paytm.pgplus.cache.annotation.RedisEncrypt;
import com.paytm.pgplus.cache.annotation.RedisEncryptAttribute;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.response.InitiateTransactionResponse;

import java.io.Serializable;

@RedisEncrypt
public class EnhanceCashierPageCachePayload implements Serializable {

    private static final long serialVersionUID = 5510352391208382404L;

    private PaymentRequestBean merchantRequestData;

    private InitiateTransactionResponse initiateTransactionResponse;

    @RedisEncryptAttribute
    private EnhancedCashierPage enhancedCashierPage;

    public EnhanceCashierPageCachePayload(PaymentRequestBean merchantRequestData,
            InitiateTransactionResponse initiateTransactionResponse, EnhancedCashierPage enhancedCashierPage) {
        this.merchantRequestData = merchantRequestData;
        this.initiateTransactionResponse = initiateTransactionResponse;
        this.enhancedCashierPage = enhancedCashierPage;
    }

    public PaymentRequestBean getMerchantRequestData() {
        return merchantRequestData;
    }

    public void setMerchantRequestData(PaymentRequestBean merchantRequestData) {
        this.merchantRequestData = merchantRequestData;
    }

    public InitiateTransactionResponse getInitiateTransactionResponse() {
        return initiateTransactionResponse;
    }

    public void setInitiateTransactionResponse(InitiateTransactionResponse initiateTransactionResponse) {
        this.initiateTransactionResponse = initiateTransactionResponse;
    }

    public EnhancedCashierPage getEnhancedCashierPage() {
        return enhancedCashierPage;
    }

    public void setEnhancedCashierPage(EnhancedCashierPage enhancedCashierPage) {
        this.enhancedCashierPage = enhancedCashierPage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnhanceCashierPageCachePayload{");
        sb.append("initiateTransactionResponse=").append(initiateTransactionResponse);
        sb.append(", merchantRequestData=").append(merchantRequestData);
        sb.append(", enhancedCashierPage=").append(enhancedCashierPage);
        sb.append('}');
        return sb.toString();
    }
}
