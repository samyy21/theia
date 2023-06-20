/**
 *
 */
package com.paytm.pgplus.cashier.looper.model;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;

/**
 * @author amit.dubey
 *
 */
public class LooperRequest implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4642242271260738694L;

    // check payment status data
    @NotBlank(message = "{notblank}")
    private String cashierRequestId;

    // check transaction status by acquirement id
    private String merchantId;
    private String acquirementId;
    private final boolean needFullInfo = true;

    // check transaction status by fund order id
    private String fundOrderId;

    /**
     * @param cashierRequestId
     * @throws CashierCheckedException
     */
    public LooperRequest(String cashierRequestId) throws CashierCheckedException {
        BeanParameterValidator.validateInputStringParam(cashierRequestId, "cashierRequestId");
        this.cashierRequestId = cashierRequestId;
    }

    /**
     * @param cashierRequestId
     * @param merchantId
     * @param acquirementId
     * @param needFullInfo
     * @throws CashierCheckedException
     */
    public LooperRequest(String cashierRequestId, String merchantId, String acquirementId)
            throws CashierCheckedException {
        this(cashierRequestId);

        BeanParameterValidator.validateInputStringParam(merchantId, "merchantId");
        this.merchantId = merchantId;

        BeanParameterValidator.validateInputStringParam(acquirementId, "acquirementId");
        this.acquirementId = acquirementId;
    }

    /**
     * @param cashierRequestId
     * @param fundOrderId
     * @throws CashierCheckedException
     */
    public LooperRequest(String cashierRequestId, String fundOrderId) throws CashierCheckedException {
        this(cashierRequestId);

        BeanParameterValidator.validateInputStringParam(fundOrderId, "fundOrderId");
        this.fundOrderId = fundOrderId;
    }

    /**
     * @return the cashierRequestId
     */
    public String getCashierRequestId() {
        return cashierRequestId;
    }

    /**
     * @return the merchantId
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * @return the acquirementId
     */
    public String getAcquirementId() {
        return acquirementId;
    }

    /**
     * @return the needFullInfo
     */
    public Boolean getNeedFullInfo() {
        return needFullInfo;
    }

    /**
     * @return the fundOrderId
     */
    public String getFundOrderId() {
        return fundOrderId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LooperRequest [cashierRequestId=");
        builder.append(cashierRequestId);
        builder.append(", merchantId=");
        builder.append(merchantId);
        builder.append(", acquirementId=");
        builder.append(acquirementId);
        builder.append(", needFullInfo=");
        builder.append(needFullInfo);
        builder.append(", fundOrderId=");
        builder.append(fundOrderId);
        builder.append("]");
        return builder.toString();
    }
}