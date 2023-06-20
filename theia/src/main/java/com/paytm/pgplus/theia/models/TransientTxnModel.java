/**
 * 
 */
package com.paytm.pgplus.theia.models;

import java.io.Serializable;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;

/**
 * @author namanjain
 *
 */
public class TransientTxnModel implements Serializable {

    private static final long serialVersionUID = 8201326115699248510L;

    private PaymentRequestBean paymentRequestModel;

    private String errorResponseCode;

    private String merchantErrMsg;

    public String getErrorResponseCode() {
        return errorResponseCode;
    }

    public void setErrorResponseCode(String errorResponseCode) {
        this.errorResponseCode = errorResponseCode;
    }

    public String getMerchantErrMsg() {
        return merchantErrMsg;
    }

    public void setMerchantErrMsg(String merchantErrMsg) {
        this.merchantErrMsg = merchantErrMsg;
    }

    public PaymentRequestBean getPaymentRequestModel() {
        return paymentRequestModel;
    }

    public void setPaymentRequestModel(PaymentRequestBean paymentRequestModel) {
        this.paymentRequestModel = paymentRequestModel;
    }

}
