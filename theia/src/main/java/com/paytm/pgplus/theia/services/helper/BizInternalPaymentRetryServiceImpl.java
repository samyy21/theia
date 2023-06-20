package com.paytm.pgplus.theia.services.helper;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.utils.PaymentAdapterUtil;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.RetryStatus;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.services.InternalPaymentRetryService;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

//import static com.paytm.pgplus.biz.utils.BizConstant.SUPPORTED_PAYMODE_TO_DISABLE_BANKFORM_RETRY;

@Service("bizInternalPaymentRetry")
public class BizInternalPaymentRetryServiceImpl extends
        InternalPaymentRetryService<IWorkFlow, WorkFlowRequestBean, GenericCoreResponseBean<WorkFlowResponseBean>> {

    private static final long serialVersionUID = -8438427119847624895L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BizServiceImpl.class);

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    Ff4jUtils ff4JUtils;

    @Override
    public GenericCoreResponseBean<WorkFlowResponseBean> retryBankFormFetchWithPayment(WorkFlowRequestBean requestBean,
            IWorkFlow workFlow) {
        LOGGER.info("Inside Biz Internal Retry | After first bank form fetch try");

        // Direct bank card flow handling requires internal retry - so we are
        // not disabling for these flows.
        // For other cases - Based on FF4J flag we will be disabling internal
        // retry.
        if (requestBean != null
                && BooleanUtils.isFalse(requestBean.isAllowInternalRetryOnDirectBankCardFlow())
                && BooleanUtils.isFalse(requestBean.isDirectBankCardFlow())
                && ff4JUtils.isFeatureEnabledOnMid(requestBean.getPaytmMID(),
                        BizConstant.Ff4jFeature.DISABLE_INTERNAL_RETRY, false)) {
            LOGGER.info("FF4J flag is enabled : Disabling Biz Internal Retry");
            return null;
        }

        if (PaymentAdapterUtil.eligibleForPG2(requestBean.getRoute())
                && iPgpFf4jClient.checkWithdefault(BizConstant.Ff4jFeature.DISABLE_PG2_INTERNAL_RETRY, null, true)) {
            LOGGER.info("Order Created on PG2, disabling internal retry");
            return null;
        }
        Map keyMap = new HashMap();
        boolean retryEnabled = Boolean.parseBoolean(ConfigurationUtil.getProperty(
                TheiaConstant.BankFormFetchPaymentRetryFlow.BANK_FORM_FETCH_PAYMENT_RETRY_ENABLED, "false"));
        boolean fromAoaMerchant = requestBean != null ? requestBean.isFromAoaMerchant() : false;
        GenericCoreResponseBean<WorkFlowResponseBean> responseBean = null;
        if ((retryEnabled && !fromAoaMerchant) || (requestBean != null && requestBean.isDirectBankCardFlow())) {
            // LOGGER.info("Inside Biz Internal Retry | Retry enabled");
            long retryCount = Long.parseLong(ConfigurationUtil.getProperty(
                    TheiaConstant.BankFormFetchPaymentRetryFlow.BANK_FORM_FETCH_PAYMENT_RETRY_COUNT, "1"));
            LOGGER.info("Inside Cashier Internal Retry | retrycount : {}", retryCount);
            for (long counter = retryCount; counter > 0; counter--) {
                // LOGGER.info("Inside Cashier Internal Retry | current retry count : {}",
                // counter);
                setRetryParams(requestBean, retryCount - counter + 1);
                GenericCoreResponseBean<WorkFlowResponseBean> response = workFlow.process(requestBean);
                if (response != null && response.isSuccessfullyProcessed()) {
                    responseBean = response;
                }
                if (isInternalPaymentRetryRequired(response)) {
                    continue;

                } else {
                    logSuccessToEventUtils(requestBean, keyMap, counter);
                    break;
                }
            }
            if (isInternalPaymentRetryRequired(responseBean)) {
                logFailureToEventUtil(requestBean, keyMap, responseBean);
            }
        }
        return responseBean;
    }

    private void logFailureToEventUtil(WorkFlowRequestBean requestBean, Map keyMap,
            GenericCoreResponseBean<WorkFlowResponseBean> responseBean) {
        keyMap.put("FlowType", requestBean.getRequestType());
        keyMap.put("status", "FAILURE");
        keyMap.put(
                "InstaErrorCode",
                responseBean != null ? responseBean.isSuccessfullyProcessed() ? responseBean.getResponse() != null ? responseBean
                        .getResponse().getQueryPaymentStatus() != null ? responseBean.getResponse()
                        .getQueryPaymentStatus().getActualInstErrorCode() : null : null
                        : null
                        : null);
        EventUtils.pushTheiaEvents(EventNameEnum.INTERNAL_PAYMENT_RETRY, keyMap);
    }

    private void logSuccessToEventUtils(WorkFlowRequestBean requestBean, Map keyMap, long counter) {
        keyMap.put("successCount", counter);
        keyMap.put("status", "SUCCESS");
        keyMap.put("FlowType", requestBean.getRequestType());
        keyMap.put("amount", requestBean.getTxnAmount());
        EventUtils.pushTheiaEvents(EventNameEnum.INTERNAL_PAYMENT_RETRY, keyMap);
    }

    @Override
    public boolean isInternalPaymentRetryRequired(GenericCoreResponseBean<WorkFlowResponseBean> initiatePaymentResponse) {
        return initiatePaymentResponse != null && initiatePaymentResponse.getRetryStatus() != null
                && initiatePaymentResponse.getRetryStatus() == RetryStatus.BANK_FORM_FETCH_FAILED;
        /*
         * commenting as bank form retry disabling feature is not going live
         * Map<String, Object> context = new HashMap<>(); String mid=null; if
         * (initiatePaymentResponse != null &&
         * initiatePaymentResponse.getResponse().getWorkFlowRequestBean() !=
         * null &&
         * initiatePaymentResponse.getResponse().getWorkFlowRequestBean()
         * .getPaytmMID() != null) { mid =
         * initiatePaymentResponse.getResponse().
         * getWorkFlowRequestBean().getPaytmMID(); context.put("mid", mid); }
         * List<String> supportedPaymodes = new ArrayList<>(); String
         * supportedPaymodesToDisableBankFormRetry =
         * ConfigurationUtil.getProperty(
         * SUPPORTED_PAYMODE_TO_DISABLE_BANKFORM_RETRY, ""); boolean
         * isPaymodeAvailable = false; supportedPaymodes =
         * Arrays.asList(supportedPaymodesToDisableBankFormRetry
         * .split(Pattern.quote(","))); if
         * (initiatePaymentResponse.getResponse().getWorkFlowRequestBean() !=
         * null &&
         * initiatePaymentResponse.getResponse().getWorkFlowRequestBean()
         * .getPayMethod() != null) { isPaymodeAvailable =
         * supportedPaymodes.stream().anyMatch(element ->
         * initiatePaymentResponse
         * .getResponse().getWorkFlowRequestBean().getPayMethod
         * ().equalsIgnoreCase(element)); } return initiatePaymentResponse !=
         * null && initiatePaymentResponse.getRetryStatus() != null &&
         * initiatePaymentResponse.getRetryStatus() ==
         * RetryStatus.BANK_FORM_FETCH_FAILED && !(isPaymodeAvailable &&
         * StringUtils.isNotBlank(mid) &&
         * (iPgpFf4jClient.checkWithdefault(TheiaConstant
         * .FF4J.THEIA_DISABLE_RETRY_FOR_FETCHING_BANKFORM, context, false)));
         */
    }

    @Override
    public void setRetryParams(WorkFlowRequestBean request, long currentRetryCount) {
        if (request != null) {
            boolean isInternalRetryEnabled = Boolean.parseBoolean(ConfigurationUtil.getProperty(
                    TheiaConstant.BankFormFetchPaymentRetryFlow.BANK_FORM_FETCH_PAYMENT_RETRY_ENABLED, "false"));
            long totalRetryCount = Long.parseLong(ConfigurationUtil.getProperty(
                    TheiaConstant.BankFormFetchPaymentRetryFlow.BANK_FORM_FETCH_PAYMENT_RETRY_COUNT, "0"));
            request.setTerminalStateForInternalRetry(!(isInternalRetryEnabled && totalRetryCount > 0 && currentRetryCount < totalRetryCount));
            request.setCurrentInternalPaymentRetryCount(currentRetryCount);
            request.setMaxPaymentRetryCount(totalRetryCount);
        }
    }
}
