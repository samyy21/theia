/**
 *
 */
package com.paytm.pgplus.cashier.pay.model;

import java.io.Serializable;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.paytm.pgplus.facade.enums.PaymentScenario;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import com.paytm.pgplus.cashier.enums.PaymentType;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierEnvInfo;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;
import com.paytm.pgplus.facade.enums.TransType;

/**
 * @author amit.dubey
 *
 */
public class PaymentRequest implements Serializable {
    /**
     * serial version UID
     */
    private static final long serialVersionUID = -5142278190562424976L;

    @NotNull(message = "{notnull}")
    private PaymentType paymentType;

    @NotBlank(message = "{notblank}")
    private String transId;

    @NotNull(message = "{notnull}")
    private TransType transType;

    /** payer user id if user login */
    private String payerUserId;

    @NotBlank(message = "{notblank}")
    private String requestId;

    @NotNull(message = "{notnull}")
    @Valid
    private PayBillOptions payBillOptions;

    @NotNull(message = "{notnull}")
    private CashierEnvInfo cashierEnvInfo;

    private String securityId;

    @NotEmpty(message = "{notnull}")
    private Map<String, String> extendInfo;

    @NotEmpty(message = "{notnull}")
    private Map<String, String> riskExtendInfo;

    private PaymentScenario paymentScenario;

    public PaymentRequest(PaymentRequestBuilder builder) {
        this.paymentType = builder.paymentType;
        this.transId = builder.transId;
        this.transType = builder.transType;
        this.payerUserId = builder.payerUserId;
        this.requestId = builder.requestId;
        this.payBillOptions = builder.payBillOptions;
        this.cashierEnvInfo = builder.cashierEnvInfo;
        this.securityId = builder.securityId;
        this.extendInfo = builder.extendInfo;
        this.riskExtendInfo = builder.riskExtendInfo;
    }

    /**
     * @return the payerUserId
     */
    public String getPayerUserId() {
        return payerUserId;
    }

    /**
     * @return the transId
     */
    public String getTransId() {
        return transId;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @return the envInfo
     */
    public CashierEnvInfo getCashierEnvInfo() {
        return cashierEnvInfo;
    }

    /**
     * @return the securityId
     */
    public String getSecurityId() {
        return securityId;
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    /**
     * @return the transTypeVal
     */
    public TransType getTransType() {
        return transType;
    }

    /**
     * @return the payBillOptions
     */
    public PayBillOptions getPayBillOptions() {
        return payBillOptions;
    }

    /**
     * @return the paymentType
     */
    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setRequestId(String generateRequestId) {
        this.requestId = generateRequestId;
    }

    public Map<String, String> getRiskExtendInfo() {
        return riskExtendInfo;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public void setPaymentScenario(PaymentScenario paymentScenario) {
        this.paymentScenario = paymentScenario;
    }

    public PaymentScenario getPaymentScenario() {
        return paymentScenario;
    }

    public static class PaymentRequestBuilder {

        private PaymentType paymentType;
        private String transId;
        private TransType transType;
        private String requestId;
        private PayBillOptions payBillOptions;
        private CashierEnvInfo cashierEnvInfo;
        private String payerUserId;
        private String securityId;
        private Map<String, String> extendInfo;
        private Map<String, String> riskExtendInfo;

        /**
         * @param transId
         * @param transTypeVal
         * @param requestId
         * @param payBillOptions
         * @param envInfo
         * @throws CashierCheckedException
         */
        public PaymentRequestBuilder(PaymentType paymentType, String transId, TransType transType, String requestId,
                PayBillOptions payBillOptions, CashierEnvInfo cashierEnvInfo) throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(paymentType, "paymentType");
            this.paymentType = paymentType;

            BeanParameterValidator.validateInputStringParam(transId, "transId");
            this.transId = transId;

            BeanParameterValidator.validateInputObjectParam(transType, "transType");
            this.transType = transType;

            BeanParameterValidator.validateInputStringParam(requestId, "requestId");
            this.requestId = requestId;

            BeanParameterValidator.validateInputObjectParam(payBillOptions, "payBillOptions");
            this.payBillOptions = payBillOptions;

            BeanParameterValidator.validateInputObjectParam(cashierEnvInfo, "cashierEnvInfo");
            this.cashierEnvInfo = cashierEnvInfo;
        }

        public PaymentRequestBuilder setPayerUserId(String payerUserId) throws CashierCheckedException {
            this.payerUserId = payerUserId;
            return this;
        }

        public PaymentRequestBuilder setSecurityId(String securityId) throws CashierCheckedException {
            this.securityId = securityId;
            return this;
        }

        public PaymentRequestBuilder setExtendInfo(Map<String, String> extendInfo) throws CashierCheckedException {
            this.extendInfo = extendInfo;
            return this;
        }

        public PaymentRequestBuilder setRiskExtendInfo(Map<String, String> riskExtendInfo)
                throws CashierCheckedException {
            this.riskExtendInfo = riskExtendInfo;
            return this;
        }

        public PaymentRequestBuilder setPaymentType(PaymentType paymentType) throws CashierCheckedException {
            this.paymentType = paymentType;
            return this;
        }

        public PaymentRequestBuilder setPayBillOptions(PayBillOptions payBillOptions) throws CashierCheckedException {
            this.payBillOptions = payBillOptions;
            return this;
        }

        public PaymentRequest build() {
            return new PaymentRequest(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PaymentRequest [paymentType=").append(paymentType).append(", transId=").append(transId)
                .append(", transType=").append(transType).append(", payerUserId=").append(payerUserId)
                .append(", requestId=").append(requestId).append(", payBillOptions=").append(payBillOptions)
                .append(", cashierEnvInfo=").append(cashierEnvInfo).append(", securityId=").append(securityId)
                .append(", extendInfo=").append(extendInfo).append(", riskExtendInfo=").append(riskExtendInfo)
                .append("]");
        return builder.toString();
    }
}