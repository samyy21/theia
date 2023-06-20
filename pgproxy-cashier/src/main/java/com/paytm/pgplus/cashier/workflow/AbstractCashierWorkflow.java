package com.paytm.pgplus.cashier.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.constant.CashierWorkflow;
import com.paytm.pgplus.cashier.looper.model.CashierLopperMapper;
import com.paytm.pgplus.cashier.models.*;
import com.paytm.pgplus.cashier.payoption.PayBillOptions;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequest;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequestBody;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.cart.IPaymentValidationService;
import com.paytm.pgplus.facade.cart.model.*;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.fund.models.request.CloseFundRequest;
import com.paytm.pgplus.facade.fund.models.request.CloseFundRequestBody;
import com.paytm.pgplus.facade.fund.services.ITopup;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequest;
import com.paytm.pgplus.facade.payment.models.request.PayResultQueryRequestBody;
import com.paytm.pgplus.facade.payment.models.response.PayResultQueryResponse;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.paytm.pgplus.cashier.cache.service.ICashierCacheService;
import com.paytm.pgplus.cashier.cachecard.service.ICashierCardService;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.exception.ValidationException;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.looper.service.ILooperService;
import com.paytm.pgplus.cashier.pay.service.ICashierPayService;
import com.paytm.pgplus.cashier.util.SavedCardUtilService;
import com.paytm.pgplus.cashier.validator.ValidationHelper;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.enums.PayMethod;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;

/**
 * Template class for all Cashier workflows
 *
 * @author Lalit Mehra, Amit Dubey
 * @since March 7, 2016
 */
