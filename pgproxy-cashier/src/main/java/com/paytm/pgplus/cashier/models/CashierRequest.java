/**
 *
 */
package com.paytm.pgplus.cashier.models;

import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.LooperRequest;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.pay.model.ValidationRequest;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.common.validator.GenericBeanValidator;
import com.paytm.pgplus.facade.enums.ProductCodes;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;

import java.io.Serializable;

/**
 * @author amit.dubey
 *
 */
public class CashierRequest implements Serializable {
    /** serial version UID */
    private static final long serialVersionUID = -4666145882534743833L;

    private transient String acquirementId;
    private transient String requestId;
    private CashierMerchant cashierMerchant;
    /** data for asset cache card API */
    private CardRequest cardRequest;
    /** data for cashier pay API */
    private PaymentRequest paymentRequest;
    /** data for fetching payment and transaction status */
    private transient LooperRequest looperRequest;
    private CashierWorkflow cashierWorkflow;
    private CompleteCardRequest internalCardRequest;
    private boolean isProcessed;
    private ValidationRequest validationRequest;
    private BinCardRequest binCardRequest;
    private boolean directBankCardPayRequest;
    private ProductCodes productCode;
    private String transCreatedtime;
    private EnvInfoRequestBean envInfo;
    private boolean isFundOrder;
    private DigitalCreditRequest digitalCreditRequest;
    private PaymentsBankRequest paymentsBankRequest;
    private boolean cardTokenRequired;
    private boolean linkBasedPaymentRequest;
    private String merchantName;
    private String merchantImage;
    private UPIPushRequest upiPushRequest;
    private boolean OnTheFlyKYCRequired;
    private boolean cartValidationRequired;
    private String ssoToken;
    private boolean fromAoaMerchant;
    private boolean isAsyncTxnStatusFlow;
    private String dummyAlipayMid;
    private Routes route;
    private String paytmMerchantId;

    public CashierRequest(CashierRequestBuilder builder) {
        this.requestId = builder.requestId;
        this.cashierMerchant = builder.cashierMerchant;
        this.cardRequest = builder.cardRequest;
        this.paymentRequest = builder.paymentRequest;
        this.cashierWorkflow = builder.cashierWorkflow;
        this.looperRequest = builder.looperRequest;
        this.acquirementId = builder.acquirementId;
        this.isProcessed = builder.isProcessed;
        this.validationRequest = builder.validationRequest;
        this.binCardRequest = builder.binCardRequest;
        this.directBankCardPayRequest = builder.directBankCardPayRequest;
        this.envInfo = builder.envInfo;
        this.isFundOrder = builder.isFundOrder;
        this.digitalCreditRequest = builder.digitalCreditRequest;
        this.paymentsBankRequest = builder.paymentsBankRequest;
        this.linkBasedPaymentRequest = builder.linkBasedPaymentRequest;
        this.merchantName = builder.merchantName;
        this.merchantImage = builder.merchantImage;
        this.upiPushRequest = builder.upiPushRequest;
        this.OnTheFlyKYCRequired = builder.OnTheFlyKYCRequired;
        this.fromAoaMerchant = builder.fromAoaMerchant;
        this.route = builder.route;
        this.paytmMerchantId = builder.paytmMerchantId;
    }

    public boolean isAsyncTxnStatusFlow() {
        return isAsyncTxnStatusFlow;
    }

    public void setAsyncTxnStatusFlow(boolean asyncTxnStatusFlow) {
        isAsyncTxnStatusFlow = asyncTxnStatusFlow;
    }

