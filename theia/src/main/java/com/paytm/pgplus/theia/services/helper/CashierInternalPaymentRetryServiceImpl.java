package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.cashier.models.InitiatePaymentResponse;
import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.cashier.service.impl.PaymentServiceImpl;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.utils.RequestIdGenerator;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.InternalPaymentRetryService;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("cashierInternalPaymentRetry")
public class CashierInternalPaymentRetryServiceImpl
        extends
        InternalPaymentRetryService<PaymentServiceImpl, CashierRequest, GenericCoreResponseBean<InitiatePaymentResponse>> {

    private static final long serialVersionUID = -8438427119847624895L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BizServiceImpl.class);

    @Autowired
    private PaymentServiceImpl paymentServiceImpl;

    @Override
    public GenericCoreResponseBean<InitiatePaymentResponse> retryBankFormFetchWithPayment(
            CashierRequest cashierRequest, PaymentServiceImpl paymentServiceImpl) {
        Map keyMap = new HashMap();
        boolean retryEnabled = Boolean.parseBoolean(ConfigurationUtil.getProperty(
                TheiaConstant.BankFormFetchPaymentRetryFlow.BANK_FORM_FETCH_PAYMENT_RETRY_ENABLED, "false"));

        LOGGER.info("Inside Cashier Internal Retry | After first bank form fetch try");
        boolean fromAoaMerchant = cashierRequest != null ? cashierRequest.isFromAoaMerchant() : false;
        if (retryEnabled && !fromAoaMerchant) {
            // LOGGER.info("Inside Biz Internal Retry | Retry enabled");
            long retryCount = Long.parseLong(ConfigurationUtil.getProperty(
                    TheiaConstant.BankFormFetchPaymentRetryFlow.BANK_FORM_FETCH_PAYMENT_RETRY_COUNT, "1"));
            GenericCoreResponseBean<InitiatePaymentResponse> retryInitiatePaymentResponse = null;
            LOGGER.info("Inside Cashier Internal Retry | retrycount : {}", retryCount);
            for (long counter = retryCount; counter > 0; counter--) {
                try {
                    // LOGGER.info("Inside Cashier Internal Retry | current retry count : {}",
                    // counter);
                    cashierRequest = getCashierRequestWithUpdatedRequestId(cashierRequest, counter);
                    retryInitiatePaymentResponse = paymentServiceImpl.initiate(cashierRequest);
                    // Checking if bank form re fetching is required
                    if (isInternalPaymentRetryRequired(retryInitiatePaymentResponse)) {
                        continue;
                    }
                    logSuccessToEventUtils(keyMap, counter);
                    return retryInitiatePaymentResponse;
                } catch (PaytmValidationException e) {
                    LOGGER.error("Error occurred while retrying bank form fetch : {} ", e);
                }
            }
            logFailureToEventUtil(keyMap, retryInitiatePaymentResponse);
            EventUtils.pushTheiaEvents(EventNameEnum.INTERNAL_PAYMENT_RETRY, keyMap);
        }
        return null;
    }

    private void logFailureToEventUtil(Map keyMap,
            GenericCoreResponseBean<InitiatePaymentResponse> retryInitiatePaymentResponse) {
        keyMap.put("status", "FAILURE");
        keyMap.put("FlowType", "CASHIER");
        keyMap.put(
                "InstaErrorCode",
                retryInitiatePaymentResponse != null ? retryInitiatePaymentResponse.isSuccessfullyProcessed() ? retryInitiatePaymentResponse
                        .getResponse() != null ? retryInitiatePaymentResponse.getResponse().getCashierPaymentStatus() != null ? retryInitiatePaymentResponse
                        .getResponse().getCashierPaymentStatus().getInstErrorCode()
                        : null
                        : null
                        : null
                        : null);
    }

    private void logSuccessToEventUtils(Map keyMap, long counter) {
        keyMap.put("successCount", counter);
        keyMap.put("status", "SUCCESS");
        keyMap.put("FlowType", "CASHIER");
        EventUtils.pushTheiaEvents(EventNameEnum.INTERNAL_PAYMENT_RETRY, keyMap);
    }

    @Override
    public boolean isInternalPaymentRetryRequired(
            GenericCoreResponseBean<InitiatePaymentResponse> initiatePaymentResponse) {

        if (initiatePaymentResponse != null && initiatePaymentResponse.isSuccessfullyProcessed()
                && initiatePaymentResponse.getResponse() != null
                && initiatePaymentResponse.getResponse().getCashierPaymentStatus() != null) {
            return initiatePaymentResponse.getResponse().getCashierPaymentStatus().isBankFormFetchFailed();
        }
        return false;
    }

    @Override
    public void setRetryParams(CashierRequest request, long currentRetryCount) {
        return;
    }

    private CashierRequest getCashierRequestWithUpdatedRequestId(CashierRequest request, long counter) {
        CashierRequest cashierRequest = request;
        cashierRequest.setRequestId(RequestIdGenerator.generateRequestId());
        PaymentRequest paymentRequest = cashierRequest.getPaymentRequest();
        if (paymentRequest != null) {
            paymentRequest.setRequestId(cashierRequest.getRequestId());
        }
        return cashierRequest;
    }
}
