package com.paytm.pgplus.cashier.workflow;

import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.VPACardRequest;
import com.paytm.pgplus.cashier.looper.model.LooperRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.cashier.util.CommonServiceUtil;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.facade.enums.InstNetworkType;

import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.facade.paymentrouter.enums.Routes;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.service.IUPICollectSyncService;
import com.paytm.pgplus.cashier.validator.ValidationHelper;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequest;
import com.paytm.pgplus.facade.acquiring.models.request.CloseRequestBody;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.common.model.AlipayExternalRequestHeader;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.cashier.constant.CashierConstant.STOP_FETCHING_ACQ_STATUS_IN_UPI_COLLECT_POLLING;

/**
 * Created by prashant on 1/13/17.
 */
@Component("UPIWorkflow")
public class UPIWorkFlow extends PaymentWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPIWorkFlow.class);

    @Autowired
    IAcquiringOrder acquiringOrder;

    @Autowired
    CashierUtilService cashierUtilService;

    @Autowired
    IUPICollectSyncService upiCollectSycnServiceImpl;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_VALIDATION)
    public ValidationHelper validate(ValidationHelper validationHelper) {
        return super.validate(validationHelper);
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_INIT)
    public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {
        validateInitiatePaymentRequest(cashierRequest);

        // update last usage for saved Vpa
        if (cashierRequest.getCardRequest().getSavedVpaCardRequest() != null) {
            String savedVpaId = cashierRequest.getCardRequest().getSavedVpaCardRequest().getSavedCardId();
            if (StringUtils.isNotBlank(savedVpaId)) {
                cashierUtilService.updateSavedCardLastUsage(savedVpaId);
            }
        }

        // call AbstractPaymentWorkflow initiatePayment
        InitiatePaymentResponse initiatePaymentResponse = super.initiatePayment(cashierRequest);
        if (null == initiatePaymentResponse) {
            throw new CashierCheckedException("Process failed : initiatePaymentResponse received as null");
        }
        if (cashierRequest.getCardRequest().getVpaCardRequest() != null) {
            VPACardRequest cardRequest = cashierRequest.getCardRequest().getVpaCardRequest();
            cashierRequest.setInternalCardRequest(new CompleteCardRequest(InstNetworkType.UPI));
            cashierRequest.getInternalCardRequest().setVpaCardRequest(cardRequest);
        } else if (cashierRequest.getCardRequest().getSavedVpaCardRequest() != null) {
            VPACardRequest cardRequest = new VPACardRequest(cashierRequest.getCardRequest().getSavedVpaCardRequest()
                    .getVpa());
            cashierRequest.setInternalCardRequest(new CompleteCardRequest(InstNetworkType.UPI));
            cashierRequest.getInternalCardRequest().setVpaCardRequest(cardRequest);
            cashierRequest.getInternalCardRequest().setSavedDataRequest(true);
        }
        cacheUserInput(cashierRequest, initiatePaymentResponse.getCashierRequestId());

        // skip call to fetch bank form for express flow
        if (cashierRequest.getUpiPushRequest() != null
                && !cashierRequest.getUpiPushRequest().isUpiPushExpressSupported()) {
            // fetch bank payment form ~ looper call
            CashierPaymentStatus cashierPaymentStatus = fetchBankForm(initiatePaymentResponse.getCashierRequestId());
            if (null == cashierPaymentStatus) {
                closeOrder(cashierRequest.getPaytmMerchantId(), cashierRequest.getAcquirementId(),
                        cashierRequest.isFromAoaMerchant());
                throw new CashierCheckedException("Process failed : cashierPaymentStatus received as null");
            }
            initiatePaymentResponse.setCashierPaymentStatus(cashierPaymentStatus);
        }

        return initiatePaymentResponse;

    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public DoPaymentResponse doPayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {

        validateDoPaymentRequest(cashierRequest);

        DoPaymentResponse doPaymentResponse = new DoPaymentResponse();

        LooperRequest looperRequest = cashierRequest.getLooperRequest();

        String acquirementId;

        if (StringUtils.isNotBlank(cashierRequest.getLooperRequest().getAcquirementId())) {
            acquirementId = cashierRequest.getLooperRequest().getAcquirementId();
        } else {
            acquirementId = cashierRequest.getAcquirementId();
        }

        cashierRequest.setFundOrder(false);

        // check payment status ~ looper call
        CashierPaymentStatus paymentStatus = checkPaymentStatus(acquirementId, cashierRequest);

        if (null == paymentStatus) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "PaymentWorkflow", "response",
                    "payment status null", null);
            throw new CashierCheckedException("process failed : payment status received null");
        }

        doPaymentResponse.setPaymentStatus(paymentStatus);

        boolean paymentSuccessFlag = false;
        if (PaymentStatus.SUCCESS.name().equalsIgnoreCase(paymentStatus.getPaymentStatusValue())) {
            paymentSuccessFlag = true;
        }

        if (StringUtils.isNotBlank(cashierRequest.getDummyAlipayMid())) {
            looperRequest.setMerchantId(cashierRequest.getDummyAlipayMid());
        }

        // For UPI Collect : In case the payment status is pending we can stop
        // fetching the acquirement status.
        Map<String, Object> context = new HashMap<>();
        context.put("mid", cashierRequest.getPaytmMerchantId());
        boolean isUpiCollectRequest = false;
        if (CollectionUtils.isNotEmpty(paymentStatus.getPayOptions())
                && MapUtils.isNotEmpty(paymentStatus.getPayOptions().get(0).getExtendInfo())) {
            Map<String, String> extendInfo = paymentStatus.getPayOptions().get(0).getExtendInfo();
            if (TheiaConstant.BasicPayOption.UPI.equalsIgnoreCase(extendInfo
                    .get(TheiaConstant.ExtraConstants.PAY_METHOD))
                    && TheiaConstant.BasicPayOption.UPI.equalsIgnoreCase(extendInfo.get("payOption")))
                isUpiCollectRequest = true;
        }

        if (isUpiCollectRequest
                && (BooleanUtils.isFalse(PaymentStatus.SUCCESS.name().equalsIgnoreCase(
                        paymentStatus.getPaymentStatusValue())
                        || PaymentStatus.FAIL.name().equalsIgnoreCase(paymentStatus.getPaymentStatusValue())))
                && iPgpFf4jClient.checkWithdefault(STOP_FETCHING_ACQ_STATUS_IN_UPI_COLLECT_POLLING, context, false)) {
            doPaymentResponse.setTransactionStatus(null);
        } else {
            // check transaction status ~ looper call
            CashierTransactionStatus transactionStatus = checkTransactionStatus(paymentSuccessFlag,
                    getMerchantId(paymentStatus, looperRequest), looperRequest.getAcquirementId(),
                    looperRequest.getNeedFullInfo(), cashierRequest.isFromAoaMerchant(), cashierRequest);

            if (null == transactionStatus) {
                StatisticsLogger.logForXflush(MDC.get("MID"), "Alipay", null, "response",
                        "transaction status received null", null);
                throw new CashierCheckedException("process failed : transaction status received null");
            }
            doPaymentResponse.setTransactionStatus(transactionStatus);
        }
        return doPaymentResponse;
    }

    @Override
    public CashierPaymentStatus checkPaymentStatus(String acquirementId, CashierRequest cashierRequest)
            throws CashierCheckedException {
        if (StringUtils.isBlank(cashierRequest.getLooperRequest().getCashierRequestId())) {
            throw new CashierInvalidParameterException("Process failed : cashier request id can not be null");
        }

        if (StringUtils.isBlank(acquirementId)) {
            throw new CashierInvalidParameterException("Process failed : acquirement id can not be null");
        }
        CashierPaymentStatus cashierPaymentStatus = null;

        // Not consider flag-isDisableLoopingForUPI for UPI-Push and PushExpress
        // Flow
        // TODO: Need to check UPI intent flow
        if (!cashierRequest.isAsyncTxnStatusFlow() && CommonServiceUtil.isDisableLoopingForUPI()
                && !(cashierRequest != null && CommonServiceUtil.isUPIPushFlow(cashierRequest.getUpiPushRequest())))
            cashierPaymentStatus = upiCollectSycnServiceImpl.fetchPaymentStatus(acquirementId, cashierRequest);
        else
            cashierPaymentStatus = looperServiceImpl.fetchPaymentStatus(acquirementId, cashierRequest);
        return cashierPaymentStatus;
    }

    @Override
    public void checkPaymentStatusAsync(String acquirementId, CashierRequest cashierRequest, GenericCallBack callBack)
            throws CashierCheckedException {
        if (StringUtils.isBlank(cashierRequest.getLooperRequest().getCashierRequestId())) {
            throw new CashierInvalidParameterException("Process failed : cashier request id can not be null");
        }

        if (StringUtils.isBlank(acquirementId)) {
            throw new CashierInvalidParameterException("Process failed : acquirement id can not be null");
        }
        CashierPaymentStatus cashierPaymentStatus = null;

        // Not consider flag-isDisableLoopingForUPI for UPI-Push and PushExpress
        // Flow
        // TODO: Need to check UPI intent flow
        if (CommonServiceUtil.isDisableLoopingForUPI()
                && !(cashierRequest != null && CommonServiceUtil.isUPIPushFlow(cashierRequest.getUpiPushRequest()))) {
            cashierPaymentStatus = upiCollectSycnServiceImpl.fetchPaymentStatus(acquirementId, cashierRequest);
            Object response = null;
            try {
                if (cashierPaymentStatus == null) {
                    response = new CashierCheckedException("Cashier payment Status : null");
                } else {
                    response = cashierPaymentStatus;
                }
            } catch (Exception e) {
                callBack.processResponse(e);
            }
            callBack.processResponse(response);
        } else
            looperServiceImpl.fetchPaymentStatusAsync(acquirementId, cashierRequest, callBack);
    }

    private void validateInitiatePaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Mandatory parameter is missing :  cashier request");
        }

        if (null == cashierRequest.getPaymentRequest()) {
            throw new CashierCheckedException("Mandatory parameter is missing : payment request");
        }

        if (null == cashierRequest.getCardRequest()) {
            throw new CashierCheckedException("Mandatory parameter is missing : card request");
        }
    }

    private void closeOrder(String merchantId, String acquirementId, boolean fromAoaMerchant) {
        String closeReason = "Payment failed for the acquirementId : " + acquirementId;

        try {
            ApiFunctions apiFunction = ApiFunctions.CLOSE_ORDER;
            if (fromAoaMerchant) {
                apiFunction = ApiFunctions.AOA_CLOSE_ORDER;
            }
            AlipayExternalRequestHeader head = RequestHeaderGenerator.getHeader(apiFunction);
            CloseRequestBody body = new CloseRequestBody(acquirementId, merchantId, closeReason, fromAoaMerchant);
            body.setRoute(Routes.PG2);
            body.setPaytmMerchantId(merchantId);
            CloseRequest closeRequest = new CloseRequest(head, body);
            acquiringOrder.closeOrder(closeRequest);
        } catch (Exception e) {
            LOGGER.error("Unable to close the order for the acquirementId : {}", acquirementId);
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
            CashierTransactionStatus cashierTransactionStatus = null;
            // Not consider flag-isDisableLoopingForUPI for UPI-Push and
            // PushExpress Flow
            // TODO: Need to check UPI intent flow
            if (CommonServiceUtil.isDisableLoopingForUPI()
                    && !(cashierRequest != null && CommonServiceUtil.isUPIPushFlow(cashierRequest.getUpiPushRequest())))
                cashierTransactionStatus = upiCollectSycnServiceImpl
                        .fetchTrasactionStatus(merchantId, acquirementId, needFullInfo, isFromAoaMerchant,
                                cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
            else
                cashierTransactionStatus = looperServiceImpl.fetchTrasactionStatusForAcquirementId(merchantId,
                        acquirementId, needFullInfo, isFromAoaMerchant, cashierRequest.getPaytmMerchantId(),
                        cashierRequest.getRoute());
            return cashierTransactionStatus;

        } else {
            return facadeServiceImpl.queryByAcquirementId(merchantId, acquirementId, needFullInfo, isFromAoaMerchant,
                    cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
        }
    }
}
