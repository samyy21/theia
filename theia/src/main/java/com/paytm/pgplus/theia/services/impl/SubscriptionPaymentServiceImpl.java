package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.pgproxycommon.utils.TaskFlowUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

import com.paytm.pgplus.biz.taskengine.taskexecutor.TaskExecutor;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.List;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@Service("subscriptionPaymentService")
public class SubscriptionPaymentServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = 6120532701224221906L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPaymentServiceImpl.class);

    @Autowired
    @Qualifier("subscriptionFlowUserLoggedIn")
    private IWorkFlow subscriptionFlowUserLoggedIn;

    @Autowired
    @Qualifier("subscriptionFlowUserNotLoggedIn")
    private IWorkFlow subscriptionFlowUserNotLoggedIn;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    @Override
    public PageDetailsResponse processPaymentRequest(final PaymentRequestBean requestData, final Model responseModel)
            throws TheiaServiceException {
        LOGGER.debug("requestData :{}, responseModel :{}", requestData, responseModel);

        LOGGER.info("Processing payment request for mid :{}, order id :{}, request type :{}", requestData.getMid(),
                requestData.getOrderId(), requestData.getRequestType());

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : ", e);

            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData, e.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean,
                requestData);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (StringUtils.isNotBlank(bizResponseBean.getInternalErrorCode())) {
            requestData.setInternalErrorCode(bizResponseBean.getInternalErrorCode());
        }

        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    bizResponseBean.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        LOGGER.debug("WorkFlowResponseBean : {}", workFlowResponseBean);

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason :: {}", bizResponseBean.getFailureDescription());
            throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                    + bizResponseBean.getFailureDescription());
        }

        bizRequestResponseMapper.mapWorkFlowResponseToSession(requestData, workFlowResponseBean);

        return new PageDetailsResponse(true);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(final PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean validateChecksum = validateChecksum(requestData);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }
        return ValidationResults.VALIDATION_SUCCESS;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        String subsTaskFlowMids = ConfigurationUtil.getProperty("task.flow.subs.mids", "NONE");
        if (TaskFlowUtils.isMidEligibleForTaskFlow(workFlowRequestBean.getPaytmMID(), subsTaskFlowMids)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            GenericCoreResponseBean<WorkFlowResponseBean> responseBean = taskExecutor.execute(workFlowRequestBean);
            stopWatch.stop();
            LOGGER.info("TASK Executor, Total time {}", stopWatch.getTotalTimeMillis());
            return responseBean;
        }

        if (StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
            return bizService.processWorkFlow(workFlowRequestBean, subscriptionFlowUserLoggedIn);
        } else {
            return bizService.processWorkFlow(workFlowRequestBean, subscriptionFlowUserNotLoggedIn);
        }
    }
}
