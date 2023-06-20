/**
 *
 */
package com.paytm.pgplus.cashier.models;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;

/**
 * @author amit.dubey
 *
 */
public class CashierMerchant implements Serializable {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 8347337652989577971L;

    /** merchant id */
    @NotBlank(message = "{notblank}")
    private String merchantId;

    /**
     * The Alipay MID
     */
    @NotBlank(message = "{notblank}")
    private String internalMerchantId;

    /** true/false for payment retry */
    private boolean retryConfiguredForPayment;

    /**
     * @param merchantId
     * @throws CashierCheckedException
     */
    public CashierMerchant(String merchantId) throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(merchantId, "merchantId");
        this.merchantId = merchantId;
    }

    /**
     * @param merchantId
     * @param retryConfiguredForPayment
     * @throws CashierCheckedException
     */
    public CashierMerchant(String merchantId, boolean retryConfiguredForPayment, int maxRetryCount)
            throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(merchantId, "merchantId");
        this.merchantId = merchantId;

        this.retryConfiguredForPayment = retryConfiguredForPayment;
    }

    /**
     * @param merchantId
     * @param retryConfiguredForPayment
     * @throws CashierCheckedException
     */
    public CashierMerchant(String merchantId, String internalMerchantId, boolean retryConfiguredForPayment,
            int maxRetryCount) throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(merchantId, "merchantId");
        this.merchantId = merchantId;
        this.internalMerchantId = internalMerchantId;
        this.retryConfiguredForPayment = retryConfiguredForPayment;
    }

    /**
     * @return the merchantId
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * @return the retryConfiguredForPayment
     */
    public boolean isRetryConfiguredForPayment() {
        return retryConfiguredForPayment;
    }

    /**
     * @return the internalMerchantId
     */
    public String getInternalMerchantId() {
        return internalMerchantId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CashierMerchant [merchantId=");
        builder.append(merchantId);
        builder.append(", internalMerchantId=");
        builder.append(internalMerchantId);
        builder.append(", retryConfiguredForPayment=");
        builder.append(retryConfiguredForPayment);
        builder.append("]");
        return builder.toString();
    }
}