package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.utils.MerchantPreferenceProvider;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service("paytmExpressSerivice")
public class PaytmExpressSeriviceImpl extends AbstractPaymentService {
    private static final long serialVersionUID = -3059884766173716078L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PaytmExpressSeriviceImpl.class);

    @Autowired
    @Qualifier("paytmExpressFlow")
    private IWorkFlow paytmExpressFlowService;

    @Autowired
    @Qualifier("paytmExpressBuyerPaysChargeFlow")
    private IWorkFlow paytmExpressBuyerPaysChargeFlow;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("merchantPreferenceProvider")
    private MerchantPreferenceProvider merchantPreferenceProvider;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private UpiInfoSessionUtil upiInfoSessionUtil;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {
        LOGGER.info("Processing payment request for Paytm Express flow, order id :{}", requestData.getOrderId());

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData, e.getResponseConstant());
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        LOGGER.debug("WorkFlowRequestBean : {}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL, null,
                    true);
            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL);
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /**
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
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant().getCode(),
                    bizResponseBean.getFailureDescription(), null, true);
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    bizResponseBean.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason :: {}", bizResponseBean.getFailureDescription());
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                        bizResponseBean.getResponseConstant(), bizResponseBean.getRiskRejectUserMessage());

                PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
                pageDetailsResponse.setSuccessfullyProcessed(false);
                pageDetailsResponse.setHtmlPage(htmlPage);
                return pageDetailsResponse;
            } else {
                failureLogUtil.setFailureMsgForDwhPush(
                        bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                                : null,
                        BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                                + bizResponseBean.getFailureDescription(), null, true);
                throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                        + bizResponseBean.getFailureDescription());
            }
        }

        // Set UPI transaction info in session
        upiInfoSessionUtil.generateUPISessionData(workFlowRequestBean, workFlowResponseBean, requestData,
                bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse().getQueryPaymentStatus()
                        .getPaymentStatusValue());

        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
        String responsePage = theiaResponseGenerator.generateResponseForPaytmExpress(workFlowResponseBean, requestData);
        LOGGER.debug("Response page for seamless is  :: {}", responsePage);
        theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);

        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), false);
        return new PageDetailsResponse(true);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, fetchWorkflow(workFlowRequestBean));
    }

    private IWorkFlow fetchWorkflow(final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workflow;
        if (merchantPreferenceProvider.isPostConvenienceFeesEnabled(workFlowRequestBean)) {
            workflow = paytmExpressBuyerPaysChargeFlow;
            workFlowRequestBean.setRequestType(ERequestType.PAYTM_EXPRESS_BUYER_PAYS_CHARGE);
        } else {
            workflow = paytmExpressFlowService;
        }
        return workflow;
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean validateRequest = validationService.validatePaytmExpressData(requestData);

        if (!validateRequest) {
            return ValidationResults.INVALID_REQUEST;
        }

        final boolean validateChecksum = validateChecksum(requestData);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }

        return ValidationResults.VALIDATION_SUCCESS;
    }
}
