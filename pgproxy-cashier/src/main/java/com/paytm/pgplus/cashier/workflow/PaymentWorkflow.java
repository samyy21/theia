package com.paytm.pgplus.cashier.workflow;

import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import org.apache.commons.lang3.StringUtils;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.looper.model.CashierTransactionStatus;
import com.paytm.pgplus.cashier.looper.model.LooperRequest;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.models.SeamlessBankCardPayRequest;
import com.paytm.pgplus.cashier.validator.ValidationHelper;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.slf4j.MDC;

public class PaymentWorkflow extends AbstractCashierWorkflow {

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_VALIDATION)
    public ValidationHelper validate(ValidationHelper validationHelper) {
        return validationHelper;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_INIT)
    public InitiatePaymentResponse initiatePayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {
        validateInitiatePaymentRequest(cashierRequest);

        InitiatePaymentResponse initiatePaymentResponse = new InitiatePaymentResponse();

        String cashierRequestId = submitPayment(cashierRequest);

        if (StringUtils.isEmpty(cashierRequestId)) {
            throw new CashierCheckedException("Process failed : cashier request id received null");
        }

        // populate response object
        initiatePaymentResponse.setCashierRequestId(cashierRequestId);

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

        return doPaymentResponse;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public void doPaymentAsync(CashierRequest cashierRequest, GenericCallBack callBack) throws CashierCheckedException {

        validateDoPaymentRequest(cashierRequest);

        LooperRequest looperRequest = cashierRequest.getLooperRequest();

        String acquirementId;

        if (StringUtils.isNotBlank(cashierRequest.getLooperRequest().getAcquirementId())) {
            acquirementId = cashierRequest.getLooperRequest().getAcquirementId();
        } else {
            acquirementId = cashierRequest.getAcquirementId();
        }

        cashierRequest.setFundOrder(false);

        // check payment status ~ looper call
        checkPaymentStatusAsync(acquirementId,
                cashierRequest,
                paymentResponse -> {
                    Object response;
                    if (null == paymentResponse) {
                        response = new CashierCheckedException("process failed : payment status received null");
                    } else if (paymentResponse instanceof Exception) {
                        response = paymentResponse;
                    } else {
                        CashierPaymentStatus paymentStatus = (CashierPaymentStatus) paymentResponse;
                        DoPaymentResponse doPaymentResponse = new DoPaymentResponse();
                        doPaymentResponse.setPaymentStatus(paymentStatus);

                        boolean paymentSuccessFlag = false;
                        if (PaymentStatus.SUCCESS.name().equalsIgnoreCase(paymentStatus.getPaymentStatusValue())) {
                            paymentSuccessFlag = true;
                        }

                        // check transaction status ~ looper call
                CashierTransactionStatus transactionStatus = null;
                try {
                    transactionStatus = checkTransactionStatus(paymentSuccessFlag,
                            getMerchantId(paymentStatus, looperRequest), looperRequest.getAcquirementId(),
                            looperRequest.getNeedFullInfo(), cashierRequest.isFromAoaMerchant(), null);

                    if (null == transactionStatus) {
                        StatisticsLogger.logForXflush(MDC.get("MID"), "Alipay", null, "response",
                                "transaction status received null", null);
                        throw new CashierCheckedException("process failed : transaction status received null");
                    }
                    doPaymentResponse.setTransactionStatus(transactionStatus);
                    response = doPaymentResponse;
                } catch (CashierCheckedException e) {
                    response = e;
                }
            }
            callBack.processResponse(response);
        });

    }

    public String getMerchantId(CashierPaymentStatus paymentStatus, LooperRequest looperRequest) {
        if ((paymentStatus != null) && (paymentStatus.getExtendInfo() != null)) {
            if (null != paymentStatus.getExtendInfo().get(CashierConstant.DUMMY_ALIPAY_MID)) {
                return paymentStatus.getExtendInfo().get(CashierConstant.DUMMY_ALIPAY_MID);

            }
            return paymentStatus.getExtendInfo().get(CashierConstant.EXT_INFO_ALIPAY_MERCH_ID);
        }
        return looperRequest.getMerchantId();
    }

    public void validateDoPaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("process failed :cashierRequest can not be null");
        }

        if (null == cashierRequest.getLooperRequest()) {
            throw new CashierCheckedException("process failed :looper request can not be null");
        }
    }

    private void validateInitiatePaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("process failed :cashierRequest can not be null");
        }
    }

    @Override
    public InitiatePaymentResponse seamlessBankCardPayment(SeamlessBankCardPayRequest seamlessBankCardPayRequest)
            throws PaytmValidationException, CashierCheckedException {
        return null;
    }

    @Override
    public InitiatePaymentResponse riskConsult(CashierRequest cashierRequest, String userId)
            throws PaytmValidationException, CashierCheckedException {
        return null;
    }

}
