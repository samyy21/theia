package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.AutoDebitResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.RetryInfo;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.QueryPaymentStatus;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.services.impl.FastForwardServiceHelper;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.PRNUtils;
import com.paytm.pgplus.theia.utils.ResponseCodeUtil;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("autoDebitService")
public class AutoDebitServiceImpl implements IJsonResponsePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDebitServiceImpl.class);

    @Autowired
    @Qualifier("autoDebitFlow")
    private IWorkFlow autoDebitFlow;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    protected ChecksumService checksumService;

    @Autowired
    private AutoDebitCoreService autoDebitCoreService;

    @Autowired
    private PRNUtils prnUtils;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    MerchantResponseUtil merchantResponseUtil;

    @Autowired
    ResponseCodeUtil responseCodeUtil;

    @Autowired
    FastForwardServiceHelper fastForwardServiceHelper;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws TheiaServiceException {
        LOGGER.debug("Processing payment request for AutoDebit flow with Payment_Mode {}",
                requestData.getPaymentTypeId());

        WorkFlowRequestBean workFlowRequestBean;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            throw new TheiaServiceException(e);
        }
        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        checkForNativeRetry(requestData, workFlowRequestBean);

        // Setting TerminalType, ChannelId & ClientIp for auto-debit flow only
        workFlowRequestBean.getEnvInfoReqBean().setTerminalType(ETerminalType.SYSTEM);
        if (!StringUtils.isBlank(requestData.getChannelId())) {
            workFlowRequestBean.setChannelID(requestData.getChannelId());
        } else {
            workFlowRequestBean.setChannelID(workFlowRequestBean.getEnvInfoReqBean().getTerminalType().getTerminal());
        }
        if (StringUtils.isNotBlank(requestData.getAppIp())) {
            workFlowRequestBean.setClientIP(requestData.getAppIp());
            workFlowRequestBean.getEnvInfoReqBean().setClientIp(requestData.getAppIp());
            workFlowRequestBean.getExtendInfo().setClientIP(requestData.getAppIp());
        }

        setIfPRNEnabled(requestData, workFlowRequestBean);
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        boolean isDynamicFeeMerchant = merchantPreferenceService
                .isDynamicFeeMerchant(workFlowRequestBean.getPaytmMID());
        if (merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID()) || isSlabBasedMdr
                || isDynamicFeeMerchant) {
            workFlowRequestBean.setPostConvenience(true);
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
        }

        LOGGER.debug("Modified WorkFlowRequestBean for auto-debit : {}", workFlowRequestBean);

        return processBizWorkFlow(workFlowRequestBean, requestData).getResponse();
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        if (checksumService.validateChecksum(requestData)) {
            return ValidationResults.VALIDATION_SUCCESS;
        }
        return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        // If createOrderAndPay already done , set transId from Cache in request
        // bean
        workFlowRequestBean.setTransID(nativeSessionUtil.getTxnId(requestData.getMid() + requestData.getOrderId()));

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = bizService.processWorkFlow(workFlowRequestBean,
                autoDebitFlow);
        if (bizResponseBean.getResponse() != null) {
            QueryPaymentStatus queryPaymentStatus = bizResponseBean.getResponse().getQueryPaymentStatus();
            // set transId in cache
            nativeSessionUtil.setTxnId(requestData.getMid() + requestData.getOrderId(), bizResponseBean.getResponse()
                    .getTransID());
            if (queryPaymentStatus != null
                    && PaymentStatus.FAIL.toString().equals(queryPaymentStatus.getPaymentStatusValue())) {
                ResponseCodeDetails responseCodeDetails;
                if (StringUtils.isNotBlank(queryPaymentStatus.getPaymentErrorCode()))
                    responseCodeDetails = merchantResponseUtil.getResponseCodeDetail(queryPaymentStatus
                            .getPaymentErrorCode());
                else
                    responseCodeDetails = merchantResponseUtil.getResponseCodeDetail(queryPaymentStatus
                            .getInstErrorCode());
                if (responseCodeDetails != null && responseCodeDetails.getMessageAndRetryDetails() != null) {
                    RetryInfo retryInfo = responseCodeUtil.getRetryInfoFromResponseCodeDetails(
                            responseCodeDetails.getMessageAndRetryDetails(), workFlowRequestBean.getPaytmMID());
                    bizResponseBean.getResponse().setRetryInfo(retryInfo);
                }
            }
        }
        AutoDebitResponse autoDebitResponse = new AutoDebitResponse();
        autoDebitCoreService.setResponseData(bizResponseBean, autoDebitResponse, requestData);
        LOGGER.debug("AutoDebitResponse data : {}", autoDebitResponse);
        return autoDebitCoreService.generateAutoDebitResponse(bizResponseBean, autoDebitResponse);
    }

    @Override
    public String getResponseWithChecksumForJsonResponse(String response, String clientId) {
        throw new TheiaServiceException("This operation is not implemented yet");
    }

    private void setIfPRNEnabled(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (prnUtils.checkIfPRNEnabled(requestData.getMid())) {
            workFlowRequestBean.setPrnEnabled(true);
        }
    }

    private void checkForNativeRetry(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (!nativeRetryUtil.isRetryPossible(workFlowRequestBean)) {
            LOGGER.error("Retry count breached for Native payment. Sending response to merchant");
            requestData.setNativeRetryEnabled(false);
            requestData.setNativeRetryErrorMessage(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getMessage());
            throwExceptionForNativeJsonRequest(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED, requestData);

        } else {
            nativeRetryUtil.increaseRetryCount(requestData.getMid() + requestData.getOrderId(), requestData.getMid(),
                    requestData.getOrderId());
        }
    }

    private void throwExceptionForNativeJsonRequest(ResponseConstants rc, PaymentRequestBean requestBean) {
        throw new NativeFlowException.ExceptionBuilder(rc).isHTMLResponse(false).isNativeJsonRequest(true)
                .isRetryAllowed(requestBean.isNativeRetryEnabled()).build();
    }

}