    public boolean isLinkBasedPaymentRequest() {
        return linkBasedPaymentRequest;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantImage() {
        return merchantImage;
    }

    public ProductCodes getProductCode() {
        return productCode;
    }

    public void setProductCode(ProductCodes productCode) {
        this.productCode = productCode;
    }

    public BinCardRequest getBinCardRequest() {
        return binCardRequest;
    }

    /**
     * @return the acquirementId
     */
    public String getAcquirementId() {
        return acquirementId;
    }

    /**
     * @return the cashierRequestId
     */
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the cashierMerchant
     */
    public CashierMerchant getCashierMerchant() {
        return cashierMerchant;
    }

    /**
     * @return the cashierCardRequest
     */
    public CardRequest getCardRequest() {
        return cardRequest;
    }

    /**
     * @return the cashierPayRequest
     */
    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    /**
     * @return the cashierWorkflow
     */
    public CashierWorkflow getCashierWorkflow() {
        return cashierWorkflow;
    }

    /**
     * @return the looperRequest
     */
    public LooperRequest getLooperRequest() {
        return looperRequest;
    }

    /**
     * @return the internalCardRequest
     */
    public CompleteCardRequest getInternalCardRequest() {
        return internalCardRequest;
    }

    /**
     * @param internalCardRequest
     *            the internalCardRequest to set
     */
    public void setInternalCardRequest(CompleteCardRequest internalCardRequest) {
        this.internalCardRequest = internalCardRequest;
    }

    public void setBinCardRequest(BinCardRequest binCardRequest) {
        this.binCardRequest = binCardRequest;
    }

    public DigitalCreditRequest getDigitalCreditRequest() {
        return digitalCreditRequest;
    }

    public void setDigitalCreditRequest(DigitalCreditRequest digitalCreditRequest) {
        this.digitalCreditRequest = digitalCreditRequest;
    }

    public PaymentsBankRequest getPaymentsBankRequest() {
        return paymentsBankRequest;
    }

    public void setPaymentsBankRequest(PaymentsBankRequest paymentsBankRequest) {
        this.paymentsBankRequest = paymentsBankRequest;
    }

    public UPIPushRequest getUpiPushRequest() {
        return upiPushRequest;
    }

    public void setUpiPushRequest(UPIPushRequest upiPushRequest) {
        this.upiPushRequest = upiPushRequest;
    }

    public ValidationRequest getValidationRequest() {
        return validationRequest;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public EnvInfoRequestBean getEnvInfo() {
        return envInfo;
    }

    public void setIsDirectBankCardPayRequest(boolean directBankCardPayRequest) {
        this.directBankCardPayRequest = directBankCardPayRequest;
    }

    public boolean isDirectBankCardPayRequest() {
        return directBankCardPayRequest;
    }

    public String getTransCreatedtime() {
        return transCreatedtime;
    }

    public void setTransCreatedtime(String transCreatedtime) {
        this.transCreatedtime = transCreatedtime;
    }

    public boolean isFundOrder() {
        return isFundOrder;
    }

    public void setFundOrder(boolean isFundOrder) {
        this.isFundOrder = isFundOrder;
    }

    public void setCardRequest(CardRequest cardRequest) {
        this.cardRequest = cardRequest;
    }

    public void setPaymentRequest(PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    public boolean isCardTokenRequired() {
        return cardTokenRequired;
    }

    public void setCardTokenRequired(boolean cardTokenRequired) {
        this.cardTokenRequired = cardTokenRequired;
    }

    public boolean isOnTheFlyKYCRequired() {
        return OnTheFlyKYCRequired;
    }

    public boolean isCartValidationRequired() {
        return cartValidationRequired;
    }

    public void setCartValidationRequired(boolean cartValidationRequired) {
        this.cartValidationRequired = cartValidationRequired;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public boolean isFromAoaMerchant() {
        return fromAoaMerchant;
    }

    public void setFromAoaMerchant(boolean fromAoaMerchant) {
        this.fromAoaMerchant = fromAoaMerchant;
    }

    public void setAcquirementId(String acquirementId) {
        this.acquirementId = acquirementId;
    }

    public String getDummyAlipayMid() {
        return dummyAlipayMid;
    }

    public void setDummyAlipayMid(String dummyAlipayMid) {
        this.dummyAlipayMid = dummyAlipayMid;
    }

    public static class CashierRequestBuilder {
        private String acquirementId;
        private String requestId;
        private CashierMerchant cashierMerchant;
        private CardRequest cardRequest;
        private PaymentRequest paymentRequest;
        private CashierWorkflow cashierWorkflow;
        private LooperRequest looperRequest;
        private boolean isProcessed;
        private ValidationRequest validationRequest;
        private BinCardRequest binCardRequest;
        private boolean directBankCardPayRequest;
        private EnvInfoRequestBean envInfo;
        private boolean isFundOrder;
        private DigitalCreditRequest digitalCreditRequest;
        private PaymentsBankRequest paymentsBankRequest;
        private boolean linkBasedPaymentRequest;
        private String merchantName;
        private String merchantImage;
        private UPIPushRequest upiPushRequest;
        private boolean OnTheFlyKYCRequired;
        private boolean fromAoaMerchant;
        private Routes route;
        private String paytmMerchantId;

        /**
         * @param acquirementId
         * @param requestId
         * @param cashierWorkflow
         * @throws CashierCheckedException
         */
        public CashierRequestBuilder(final String acquirementId, final String requestId,
                final CashierWorkflow cashierWorkflow) throws CashierCheckedException {
            BeanParameterValidator.validateInputStringParam(acquirementId, "acquirementId");
            this.acquirementId = acquirementId;

            BeanParameterValidator.validateInputStringParam(requestId, "requestId");
            this.requestId = requestId;

            BeanParameterValidator.validateInputObjectParam(cashierWorkflow, "cashierWorkflow");
            this.cashierWorkflow = cashierWorkflow;
        }

        /**
         * @param acquirementId
         * @param isProcessed
         */
        public CashierRequestBuilder(String acquirementId, boolean isProcessed) {
            this.acquirementId = acquirementId;
            this.isProcessed = isProcessed;
        }

        /**
         * @param merchantId
         * @param cashierWorkflow
         * @throws CashierCheckedException
         */
        public CashierRequestBuilder setCashierMerchant(final CashierMerchant cashierMerchant)
                throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(cashierMerchant, "cashierMerchant");
            this.cashierMerchant = cashierMerchant;

            return this;
        }

        /**
         * @param cardRequest
         * @return
         * @throws CashierCheckedException
         */
        public CashierRequestBuilder setCardRequest(CardRequest cardRequest) throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(cardRequest, "cardRequest");

            this.cardRequest = cardRequest;
            return this;
        }

        public CashierRequestBuilder setLinkBasedPaymentRequest(boolean linkBasedPaymentRequest) {
            this.linkBasedPaymentRequest = linkBasedPaymentRequest;
            return this;
        }

        public CashierRequestBuilder setMerchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public CashierRequestBuilder setMerchantImage(String merchantImage) {
            this.merchantImage = merchantImage;
            return this;
        }

        public CashierRequestBuilder setBinCardRequest(BinCardRequest binCardRequest) throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(binCardRequest, "binCardRequest");

            this.binCardRequest = binCardRequest;
            return this;
        }

        /**
         * @param paymentRequest
         * @return
         * @throws CashierCheckedException
         */
        public CashierRequestBuilder setPaymentRequest(PaymentRequest paymentRequest) throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(paymentRequest, "paymentRequest");

            GenericBeanValidator<PaymentRequest> bean = new GenericBeanValidator<>(paymentRequest);

            if (!bean.validate()) {
                throw new CashierCheckedException("Validation failed for payment request data");
            }

            this.paymentRequest = paymentRequest;
            return this;
        }

        /**
         * @param looperRequest
         * @return
         * @throws CashierCheckedException
         */
        public CashierRequestBuilder setLooperRequest(LooperRequest looperRequest) throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(looperRequest, "looperRequest");

            this.looperRequest = looperRequest;
            return this;
        }