public abstract class AbstractCashierWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCashierWorkflow.class);

    @Autowired
    ICashierCardService cashierCardServiceImpl;

    @Autowired
    ICashierPayService cashierPayServiceImpl;

    @Autowired
    ILooperService looperServiceImpl;

    @Autowired
    ICashierCacheService cashierCacheServiceImpl;

    @Autowired
    IFacadeService facadeServiceImpl;

    @Autowired
    ITopup topupImpl;

    @Autowired
    IAcquiringOrder acquiringOrder;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    @Qualifier("savedCardUtilService")
    SavedCardUtilService savedCardUtilService;

    @Autowired
    @Qualifier("paymentValidationService")
    IPaymentValidationService paymentValidationService;

    public abstract ValidationHelper validate(ValidationHelper validationHelper) throws ValidationException;

    public String getCacheCardToken(final CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {
        /**
         * If cache card api already generated the token id and simply return it
         */
        if (!StringUtils.isBlank(cashierRequest.getPaymentRequest().getPayBillOptions().getCardCacheToken())) {
            return cashierRequest.getPaymentRequest().getPayBillOptions().getCardCacheToken();
        }
        CacheCardResponseBody cacheCardResponse = getCacheCardResponse(cashierRequest);
        return cacheCardResponse.getTokenId();
    }

    public CacheCardResponseBody getCacheCardResponse(final CashierRequest cashierRequest)
            throws CashierCheckedException, PaytmValidationException {

        if (null == cashierRequest.getCardRequest()) {
            throw new CashierInvalidParameterException("Process failed : card request can not be null");
        }

        if (null == cashierRequest.getPaymentRequest()) {
            throw new CashierInvalidParameterException("Process failed : payment request can not be null");
        }

        InstNetworkType instNetworkType = getInstnetworkType(cashierRequest);

        CacheCardResponseBody cacheCardResponse = cashierCardServiceImpl.submitCacheCard(cashierRequest,
                instNetworkType);
        validatePaymentRequestFromCart(cashierRequest, cacheCardResponse);
        return cacheCardResponse;
    }

    // for samsung and zero cost EMI
    private void validatePaymentRequestFromCart(CashierRequest cashierRequest, CacheCardResponseBody cacheCardResponse)
            throws PaytmValidationException {
        PayBillOptions payBillOptions = cashierRequest.getPaymentRequest().getPayBillOptions();
        if (cashierRequest.isCartValidationRequired()) {
            if (StringUtils.isEmpty(cashierRequest.getSsoToken())) {
                LOGGER.info("sso token is empty in request with isCartValidationRequired true");
                throw new PaytmValidationException(PaytmValidationExceptionType.PROMO_VALIDATION_ERROR);
            }
            if (cacheCardResponse != null && StringUtils.isNotBlank(cacheCardResponse.getCardIndexNo())
                    && payBillOptions != null && payBillOptions.getExtendInfo() != null
                    && StringUtils.isNotBlank(payBillOptions.getExtendInfo().get("merchantTransId"))) {
                PaymentValidationRequest pvr = getPaymentValidationRequest(cashierRequest, cacheCardResponse,
                        payBillOptions);
                try {
                    // LOGGER.info("validating request from cart");
                    PaymentValidationResponse paymentValidationResponse = paymentValidationService.validate(pvr,
                            cashierRequest.getSsoToken());
                    if (paymentValidationResponse == null || paymentValidationResponse.getStatus() != Status.success) {
                        if (StringUtils.isEmpty(paymentValidationResponse.getMessage())) {
                            throw new PaytmValidationException(PaytmValidationExceptionType.PROMO_VALIDATION_ERROR);
                        }
                        throw new PaytmValidationException(paymentValidationResponse.getCode(),
                                paymentValidationResponse.getMessage());
                    }
                    LOGGER.info("validation from cart successful");
                } catch (FacadeCheckedException e) {
                    LOGGER.info("FacadeCheckedException occured : ");
                    throw new PaytmValidationException(PaytmValidationExceptionType.PROMO_VALIDATION_ERROR);
                }
            }
        }
    }

    private PaymentValidationRequest getPaymentValidationRequest(CashierRequest cashierRequest,
            CacheCardResponseBody cacheCardResponse, PayBillOptions payBillOptions) {
        PaymentDetail paymentDetail = new PaymentDetail(Method.fromCardType(cashierRequest.getBinCardRequest()
                .getCardType().getValue()), cacheCardResponse.getCardIndexNo());
        List<PaymentDetail> paymentDetails = new ArrayList<>();
        paymentDetails.add(paymentDetail);
        return new PaymentValidationRequest(payBillOptions.getExtendInfo().get("merchantTransId"), paymentDetails);
    }

    public String submitPayment(CashierRequest cashierRequest) throws CashierCheckedException, PaytmValidationException {
        if (null == cashierRequest) {
            throw new CashierInvalidParameterException("Process failed : cashier request can not be null");
        }

        return cashierPayServiceImpl.submitPay(cashierRequest);
    }

    public void cacheUserInput(CashierRequest cashierRequest, String cashierRequestId) throws CashierCheckedException {
        if ((null != cashierRequest.getInternalCardRequest()) && (null != cashierRequest.getPaymentRequest())) {
            if (!cashierRequest.getInternalCardRequest().isSavedDataRequest()
                    && cashierRequest.getPaymentRequest().getPayBillOptions().isSaveChannelInfoAfterPay()
                    && checkIfUniqueVPA(cashierRequest)) {
                savedCardUtilService.cacheCardData(cashierRequest);
            }
        }

        // Cache the cashier request data for retry payment
        cashierCacheServiceImpl.cacheCashierRequest(cashierRequestId, cashierRequest);
    }

    // Store only Unique VPA.If already exist, then ignore
    public boolean checkIfUniqueVPA(CashierRequest cashierRequest) {
        CompleteCardRequest completeCardRequest = cashierRequest.getInternalCardRequest();
        CardRequest cardRequest = cashierRequest.getCardRequest();
        if ((CashierWorkflow.UPI.getValue()).equals(completeCardRequest.getInstNetworkType().getNetworkType())
                && cardRequest != null && completeCardRequest != null
                && !(cardRequest.getMerchantViewSavedCardsList().isEmpty())
                && completeCardRequest.getVpaCardRequest() != null) {
            List<SavedCardInfo> merchantViewSavedCardsList = cashierRequest.getCardRequest()
                    .getMerchantViewSavedCardsList();
            String vpa = completeCardRequest.getVpaCardRequest().getVpa();
            for (SavedCardInfo savedCardInfo : merchantViewSavedCardsList) {
                if (savedCardInfo.getCardNumber().equalsIgnoreCase(vpa))
                    return false;
            }
        }
        return true;
    }

    public CashierPaymentStatus fetchBankForm(String cashierRequestId) throws CashierCheckedException {
        if (StringUtils.isEmpty(cashierRequestId)) {
            throw new CashierInvalidParameterException("Process failed : cashier request id can not be null");
        }

        return looperServiceImpl.fetchBankForm(cashierRequestId);
    }

    public CashierPaymentStatus checkPaymentStatus(String acquirementId, CashierRequest cashierRequest)
            throws CashierCheckedException {
        if (StringUtils.isBlank(cashierRequest.getLooperRequest().getCashierRequestId())) {
            throw new CashierInvalidParameterException("Process failed : cashier request id can not be null");
        }

        if (StringUtils.isBlank(acquirementId)) {
            throw new CashierInvalidParameterException("Process failed : acquirement id can not be null");
        }

        // Disabling the code for higher exception count
        /*
         * if (cashierRequest.getCashierWorkflow() == CashierWorkflow.UPI ||
         * cashierRequest.getCashierWorkflow() == CashierWorkflow.ADD_MONEY_UPI)
         * { return fetchPaymentStatus(acquirementId, cashierRequest); }
         */
        return looperServiceImpl.fetchPaymentStatus(acquirementId, cashierRequest);
    }

    public void checkPaymentStatusAsync(String acquirementId, CashierRequest cashierRequest, GenericCallBack callBack)
            throws CashierCheckedException {
        if (StringUtils.isBlank(cashierRequest.getLooperRequest().getCashierRequestId())) {
            throw new CashierInvalidParameterException("Process failed : cashier request id can not be null");
        }

        if (StringUtils.isBlank(acquirementId)) {
            throw new CashierInvalidParameterException("Process failed : acquirement id can not be null");
        }

        // Disabling the code for higher exception count
        /*
         * if (cashierRequest.getCashierWorkflow() == CashierWorkflow.UPI ||
         * cashierRequest.getCashierWorkflow() == CashierWorkflow.ADD_MONEY_UPI)
         * { return fetchPaymentStatus(acquirementId, cashierRequest); }
         */
        looperServiceImpl.fetchPaymentStatusAsync(acquirementId, cashierRequest, callBack);
    }

    private void validatePayResultQueryResponse(PayResultQueryResponse payResultQueryResponse)
            throws CashierCheckedException {
        if (null == payResultQueryResponse) {
            throw new CashierCheckedException("Process failed : payResultQueryResponse received as null");
        }

        if (null == payResultQueryResponse.getBody()) {
            throw new CashierCheckedException("Process failed : payResultQueryResponse body received as null");
        }

        if (null == payResultQueryResponse.getBody().getResultInfo()) {
            throw new CashierCheckedException(
                    "Process failed : payResultQueryResponse body Result Info received as null");
        }
    }

    public CashierPaymentStatus fetchPaymentStatus(String acquirementId, CashierRequest cashierRequest)
            throws CashierCheckedException {
        try {
            ApiFunctions apiFunction = ApiFunctions.QUERY_PAYRESULT;
            if (cashierRequest.isFromAoaMerchant()) {
                apiFunction = ApiFunctions.AOA_QUERY_PAYRESULT;
            }
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);
            PayResultQueryRequestBody body = new PayResultQueryRequestBody(cashierRequest.getLooperRequest()
                    .getCashierRequestId(), cashierRequest.isFromAoaMerchant());

            PayResultQueryRequest requestData = new PayResultQueryRequest(head, body);
            PayResultQueryResponse payResultQueryResponse = facadeServiceImpl.fetchPayResultQueryResponse(requestData);

            validatePayResultQueryResponse(payResultQueryResponse);

            ResultInfo resultInfo = payResultQueryResponse.getBody().getResultInfo();

            if (CashierConstant.SUCCESS_RESULT_CODE.equalsIgnoreCase(resultInfo.getResultCode())) {
                if (PaymentStatus.FAIL == payResultQueryResponse.getBody().getPaymentStatus()) {
                    CashierResponseCodeDetails cashierResponseCodeDetails = cashierUtilService
                            .getMerchantResponseCode(payResultQueryResponse.getBody().getInstErrorCode());

                    if (cashierResponseCodeDetails.isRetry()) {
                        return CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse.getBody(),
                                cashierResponseCodeDetails);
                    }
                    if (cashierRequest.isFundOrder()) {
                        closeFundOrder(cashierRequest.getLooperRequest().getFundOrderId(), cashierRequest.getEnvInfo());
                    } else {
                        closeOrder(cashierRequest.getPaytmMerchantId(), acquirementId,
                                cashierRequest.isFromAoaMerchant());
                    }
                    CashierPaymentStatus cashierPaymentStatus = CashierLopperMapper.buildCashierPaymentStatus(
                            payResultQueryResponse.getBody(), cashierResponseCodeDetails);

                    return cashierPaymentStatus;
                }

                return CashierLopperMapper.buildCashierPaymentStatus(payResultQueryResponse.getBody());
            } else {
                String resultInfoStr = JsonMapper.mapObjectToJson(resultInfo);
                throw new CashierCheckedException("Result Info : received : " + resultInfoStr);
            }

        } catch (FacadeCheckedException e) {
            throw new CashierCheckedException(
                    "Exception occurred while working with facade for fetching payment status", e);
        }
    }

    private void closeOrder(String merchantId, String acquirementId, boolean fromAoaMerchant) {
        String closeReason = "Payment failed for the acquirementId : " + acquirementId;

        try {
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CLOSE_ORDER);
            CloseRequestBody body = new CloseRequestBody(acquirementId, merchantId, closeReason, fromAoaMerchant);
            body.setRoute(Routes.PG2);
            body.setPaytmMerchantId(merchantId);
            CloseRequest closeRequest = new CloseRequest(head, body);
            acquiringOrder.closeOrder(closeRequest);
        } catch (Exception e) {
            LOGGER.error("Unable to close the order for the acquirementId : {}", acquirementId);
        }
    }

    private void closeFundOrder(String fundOrderId, EnvInfoRequestBean envInfo) {
        try {
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(ApiFunctions.CLOSE_FUND_ORDER);
            CloseFundRequestBody body = new CloseFundRequestBody(fundOrderId, envInfo);
            body.setRoute(Routes.PG2);
            CloseFundRequest closeRequest = new CloseFundRequest(head, body);
            topupImpl.closeFundOrder(closeRequest);
        } catch (Exception e) {
            LOGGER.error("Unable to close the order for the fundOrderId : {}", fundOrderId);
        }
    }

    public CashierTransactionStatus checkTransactionStatus(boolean paymentSuccessFlag, final String merchantId,
            final String acquirementId, boolean needFullInfo, boolean isFromAoaMerchant, CashierRequest cashierRequest)
            throws CashierCheckedException {
        if (StringUtils.isBlank(merchantId)) {
            throw new CashierInvalidParameterException("Process failed : cashier request id can not be null");
        }

        if (StringUtils.isBlank(acquirementId)) {
            throw new CashierInvalidParameterException("Process failed : acquirementId can not be null");
        }

        if (paymentSuccessFlag) {
            return looperServiceImpl.fetchTrasactionStatusForAcquirementId(merchantId, acquirementId, needFullInfo,
                    isFromAoaMerchant, cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());

        } else {
            return facadeServiceImpl.queryByAcquirementId(merchantId, acquirementId, needFullInfo, isFromAoaMerchant,
                    cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
        }
    }

    public CashierFundOrderStatus checkFundOrderStatus(String fundOrderId, String paytmMerchnatId, Routes route)
            throws CashierCheckedException {
        if (StringUtils.isBlank(fundOrderId)) {
            throw new CashierInvalidParameterException("Process failed : fundOrderId can not be null");
        }

        return looperServiceImpl.fetchFundOrderStatus(fundOrderId, paytmMerchnatId, route);
    }

    /**
     * Implementations provide concrete definition for payment initiation
     * process.<br/>
     * Refer CashierWorkflow subclasses
     *
     * @return
     * @throws CashierCheckedException
     * @throws PaytmValidationException
     * @throws ValidationException
     */
    public abstract InitiatePaymentResponse initiatePayment(CashierRequest payload) throws CashierCheckedException,
            PaytmValidationException;

    /**
     * Implementations provide concrete definition for payment completion
     * process.<br/>
     * Refer CashierWorkflow subclasses
     *
     * @return
     * @throws CashierCheckedException
     * @throws PaytmValidationException
     * @throws ValidationException
     */

    public abstract DoPaymentResponse doPayment(CashierRequest payload) throws CashierCheckedException,
            PaytmValidationException;

    public abstract void doPaymentAsync(CashierRequest payload, GenericCallBack callBack)
            throws CashierCheckedException, PaytmValidationException;

    /**
     * @param cashierRequest
     * @return InstNetworkType
     */
    private InstNetworkType getInstnetworkType(CashierRequest cashierRequest) {

        Set<PayMethod> payMethods = cashierRequest.getPaymentRequest().getPayBillOptions().getPayOptions().keySet();

        for (PayMethod payMethod : payMethods) {
            switch (payMethod) {
            case CREDIT_CARD:
            case DEBIT_CARD:
            case EMI:
                return InstNetworkType.ISOCARD;
            case IMPS:
                return InstNetworkType.IMPS;
            case ATM:
            case BALANCE:
            case BANK_EXPRESS:
            case COD:
            case HYBRID_PAYMENT:
            case MP_COD:
            case NB:
            case NET_BANKING:
            case UPI:
            case WITHDRAW:
            case PAYTM_DIGITAL_CREDIT:
            default:
                break;
            }
        }
        return null;
    }

    public abstract InitiatePaymentResponse seamlessBankCardPayment(
            SeamlessBankCardPayRequest seamlessBankCardPayRequest) throws PaytmValidationException,
            CashierCheckedException;

    public abstract InitiatePaymentResponse riskConsult(CashierRequest cashierRequest, String userId)
            throws PaytmValidationException, CashierCheckedException;
}