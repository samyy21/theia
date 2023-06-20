package com.paytm.pgplus.cashier.looper.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.paytm.pgplus.cashier.models.PayOption;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;

/**
 * @author amit.dubey
 *
 */
public class CashierPaymentStatus implements Serializable {
    /**
     * serial version uid
     */
    private static final long serialVersionUID = 1L;

    private String transId;
    private String transTypeDescription;
    private String transAmountCurrencyType;
    private String transAmountValue;
    private String payerUserId;
    private String paymentStatusValue;
    private String webFormContext;
    private String resultPageRedirectURL;
    private String paymentErrorCode;
    private String instErrorCode;
    private Date paidTime;

    private List<PayOption> payOptions;
    private Map<String, String> extendInfo;

    private boolean paymentRetryPossible;
    private String paytmResponseCode;
    private String errorMessage;
    private boolean bankFormFetchFailed;
    private String pwpCategory;

    public CashierPaymentStatus(CashierPaymentStatusBuilder builder) {
        this.transId = builder.transId;
        this.transTypeDescription = builder.transTypeDescription;
        this.transAmountCurrencyType = builder.transAmountCurrencyType;
        this.transAmountValue = builder.transAmountValue;
        this.payerUserId = builder.payerUserId;
        this.paymentStatusValue = builder.paymentStatusValue;
        this.webFormContext = builder.webFormContext;
        this.resultPageRedirectURL = builder.resultPageRedirectURL;
        this.paymentErrorCode = builder.paymentErrorCode;
        this.instErrorCode = builder.instErrorCode;
        this.paidTime = builder.paidTime;
        this.payOptions = builder.payOptions;
        this.extendInfo = builder.extendInfo;
        this.paymentRetryPossible = builder.paymentRetryPossible;
        this.paytmResponseCode = builder.paytmResponseCode;
        this.errorMessage = builder.errorMessage;
        this.pwpCategory = builder.pwpCategory;
    }

    public CashierPaymentStatus(QueryPaymentStatus queryPaymentStatus) {
        this.transId = queryPaymentStatus.getTransId();
        this.transTypeDescription = queryPaymentStatus.getTransTypeDescription();
        this.transAmountCurrencyType = queryPaymentStatus.getTransAmountCurrencyType();
        this.transAmountValue = queryPaymentStatus.getTransAmountValue();
        this.payerUserId = queryPaymentStatus.getPayerUserId();
        this.paymentStatusValue = queryPaymentStatus.getPaymentStatusValue();
        this.webFormContext = queryPaymentStatus.getWebFormContext();
        this.resultPageRedirectURL = queryPaymentStatus.getResultPageRedirectURL();
        this.paymentErrorCode = queryPaymentStatus.getPaymentErrorCode();
        this.instErrorCode = queryPaymentStatus.getInstErrorCode();
        this.paidTime = queryPaymentStatus.getPaidTime();
        // this.payOptions = queryPaymentStatus.getPayOptions();
        this.extendInfo = queryPaymentStatus.getExtendInfo();
        this.paymentRetryPossible = false;
        this.paytmResponseCode = queryPaymentStatus.getPaytmResponseCode();
        this.errorMessage = queryPaymentStatus.getErrorMessage();
    }

    public void setPayOptions(List<PayOption> payOptions) {
        this.payOptions = payOptions;
    }

    /**
     * @return the paytmResponseCode
     */
    public String getPaytmResponseCode() {
        return paytmResponseCode;
    }

