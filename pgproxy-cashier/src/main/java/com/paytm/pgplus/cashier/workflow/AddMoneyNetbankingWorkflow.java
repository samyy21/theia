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
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * Netbanking Specific Workflow
 *
 * @author Lalit Mehra, Amit Dubey
 * @since March 25, 2016
 */
@Component(value = "AddMoneyNetbankingWorkflow")
public class AddMoneyNetbankingWorkflow extends NetbankingWorkflow {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyNetbankingWorkflow.class);

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public DoPaymentResponse doPayment(CashierRequest cashierRequest) throws CashierCheckedException {
        validateNetBankingDoPaymentRequest(cashierRequest);

        DoPaymentResponse doPaymentResponse = new DoPaymentResponse();

        String acquirementId;

        if (StringUtils.isNotBlank(cashierRequest.getLooperRequest().getAcquirementId())) {
            acquirementId = cashierRequest.getLooperRequest().getAcquirementId();
        } else {
            acquirementId = cashierRequest.getAcquirementId();
        }

        cashierRequest.setFundOrder(true);

        // check payment status ~ looper call
        CashierPaymentStatus paymentStatus = checkPaymentStatus(acquirementId, cashierRequest);
        if (null == paymentStatus) {
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyNB", "response", "payment status null",
                    null);
            throw new CashierCheckedException("Process failed : payment status received as null");
        }
        // populate payment status response
        doPaymentResponse.setPaymentStatus(paymentStatus);

        // check fund order status ~ looper call
        CashierFundOrderStatus fundOrderStatus = checkFundOrderStatus(cashierRequest.getLooperRequest()
                .getFundOrderId(), cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
        if (null == fundOrderStatus) {
            LOGGER.error("Process failed : fund order status received as null : request id : {}",
                    cashierRequest.getRequestId());
            throw new CashierCheckedException("Process failed : fund order status received as null");
        }

        // populate fund order response
        doPaymentResponse.setFundOrderStatus(fundOrderStatus);

        LOGGER.info("Request completed : AddMoneyNetBanking : request id {} ", cashierRequest.getRequestId());
        return doPaymentResponse;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public void doPaymentAsync(CashierRequest cashierRequest, GenericCallBack callBack) throws CashierCheckedException {
        // validate the request
        validateNetBankingDoPaymentRequest(cashierRequest);

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

                        StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyNB", "response",
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
                            fundOrderStatus = checkFundOrderStatus(cashierRequest.getLooperRequest().getFundOrderId(),
                                    cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
                            if (null == fundOrderStatus) {
                                StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyNB", "response",
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
                    LOGGER.info("Request completed : AddMoneyNetBanking : request id {} ",
                            cashierRequest.getRequestId());
                    callBack.processResponse(response);
                });
    }

    private void validateNetBankingDoPaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            LOGGER.error("Manadatory parameter is missing : AddMoneyNetBanking : cashier request ");
            throw new CashierCheckedException("Manadatory parameter is missing : cashier request");
        }

        if (null == cashierRequest.getLooperRequest()) {
            LOGGER.error("Manadatory parameter is missing : AddMoneyNetBanking : looper request ");
            throw new CashierCheckedException("Manadatory parameter is missing : looper request");
        }

        if (StringUtils.isEmpty(cashierRequest.getLooperRequest().getCashierRequestId())) {
            LOGGER.error("Manadatory parameter is missing : AddMoneyNetBanking : cashier request id");
            throw new CashierCheckedException("Manadatory parameter is missing : cashier request id");
        }

        if (StringUtils.isEmpty(cashierRequest.getLooperRequest().getFundOrderId())) {
            LOGGER.error("Manadatory parameter is missing : AddMoneyNetBanking : fund order id");
            throw new CashierCheckedException("Manadatory parameter is missing : fund order id");
        }
    }

}
