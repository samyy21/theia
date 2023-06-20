/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.dynamicwrapper.utils.CommonUtils;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.utils.DateUtils;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.utils.ResponseCodeUtil;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Date;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@Service("errorPaymentService")
public class ErrorPaymentServiceImpl extends AbstractPaymentService {
    private static final long serialVersionUID = -3059884766173716078L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorPaymentServiceImpl.class);

    @Autowired
    @Qualifier("paymentErrorFlow")
    private IWorkFlow paymentErrorFlow;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {

        LOGGER.debug("requestData :{}, responseModel :{}", requestData, responseModel);
        LOGGER.info("Processing payment request for mid :{}, order id :{}, request type :{}", requestData.getMid(),
                requestData.getOrderId(), requestData.getRequestType());

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            throw new TheiaServiceException(e);
        }
        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }
        setResponsePage(requestData, workFlowRequestBean);
        return new PageDetailsResponse(true);
    }

    private void setResponsePage(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        TransactionResponse response = new TransactionResponse();
        response.setCustId(requestData.getCustId());
        response.setMid(requestData.getMid());
        response.setOrderId(requestData.getOrderId());
        response.setTxnDate(DateUtils.format(new Date()));
        SystemResponseCode systemRespCode = null;
        if (ValidationResults.UNKNOWN_VALIDATION_FAILURE.toString().equals(requestData.getValidationError())) {
            systemRespCode = SystemResponseCode.PAGE_OPEN_RESPONSE_CODE;
        } else {
            systemRespCode = SystemResponseCode.CHECKSUM_FAILURE_CODE;
        }
        response.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.toString());
        response.setCallbackUrl(workFlowRequestBean.getExtendInfo().getCallBackURL());
        CommonUtils.setExtraParamsMapFromReqToResp(requestData, response);

        // Set Response-Code and Response-Message
        responseCodeUtil.setRespMsgeAndCode(response, null, systemRespCode);

        String responsePage = theiaResponseGenerator.getFinalHtmlResponse(response);
        theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        return ValidationResults.VALIDATION_SUCCESS;
    }

}