package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaCashierUtil;
import com.paytm.pgplus.theia.utils.TheiaPromoUtil;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.theia.validator.service.ValidationService;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author vivek
 *
 */
@Service("topupExpressService")
public class TopupExpressServiceImpl extends AbstractPaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopupExpressServiceImpl.class);
    private static final long serialVersionUID = -3059884766173716070L;

    @Autowired
    private ValidationService validationService;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("topupExpressFlow")
    private IWorkFlow topupExpressFlow;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    TheiaPromoUtil theiaPromoUtil;

    @Autowired
    TheiaCashierUtil theiaCashierUtil;

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

        LOGGER.info("Processing payment request, request type :{}", requestData.getRequestType());

        WorkFlowRequestBean workFlowRequestBean = null;

        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            failureLogUtil.setFailureMsgForDwhPush(null, e.getMessage(), null, true);
            LOGGER.error("SYSTEM_ERROR ", e);
            return theiaResponseGenerator.getPageDetailsResponse(requestData, e.getResponseConstant());
        }

        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            failureLogUtil.setFailureMsgForDwhPush(null, BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL, null,
                    true);
            throw new TheiaServiceException(BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL);
        }
        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());
            failureLogUtil.setFailureMsgForDwhPush(bizResponseBean.getResponseConstant().getCode(),
                    bizResponseBean.getFailureDescription(), null, true);
            return theiaResponseGenerator.getPageDetailsResponse(requestData, bizResponseBean.getResponseConstant());
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason :: {}", bizResponseBean.getFailureDescription());
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                LOGGER.info("WorkFlowResponseBean is null, Reason : {}", bizResponseBean.getRiskRejectUserMessage());
                requestData.setNativeRetryErrorMessage(bizResponseBean.getRiskRejectUserMessage());
                return theiaResponseGenerator.getPageDetailsResponse(requestData,
                        ResponseConstants.MERCHANT_RISK_REJECT);
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
        if (PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())) {
            if (bizResponseBean.isSuccessfullyProcessed()
                    && !PaymentStatus.FAIL.toString().equals(
                            bizResponseBean.getResponse().getQueryPaymentStatus().getPaymentStatusValue())) {
                theiaSessionDataService.setUPIAccepted(requestData.getRequest(), true);
            } else {
                theiaSessionDataService.setUPIAccepted(requestData.getRequest(), false);
            }
            upiInfoSessionUtil.generateAndSetRequiredUpiTransactionInfoInSession(requestData, workFlowRequestBean,
                    workFlowResponseBean);
            upiInfoSessionUtil.generateAndSetRequiredTransactionInfoInSession(requestData, workFlowRequestBean,
                    workFlowResponseBean);
            upiInfoSessionUtil.generateAndSetRequiredMerchantInfoInSession(requestData);
        }

        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
        String responsePage = theiaResponseGenerator.generateResponseForPaytmExpress(workFlowResponseBean, requestData);
        // LOGGER.info("Response page for add money express is  :: {}",
        // responsePage);
        theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);

        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), true);
        return new PageDetailsResponse(true);
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
        LOGGER.debug("Checksum Validation Result is : {}", validateChecksum);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }
        return ValidationResults.VALIDATION_SUCCESS;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        GenericCoreResponseBean<WorkFlowResponseBean> workFlowResponseBean = null;
        workFlowResponseBean = bizService.processWorkFlow(workFlowRequestBean, topupExpressFlow);
        return workFlowResponseBean;
    }
}