        public CashierRequestBuilder setValidationRequest(ValidationRequest validationRequest) {

            this.validationRequest = validationRequest;
            return this;
        }

        /**
         * @param isProcessed
         *            the isProcessed to set
         */
        public void setProcessed(boolean isProcessed) {
            this.isProcessed = isProcessed;
        }

        /**
         * @return
         */
        public CashierRequest build() {
            return new CashierRequest(this);
        }

        public void setDirectBankCardPayRequest(boolean directBankCardPayRequest) {
            this.directBankCardPayRequest = directBankCardPayRequest;
        }

        public CashierRequestBuilder setEnvInfo(EnvInfoRequestBean envInfo) throws CashierCheckedException {
            BeanParameterValidator.validateInputObjectParam(envInfo, "envInfo");
            this.envInfo = envInfo;
            return this;
        }

        public CashierRequestBuilder setDigitalCreditRequest(DigitalCreditRequest digitalCreditRequest) {
            this.digitalCreditRequest = digitalCreditRequest;
            return this;
        }

        public CashierRequestBuilder setPaymentsBankRequest(PaymentsBankRequest paymentsBankRequest) {
            this.paymentsBankRequest = paymentsBankRequest;
            return this;
        }

        public CashierRequestBuilder setUpiPushRequest(UPIPushRequest upiPushRequest) {
            this.upiPushRequest = upiPushRequest;
            return this;
        }

        public CashierRequestBuilder OnTheFlyKYCRequired(boolean OnTheFlyKYCRequired) {
            this.OnTheFlyKYCRequired = OnTheFlyKYCRequired;
            return this;
        }

        public CashierRequestBuilder setFromAoaMerchant(boolean fromAoaMerchant) {
            this.fromAoaMerchant = fromAoaMerchant;
            return this;
        }

        public CashierRequestBuilder setRoute(Routes route) {
            this.route = route;
            return this;
        }

        public CashierRequestBuilder setPaytmMerchantId(String paytmMerchantId) {
            this.paytmMerchantId = paytmMerchantId;
            return this;
        }
    }

    public void setLooperRequest(LooperRequest looperRequest) {
        this.looperRequest = looperRequest;
    }

    public Routes getRoute() {
        return route;
    }

    public void setRoute(Routes route) {
        this.route = route;
    }

    public String getPaytmMerchantId() {
        return paytmMerchantId;
    }

    public void setPaytmMerchantId(String paytmMerchantId) {
        this.paytmMerchantId = paytmMerchantId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CashierRequest [cashierMerchant=").append(cashierMerchant).append(", cardRequest=")
                .append(cardRequest).append(", paymentRequest=").append(paymentRequest).append(", cashierWorkflow=")
                .append(cashierWorkflow).append(", internalCardRequest=").append(internalCardRequest)
                .append(", isProcessed=").append(isProcessed).append(", validationRequest=").append(validationRequest)
                .append(", binCardRequest=").append(binCardRequest).append(", directBankCardPayRequest=")
                .append(directBankCardPayRequest).append(", productCode=").append(productCode)
                .append(", transCreatedtime=").append(transCreatedtime).append(", envInfo=").append(envInfo)
                .append(", isFundOrder=").append(isFundOrder).append(", digitalCreditRequest=")
                .append(digitalCreditRequest).append(", paymentsBankRequest=").append(paymentsBankRequest).append("]")
                .append(", linkBasedPaymentRequest=").append(linkBasedPaymentRequest).append("]")
                .append(", merchantName=").append(merchantName).append("]").append(", merchantImage=")
                .append(merchantImage).append(", upiPushrequest=").append(upiPushRequest).append("]")
                .append(", fromAoaMerchant=").append(fromAoaMerchant).append(", Route=").append(route);
        return builder.toString();
    }

}