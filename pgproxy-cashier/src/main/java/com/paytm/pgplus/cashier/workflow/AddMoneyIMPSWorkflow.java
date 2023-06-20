package com.paytm.pgplus.cashier.workflow;

import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.enums.TransType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * IMPS Specific Workflow
 *
 * @author Lalit Mehra, Amit Dubey
 *
 * @since March 7, 2016
 */
@Component(value = "AddMoneyIMPSWorkflow")
public class AddMoneyIMPSWorkflow extends CardPaymentWorkflow {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyIMPSWorkflow.class);

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public DoPaymentResponse doPayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {
        validateIMPSDoPaymentRequest(cashierRequest);

        InitiatePaymentResponse initiatePaymentResponse = super.initiatePayment(cashierRequest);

        if (null == initiatePaymentResponse) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyIMPS", "response", "payment status null",
                    null);
            throw new CashierCheckedException("Process failed : initiate payment status received as null");
        }

        DoPaymentResponse doPaymentResponse = new DoPaymentResponse();

        String acquirementId;

        if ((cashierRequest.getLooperRequest() != null)
                && StringUtils.isNotBlank(cashierRequest.getLooperRequest().getAcquirementId())) {
            acquirementId = cashierRequest.getLooperRequest().getAcquirementId();
        } else {
            acquirementId = cashierRequest.getAcquirementId();
        }

        cashierRequest.setFundOrder(true);

        // check payment status ~ looper call
        CashierPaymentStatus paymentStatus = checkPaymentStatus(acquirementId, cashierRequest);

        if (null == paymentStatus) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyIMPS", "response", "payment status null",
                    null);
            throw new CashierCheckedException("Process failed : payment status received as null");
        }
        // populate payment status object
        doPaymentResponse.setPaymentStatus(paymentStatus);

        // check fund order status ~ looper call
        CashierFundOrderStatus fundOrderStatus = checkFundOrderStatus(cashierRequest.getPaymentRequest().getTransId(),
                cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());

        if (null == fundOrderStatus) {
            throw new CashierCheckedException("Process failed : fund order status received as null");
        }

        // populate fund order status object
        doPaymentResponse.setFundOrderStatus(fundOrderStatus);

        return doPaymentResponse;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public void doPaymentAsync(CashierRequest cashierRequest, GenericCallBack callBack) throws CashierCheckedException {
        // validate the request
        validateIMPSDoPaymentRequest(cashierRequest);

        InitiatePaymentResponse initiatePaymentResponse = null;
        try {
            initiatePaymentResponse = super.initiatePayment(cashierRequest);
        } catch (PaytmValidationException e) {
            LOGGER.error("Initiate Payment Failed with Reason : {} ", e);
            throw new CashierCheckedException("Process failed : Initiate Payment Failed");
        }

        if (null == initiatePaymentResponse) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyIMPS", "response", "payment status null",
                    null);
            throw new CashierCheckedException("Process failed : initiate payment status received as null");
        }

        DoPaymentResponse doPaymentResponse = new DoPaymentResponse();

        String acquirementId;

        if (StringUtils.isNotBlank(cashierRequest.getLooperRequest().getAcquirementId())) {
            acquirementId = cashierRequest.getLooperRequest().getAcquirementId();
        } else {
            acquirementId = cashierRequest.getAcquirementId();
        }
        cashierRequest.setFundOrder(true);

        // check payment status ~ looper call
        checkPaymentStatusAsync(
                acquirementId,
                cashierRequest,
                cashierResponse -> {
                    Object response;
                    if (null == cashierResponse) {

                        StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyIMPS", "response",
                                "payment status null", null);
                        response = new CashierCheckedException("process failed : payment status received null");

                    } else if (cashierResponse instanceof Exception) {

                        response = cashierResponse;

                    } else {

                        // populate payment status object
                        CashierPaymentStatus paymentStatus = (CashierPaymentStatus) cashierResponse;
                        doPaymentResponse.setPaymentStatus(paymentStatus);

                        // check fund order status ~ looper call
                        CashierFundOrderStatus fundOrderStatus = null;
                        try {
                            fundOrderStatus = checkFundOrderStatus(cashierRequest.getPaymentRequest().getTransId(),
                                    cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
                            if (null == fundOrderStatus) {
                                StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyIMPS", "response",
                                        "fund Order status null", null);
                                throw new CashierCheckedException("process failed : transaction status received null");
                            }
                            // populate fund order status object
                            doPaymentResponse.setFundOrderStatus(fundOrderStatus);
                            response = doPaymentResponse;
                        } catch (Exception e) {
                            response = new CashierCheckedException("Process failed : fund order status received null");
                        }
                    }
                    LOGGER.info("Request completed : AddMoneyIMPS : request id {} ", cashierRequest.getRequestId());
                    callBack.processResponse(response);
                });
    }

    private void validateIMPSDoPaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            LOGGER.error("Manadatory parameter is missing : AddMoneyIMPS : cashier request ");
            throw new CashierCheckedException("Manadatory parameter is missing :  cashier request");
        }

        if (null == cashierRequest.getPaymentRequest()) {
            LOGGER.error("Manadatory parameter is missing : AddMoneyIMPS : payment request ");
            throw new CashierCheckedException("Manadatory parameter is missing :  payment request");
        }

        if (TransType.TOP_UP != cashierRequest.getPaymentRequest().getTransType()) {
            LOGGER.error("Process failed : Only TOP_UP as TransType allowed in AddMoneyIMPS");
            throw new CashierCheckedException("Process failed : Only TOP_UP as TransType allowed in AddMoneyIMPS");
        }

        if (StringUtils.isEmpty(cashierRequest.getPaymentRequest().getPayerUserId())) {
            LOGGER.error("Manadatory parameter is missing : AddMoneyIMPS : payer user id");
            throw new CashierCheckedException("Manadatory parameter is missing :  payer user id");
        }
    }

}
