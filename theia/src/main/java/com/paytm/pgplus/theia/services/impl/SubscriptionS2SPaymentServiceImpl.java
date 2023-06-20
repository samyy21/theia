/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.biz.workflow.model.SubscriptionRenewalResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@Service("subscriptionS2SPaymentServiceImpl")
public class SubscriptionS2SPaymentServiceImpl implements IJsonResponsePaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionS2SPaymentServiceImpl.class);

    @Autowired
    @Qualifier("subscriptionS2SWorkflow")
    private IWorkFlow subscriptionS2SWorkflow;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    protected ChecksumService checksumService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Override
    public WorkFlowResponseBean processPaymentRequest(final PaymentRequestBean requestData)
            throws TheiaServiceException {
        LOGGER.info("Processing payment request for subscription S2S flow, order id :{}", requestData.getOrderId());

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            SubscriptionRenewalResponse subscriptionRenewalResponse = new SubscriptionRenewalResponse();
            subscriptionRenewalResponse.setMid(requestData.getMid());
            subscriptionRenewalResponse.setOrderId(requestData.getOrderId());
            subscriptionRenewalResponse.setRespCode(e.getResponseConstant().getCode());
            subscriptionRenewalResponse.setRespMsg(e.getMessage());
            subscriptionRenewalResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
            subscriptionRenewalResponse.setTxnAmount(requestData.getTxnAmount());
            WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
            workFlowResponseBean.setSubscriptionRenewalResponse(subscriptionRenewalResponse);
            return workFlowResponseBean;
        }

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }
        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;
        try {
            bizResponseBean = processBizWorkFlow(workFlowRequestBean, requestData);
            WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
            LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
            if (workFlowResponseBean == null) {
                throw new TheiaServiceException("WorkFlowResponseBean is null, Reason : "
                        + bizResponseBean.getFailureDescription());
            }
            return workFlowResponseBean;
        } catch (Exception e) {
            LOGGER.error("Exception Occurred : ", e);
            SubscriptionRenewalResponse subscriptionRenewalResponse = new SubscriptionRenewalResponse();
            subscriptionRenewalResponse.setMid(requestData.getMid());
            subscriptionRenewalResponse.setOrderId(requestData.getOrderId());
            subscriptionRenewalResponse.setRespCode(ResponseConstants.SYSTEM_ERROR.getCode());
            subscriptionRenewalResponse.setRespMsg(ResponseConstants.SYSTEM_ERROR.getMessage());
            subscriptionRenewalResponse.setStatus(ExternalTransactionStatus.PENDING.name());
            subscriptionRenewalResponse.setTxnAmount(requestData.getTxnAmount());
            WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
            workFlowResponseBean.setSubscriptionRenewalResponse(subscriptionRenewalResponse);
            return workFlowResponseBean;
        }
    }

    @Override
    public ValidationResults validatePaymentRequest(final PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean validateChecksum = validateChecksum(requestData);
        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }
        return ValidationResults.VALIDATION_SUCCESS;
    }

    protected boolean validateChecksum(PaymentRequestBean requestData) {
        return checksumService.validateChecksum(requestData);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        GenericCoreResponseBean<WorkFlowResponseBean> response = subscriptionS2SWorkflow.process(workFlowRequestBean);

        if (response.isSuccessfullyProcessed()) {
            return response;
        }

        SubscriptionRenewalResponse renewalResponse = new SubscriptionRenewalResponse();
        renewalResponse.setMid(requestData.getMid());
        renewalResponse.setOrderId(requestData.getOrderId());
        renewalResponse.setRespMsg(response.getFailureMessage());
        renewalResponse.setTxnAmount(requestData.getTxnAmount());
        renewalResponse.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        WorkFlowResponseBean responseBean = new WorkFlowResponseBean();
        responseBean.setSubscriptionRenewalResponse(renewalResponse);
        return new GenericCoreResponseBean<>(responseBean);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getResponseWithChecksumForJsonResponse(String response, String clientId) {
        try {
            HashMap<String, String> responseKeyMap = JsonMapper.mapJsonToObject(response, HashMap.class);
            String mid = responseKeyMap.get(TheiaConstant.ResponseConstants.M_ID);
            if (merchantPreferenceService.isChecksumEnabled(mid)) {
                String merchantKey = merchantExtendInfoUtils.getMerchantKey(mid, clientId);
                String checksum = ValidateChecksum.getInstance().getRespCheckSumValue(merchantKey, responseKeyMap)
                        .get("CHECKSUMHASH");
                responseKeyMap.put(TheiaConstant.ResponseConstants.CHECKSUM, checksum);
                return JsonMapper.mapObjectToJson(responseKeyMap);
            } else {
                LOGGER.info("Checksum is not enabled for the MID : {}. Sending response without checksum.", mid);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while generating response checksum. Will return response without checksum ", e);
        }
        return response;
    }
}
