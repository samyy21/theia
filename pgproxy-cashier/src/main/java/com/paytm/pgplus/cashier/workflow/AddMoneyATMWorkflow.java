/**
 *
 */
package com.paytm.pgplus.cashier.workflow;

import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import org.apache.commons.lang3.StringUtils;
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
 * @author amit.dubey
 *
 */
@Component(value = "AddMoneyATMWorkflow")
public class AddMoneyATMWorkflow extends ATMWorkflow {

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public DoPaymentResponse doPayment(CashierRequest cashierRequest) throws CashierCheckedException {
        // validate the request
        validateBankCardDoPaymentRequest(cashierRequest);

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
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyATM", "response", "payment status null",
                    null);
            throw new CashierCheckedException("Process failed : payment status received null");
        }
        // populate payment status object
        doPaymentResponse.setPaymentStatus(paymentStatus);

        // check fund order status ~ looper call
        CashierFundOrderStatus fundOrderStatus = checkFundOrderStatus(cashierRequest.getLooperRequest()
                .getFundOrderId(), cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());
        if (null == fundOrderStatus) {
            throw new CashierCheckedException("Process failed : fund order status received null");
        }
        // populate fund order status object
        doPaymentResponse.setFundOrderStatus(fundOrderStatus);

        return doPaymentResponse;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public void doPaymentAsync(CashierRequest cashierRequest, GenericCallBack callBack) throws CashierCheckedException {
        // validate the request
        validateBankCardDoPaymentRequest(cashierRequest);

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

                        StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyATM", "response",
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
                                StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyATM", "response",
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
                    callBack.processResponse(response);
                });
    }

    private void validateBankCardDoPaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Manadatory parameter is missing :  cashier request");
        }

        if (null == cashierRequest.getLooperRequest()) {
            throw new CashierCheckedException("Manadatory parameter is missing : looper request");
        }

        if (StringUtils.isEmpty(cashierRequest.getLooperRequest().getCashierRequestId())) {
            throw new CashierCheckedException("Manadatory parameter is missing : cashier request id");
        }

        if (StringUtils.isEmpty(cashierRequest.getLooperRequest().getFundOrderId())) {
            throw new CashierCheckedException("Manadatory parameter is missing : fund order id");
        }
    }
}
