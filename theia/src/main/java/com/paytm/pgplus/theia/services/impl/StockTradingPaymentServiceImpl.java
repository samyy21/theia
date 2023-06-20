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
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.helper.ServiceHelper;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;

/**
 * Created by Naman on 03/07/17.
 */
@Service("stockTradingPaymentService")
public class StockTradingPaymentServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = -151202095936452298L;

    private static final Logger LOGGER = LoggerFactory.getLogger(StockTradingPaymentServiceImpl.class);

    @Autowired
    @Qualifier("stockTradingFlow")
    IWorkFlow stockTradingFlow;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {

        LOGGER.info("Processing payment request for seamless flow, order id :{}", requestData.getOrderId());

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            serviceHelper.returnFailureResponseToMerchant(requestData, e.getResponseConstant());
        }

        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);
        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = serviceHelper.processBizWorkFlow(
                workFlowRequestBean, stockTradingFlow, bizService);

        /*
         * Check if biz Response Failed and do we have to send response to
         * merchant
         */
        if (serviceHelper.checkIfBizResponseResponseFailed(bizResponseBean)) {
            LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to... {}",
                    bizResponseBean.getFailureDescription());
            return serviceHelper.returnFailureResponseToMerchant(requestData, bizResponseBean.getResponseConstant());
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        if (workFlowResponseBean == null) {
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                        + bizResponseBean.getRiskRejectUserMessage());
            } else {
                throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                        + bizResponseBean.getFailureDescription());
            }
        }

        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
        String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean, requestData);
        LOGGER.debug("Response page for seamless is : {}", responsePage);

        theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);

        transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(), workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), false);
        return new PageDetailsResponse(true);
    }

    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {

        final boolean validateChecksum = validateChecksum(requestData);

        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }

        return ValidationResults.VALIDATION_SUCCESS;
    }
}
