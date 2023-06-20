package com.paytm.pgplus.cashier.workflow;

import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.facade.utils.GenericCallBack;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.exception.CashierInvalidParameterException;
import com.paytm.pgplus.cashier.looper.model.CashierFundOrderStatus;
import com.paytm.pgplus.cashier.looper.model.CashierPaymentStatus;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.DoPaymentResponse;
import com.paytm.pgplus.cashier.util.CommonServiceUtil;
import com.paytm.pgplus.cashier.util.ConfigurationUtil;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * Created by prashant on 1/17/17.
 */
@Component("AddMoneyUPIWorkflow")
public class AddMoneyUPIWorkflow extends UPIWorkFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyBankcardWorkflow.class);

    @Override
    public DoPaymentResponse doPayment(CashierRequest cashierRequest) throws CashierCheckedException,
            PaytmValidationException {

        validateUPIDoPaymentRequest(cashierRequest);

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
            StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyUPI", "response", "payment status null",
                    null);
            throw new CashierCheckedException("Process failed : payment status received as null");
        }
        // populate payment status response
        doPaymentResponse.setPaymentStatus(paymentStatus);

        // check fund order status ~ looper call
        CashierFundOrderStatus fundOrderStatus = checkFundOrderStatus(cashierRequest.getLooperRequest()
                .getFundOrderId(), cashierRequest);
        if (null == fundOrderStatus) {
            throw new CashierCheckedException("Process failed : fund order status received as null");
        }

        // populate fund order response
        doPaymentResponse.setFundOrderStatus(fundOrderStatus);

        return doPaymentResponse;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.CASHIER_PAY)
    public void doPaymentAsync(CashierRequest cashierRequest, GenericCallBack callBack) throws CashierCheckedException {
        // validate the request
        validateUPIDoPaymentRequest(cashierRequest);

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

                        StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyUPI", "response",
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
                                    cashierRequest);
                            if (null == fundOrderStatus) {
                                StatisticsLogger.logForXflush(MDC.get("MID"), "ALIPAY", "AddMoneyUPI", "response",
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
                    LOGGER.info("Request completed : AddMoneyUPI : request id {} ", cashierRequest.getRequestId());
                    callBack.processResponse(response);
                });
    }

    public CashierFundOrderStatus checkFundOrderStatus(String fundOrderId, final CashierRequest cashierRequest)
            throws CashierCheckedException {
        if (StringUtils.isBlank(fundOrderId)) {
            throw new CashierInvalidParameterException("Process failed : fundOrderId can not be null");
        }

        // TODO: need to add support for classes in configuration class itself
        CashierFundOrderStatus cashierFundOrderStatus = null;
        if (CommonServiceUtil.isDisableLoopingForUPI()
                && !(cashierRequest != null && CommonServiceUtil.isUPIPushFlow(cashierRequest.getUpiPushRequest())))
            cashierFundOrderStatus = upiCollectSycnServiceImpl.fetchFundOrderStatus(fundOrderId, cashierRequest);
        else
            cashierFundOrderStatus = looperServiceImpl.fetchFundOrderStatus(fundOrderId,
                    cashierRequest.getPaytmMerchantId(), cashierRequest.getRoute());

        return cashierFundOrderStatus;
    }

    private void validateUPIDoPaymentRequest(CashierRequest cashierRequest) throws CashierCheckedException {
        if (null == cashierRequest) {
            throw new CashierCheckedException("Manadatory parameter is missing : cashier request");
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