    /**
     * @return the isPaymentRetryPossible
     */
    public boolean isPaymentRetryPossible() {
        return paymentRetryPossible;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return the transId
     */
    public String getTransId() {
        return transId;
    }

    /**
     * @return the transTypeDescription
     */
    public String getTransTypeDescription() {
        return transTypeDescription;
    }

    /**
     * @return the transAmountCurrencyType
     */
    public String getTransAmountCurrencyType() {
        return transAmountCurrencyType;
    }

    /**
     * @return the transAmountValue
     */
    public String getTransAmountValue() {
        return transAmountValue;
    }

    /**
     * @return the payerUserId
     */
    public String getPayerUserId() {
        return payerUserId;
    }

    /**
     * @return the paymentStatusValue
     */
    public String getPaymentStatusValue() {
        return paymentStatusValue;
    }

    /**
     * @return the webFormContext
     */
    public String getWebFormContext() {
        return webFormContext;
    }

    /**
     * @return the resultPageRedirectURL
     */
    public String getResultPageRedirectURL() {
        return resultPageRedirectURL;
    }

    /**
     * @return the paymentErrorCode
     */
    public String getPaymentErrorCode() {
        return paymentErrorCode;
    }

    /**
     * @return the instErrorCode
     */
    public String getInstErrorCode() {
        return instErrorCode;
    }

    /**
     * @return the paidTime
     */
    public Date getPaidTime() {
        return paidTime;
    }

    /**
     * @return the payOptions
     */
    public List<PayOption> getPayOptions() {
        return payOptions;
    }

    /**
     * @return the extendInfo
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    public boolean isBankFormFetchFailed() {
        return bankFormFetchFailed;
    }

    public void setBankFormFetchFailed(boolean bankFormFetchFailed) {
        this.bankFormFetchFailed = bankFormFetchFailed;
    }

    public String getPwpCategory() {
        return pwpCategory;
    }

    public void setPwpCategory(String pwpCategory) {
        this.pwpCategory = pwpCategory;
    }

    public static class CashierPaymentStatusBuilder {
        private String transId;
        private String transTypeDescription;
        private String transAmountCurrencyType;
        private String transAmountValue;
        private String payerUserId;
        private String paymentStatusValue;
        private String webFormContext;
        private String resultPageRedirectURL;
        private String paymentErrorCode;
        private String instErrorCode;
        private Date paidTime;
        private List<PayOption> payOptions;
        private Map<String, String> extendInfo;
        private boolean paymentRetryPossible;
        private String paytmResponseCode;
        private String errorMessage;
        private String pwpCategory;

        /**
         * @param transId
         * @param transTypeDescription
         * @param transAmountCurrencyType
         * @param transAmountValue
         * @param payerUserId
         * @param paymentStatusValue
         */
        public CashierPaymentStatusBuilder(String transId, String transTypeDescription, String transAmountCurrencyType,
                String transAmountValue, String payerUserId, String paymentStatusValue) {
            this.transId = transId;
            this.transTypeDescription = transTypeDescription;
            this.transAmountCurrencyType = transAmountCurrencyType;
            this.transAmountValue = transAmountValue;
            this.payerUserId = payerUserId;
            this.paymentStatusValue = paymentStatusValue;
        }

        /**
         * @param transId
         *            the transId to set
         */
        public CashierPaymentStatusBuilder setTransId(String transId) {
            this.transId = transId;
            return this;
        }

        public CashierPaymentStatusBuilder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * @param transTypeDescription
         *            the transTypeDescription to set
         */
        public CashierPaymentStatusBuilder setTransTypeDescription(String transTypeDescription) {
            this.transTypeDescription = transTypeDescription;
            return this;
        }

        /**
         * @param transAmountCurrencyType
         *            the transAmountCurrencyType to set
         */
        public CashierPaymentStatusBuilder setTransAmountCurrencyType(String transAmountCurrencyType) {
            this.transAmountCurrencyType = transAmountCurrencyType;
            return this;
        }

        /**
         * @param transAmountValue
         *            the transAmountValue to set
         */
        public CashierPaymentStatusBuilder setTransAmountValue(String transAmountValue) {
            this.transAmountValue = transAmountValue;
            return this;
        }

        /**
         * @param payerUserId
         *            the payerUserId to set
         */
        public CashierPaymentStatusBuilder setPayerUserId(String payerUserId) {
            this.payerUserId = payerUserId;
            return this;
        }

        /**
         * @param paymentStatusValue
         *            the paymentStatusValue to set
         */
        public CashierPaymentStatusBuilder setPaymentStatusValue(String paymentStatusValue) {
            this.paymentStatusValue = paymentStatusValue;
            return this;
        }

        /**
         * @param webFormContext
         *            the webFormContext to set
         */
        public CashierPaymentStatusBuilder setWebFormContext(String webFormContext) {
            this.webFormContext = webFormContext;
            return this;
        }

        /**
         * @param resultPageRedirectURL
         *            the resultPageRedirectURL to set
         */
        public CashierPaymentStatusBuilder setResultPageRedirectURL(String resultPageRedirectURL) {
            this.resultPageRedirectURL = resultPageRedirectURL;
            return this;
        }

        /**
         * @param paymentErrorCode
         *            the paymentErrorCode to set
         */
        public CashierPaymentStatusBuilder setPaymentErrorCode(String paymentErrorCode) {
            this.paymentErrorCode = paymentErrorCode;
            return this;
        }

        /**
         * @param instErrorCode
         *            the instErrorCode to set
         */
        public CashierPaymentStatusBuilder setInstErrorCode(String instErrorCode) {
            this.instErrorCode = instErrorCode;
            return this;
        }

        /**
         * @param paidTime
         *            the paidTime to set
         */
        public CashierPaymentStatusBuilder setPaidTime(Date paidTime) {
            this.paidTime = paidTime;
            return this;
        }

        /**
         * @param payOptions
         *            the payOptions to set
         */
        public CashierPaymentStatusBuilder setPayOptions(List<PayOption> payOptions) {
            this.payOptions = payOptions;
            return this;
        }

        /**
         * @param extendInfo
         *            the extendInfo to set
         */
        public CashierPaymentStatusBuilder setExtendInfo(Map<String, String> extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        /**
         * @param extendInfo
         *            the extendInfo to set
         */
        public CashierPaymentStatusBuilder setPaymentRetryPossible(boolean paymentRetryPossible) {
            this.paymentRetryPossible = paymentRetryPossible;
            return this;
        }

        /**
         * @param extendInfo
         *            the extendInfo to set
         */
        public CashierPaymentStatusBuilder setPaytmResponseCode(String paytmResponseCode) {
            this.paytmResponseCode = paytmResponseCode;
            return this;
        }

        public CashierPaymentStatusBuilder setPwpCategory(String pwpCategory) {
            this.pwpCategory = pwpCategory;
            return this;
        }

        public CashierPaymentStatus build() {
            return new CashierPaymentStatus(this);
        }
    }

    // for Offline flow
    public void setPaymentStatusValue(String paymentStatusValue) {
        this.paymentStatusValue = paymentStatusValue;
    }

    public void setWebFormContext(String webFormContext) {
        this.webFormContext = webFormContext;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CashierPaymentStatus [transId=").append(transId).append(", transTypeDescription=")
                .append(transTypeDescription).append(", transAmountCurrencyType=").append(transAmountCurrencyType)
                .append(", transAmountValue=").append(transAmountValue).append(", payerUserId=").append(payerUserId)
                .append(", paymentStatusValue=").append(paymentStatusValue).append(", webFormContext=")
                .append(webFormContext).append(", resultPageRedirectURL=").append(resultPageRedirectURL)
                .append(", paymentErrorCode=").append(paymentErrorCode).append(", instErrorCode=")
                .append(instErrorCode).append(", paidTime=").append(paidTime).append(", payOptions=")
                .append(payOptions).append(", extendInfo=").append(extendInfo).append(", paymentRetryPossible=")
                .append(paymentRetryPossible).append(", paytmResponseCode=").append(paytmResponseCode)
                .append(", errorMessage=").append(errorMessage).append("]");
        return builder.toString();
    }

}