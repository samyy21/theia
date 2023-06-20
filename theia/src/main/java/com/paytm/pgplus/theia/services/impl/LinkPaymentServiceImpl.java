package com.paytm.pgplus.theia.services.impl;

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
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.PaymentOTPService;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;

/**
 * @author Raman Preet Singh
 * @since 19/09/17
 **/

@Service("linkPaymentService")
public class LinkPaymentServiceImpl extends AbstractPaymentService {
    private static final long serialVersionUID = -3059884766173716078L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkPaymentServiceImpl.class);

    @Autowired
    private PaymentOTPService paymentOTPUtil;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    @Qualifier("linkBasedPaymentFlow")
    private IWorkFlow linkBasedPaymentFlow;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException tdme) {
            LOGGER.error("Error while getting workflow response bean due to : {}", tdme.getMessage());
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    tdme.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        LOGGER.debug("WorkFlowRequestBean CREATED : {}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (StringUtils.isNotBlank(bizResponseBean.getInternalErrorCode())) {
            requestData.setInternalErrorCode(bizResponseBean.getInternalErrorCode());
        }
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to : {}",
                    bizResponseBean.getFailureDescription());
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
            throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                    + bizResponseBean.getFailureDescription());
        }

        transactionCacheUtils.putTransInfoInCacheWrapper(workFlowResponseBean.getTransID(),
                workFlowRequestBean.getPaytmMID(), workFlowRequestBean.getOrderID(), false,
                ERequestType.getByRequestType(requestData.getRequestType()));

        bizRequestResponseMapper.mapWorkFlowResponseToSession(requestData, workFlowResponseBean);

        LoginInfo loginInfo = sessionDataService.getLoginInfoFromSession(requestData.getRequest());

        /** Generate paymentOTP if user loggedin */
        try {
            if (loginInfo != null && loginInfo.isLoginFlag()) {
                paymentOTPUtil.generateIfPaymentOTP(requestData);
            }
        } catch (PaytmValidationException e1) {
            LOGGER.error("Exception In generating Payment OTP", e1);
        }

        retryServiceHelper.setRequestDataInCache(workFlowResponseBean.getTransID(), requestData);

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

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
            final WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, linkBasedPaymentFlow);
    }

}
