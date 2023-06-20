/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.paytm.pgplus.biz.workflow.model.SubscriptionRenewalResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import com.paytm.pgplus.theia.utils.MerchantResponseService;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
@Service("subscriptionRenewalService")
public class SubscriptionRenewalServiceImpl implements IJsonResponsePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRenewalServiceImpl.class);

    @Autowired
    @Qualifier("subscriptionRenewalWorkflow")
    private IWorkFlow subscriptionRenewalWorkflow;

    @Autowired
    @Qualifier("theiaSessionDataService")
    protected ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    protected IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    protected ChecksumService checksumService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    protected boolean validateChecksum(PaymentRequestBean requestData) {
        return checksumService.validateChecksum(requestData);
    }

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws TheiaServiceException {
        LOGGER.info("Processing payment request for subscription renewal flow, order id :{}", requestData.getOrderId());
        WorkFlowResponseBean workFlowResponseBean;
        try {
            WorkFlowRequestBean workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
            LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);
            Assert.notNull(workFlowRequestBean, "Request Bean Generated Cannot Be Null");
            GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);
            /*
             * Check if biz Response Failed and do we have to send response to
             * merchant
             */
            if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                    && bizResponseBean.getResponseConstant().isResponseToMerchant()) {
                LOGGER.info("Sending response to merchant , Biz Call Unsuccessfull due to : {}",
                        bizResponseBean.getFailureDescription());
            }
            workFlowResponseBean = bizResponseBean.getResponse();
            if (null == workFlowResponseBean) {
                workFlowResponseBean = new WorkFlowResponseBean();
                SubscriptionRenewalResponse response = new SubscriptionRenewalResponse();
                response.setMid(requestData.getMid());
                response.setOrderId(requestData.getOrderId());
                response.setSubsId(requestData.getSubscriptionID());
                response.setRespCode(ResponseConstants.SYSTEM_ERROR.getCode());
                response.setRespMsg(bizResponseBean.getFailureDescription());
                response.setStatus("TXN_FAILURE");
                response.setMerchantUniqueRefernce(requestData.getMerchUniqueReference());
                workFlowResponseBean.setSubscriptionRenewalResponse(response);
            }
            LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e);

            workFlowResponseBean = new WorkFlowResponseBean();
            SubscriptionRenewalResponse response = new SubscriptionRenewalResponse();
            response.setMid(requestData.getMid());
            response.setOrderId(requestData.getOrderId());
            response.setSubsId(requestData.getSubscriptionID());
            response.setRespCode(ResponseConstants.SYSTEM_ERROR.getCode());
            response.setRespMsg(ResponseConstants.SYSTEM_ERROR.getMessage());
            response.setStatus("TXN_FAILURE");
            response.setMerchantUniqueRefernce(requestData.getMerchUniqueReference());
            workFlowResponseBean.setSubscriptionRenewalResponse(response);
        }
        HttpSession session = requestData.getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return workFlowResponseBean;
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

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, subscriptionRenewalWorkflow);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getResponseWithChecksumForJsonResponse(String response, String clientId) {
        try {
            if (StringUtils.isNotBlank(response)) {
                TreeMap<String, String> responseKeyMap = JsonMapper.mapJsonToObject(response, TreeMap.class);
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
            }
        } catch (Exception e) {
            LOGGER.error("Exception while generating response checksum. Will return response without checksum ", e);
        }
        return response;
    }
}
