package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.InvalidRequestParameterException;
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
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service("defaultMFPaymentService")
public class DefaultMFPaymentServiceImpl extends AbstractPaymentService {
    private static final long serialVersionUID = -3059884766173716078L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMFPaymentServiceImpl.class);

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    @Qualifier("defaultUserNotLoggedFlow")
    private IWorkFlow defaultUserNotLoggedFlow;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Autowired
    private PaymentOTPService paymentOTPUtil;

    @Autowired
    private RetryServiceHelper retryServiceHelper;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException tdme) {
            LOGGER.error("Error while getting workflow response bean due to : {}", tdme.getMessage());
            return theiaResponseGenerator.getPageDetailsResponse(requestData, tdme.getResponseConstant());
        }

        LOGGER.info("WorkFlowRequestBean CREATED : {}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to : {}",
                    bizResponseBean.getFailureDescription());
            return theiaResponseGenerator.getPageDetailsResponse(requestData, bizResponseBean.getResponseConstant());
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason :: {}", bizResponseBean.getFailureDescription());
            throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                    + bizResponseBean.getFailureDescription());
        }

        bizRequestResponseMapper.mapWorkFlowResponseToSession(requestData, workFlowResponseBean);
        LoginInfo loginInfo = sessionDataService.getLoginInfoFromSession(requestData.getRequest());

        try {
            if (loginInfo != null && loginInfo.isLoginFlag()) {
                paymentOTPUtil.generateIfPaymentOTP(requestData);
            }
        } catch (PaytmValidationException pve) {
            LOGGER.error("Exception In generating Payment OTP", pve);
        }

        retryServiceHelper.setRequestDataInCache(workFlowResponseBean.getTransID(), requestData);

        LOGGER.info("Payment Request successfully processed for {} flow", requestData.getRequestType());
        return new PageDetailsResponse(true);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        if (StringUtils.isEmpty(requestData.getAccountNumber()) || StringUtils.isEmpty(requestData.getBankCode())) {
            throw new InvalidRequestParameterException("Invalid request received for DEFAULT_MF", requestData);
        }
        LOGGER.info("Validating checksum for {}", requestData.getRequestType());
        final boolean validateChecksum = validateChecksum(requestData);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }
        return ValidationResults.VALIDATION_SUCCESS;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(
            final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workFlow = fetchWorkflow(workFlowRequestBean);
        return bizService.processWorkFlow(workFlowRequestBean, workFlow);
    }

    private IWorkFlow fetchWorkflow(final WorkFlowRequestBean workFlowRequestBean) {
        IWorkFlow workflow = null;

        if (!StringUtils.isNotBlank(workFlowRequestBean.getToken())) {
            workflow = defaultUserNotLoggedFlow;
        }
        // else
        return workflow;
    }
}
