/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import com.paytm.pgplus.theia.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.paytm.pgplus.biz.workflow.model.SeamlessACSPaymentResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.checksum.ValidateChecksum;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IResponseCodeService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.theia.acs.service.IAcsUrlService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;

/**
 * 
 * @author vaishakhnair
 *
 */
@Service("seamlessACSPaymentService")
public class SeamlessACSPaymentServiceImpl implements IJsonResponsePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessACSPaymentServiceImpl.class);

    @Autowired
    @Qualifier("seamlessACSWorkflow")
    private IWorkFlow seamlessACSWorkflow;

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
    private IResponseCodeService responseCodeService;

    @Autowired
    @Qualifier("acsUrlServiceImpl")
    private IAcsUrlService acsUrlService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    protected boolean validateChecksum(PaymentRequestBean requestData) {
        return checksumService.validateChecksum(requestData);
    }

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws TheiaServiceException {
        LOGGER.info("Processing payment request for Seamless ACS flow, order id :{}", requestData.getOrderId());
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
                throw new PaymentRequestValidationException(bizResponseBean.getFailureDescription(),
                        bizResponseBean.getResponseConstant());
            }
            workFlowResponseBean = bizResponseBean.getResponse();
            LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
            Assert.notNull(workFlowResponseBean, "Workflow response bean cannot Be null");
            generateACSResponse(workFlowResponseBean, workFlowRequestBean);
            transactionCacheUtils.putTransInfoInCache(workFlowResponseBean.getTransID(),
                    workFlowRequestBean.getPaytmMID(), workFlowRequestBean.getOrderID(), false);
        } catch (PaymentRequestValidationException e) {
            LOGGER.error("PaymentRequestValidationException under Seamless ACS : ", e);
            workFlowResponseBean = new WorkFlowResponseBean();

            SeamlessACSPaymentResponse response = generateAcsValidationErrorResponse(requestData,
                    e.getResponseConstants() == null ? ResponseConstants.SYSTEM_ERROR : e.getResponseConstants());
            workFlowResponseBean.setSeamlessACSPaymentResponse(response);
        } catch (TheiaDataMappingException e) {
            LOGGER.error("TheiaDataMappingException under Seamless ACS : ", e);
            workFlowResponseBean = new WorkFlowResponseBean();

            SeamlessACSPaymentResponse response = generateAcsValidationErrorResponse(requestData,
                    e.getResponseConstant() == null ? ResponseConstants.SYSTEM_ERROR : e.getResponseConstant());
            workFlowResponseBean.setSeamlessACSPaymentResponse(response);
        } catch (Exception e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            workFlowResponseBean = new WorkFlowResponseBean();

            SeamlessACSPaymentResponse response = generateAcsUnknownErrorResponse(requestData);
            workFlowResponseBean.setSeamlessACSPaymentResponse(response);
        }
        HttpSession session = requestData.getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return workFlowResponseBean;
    }

    private void generateACSResponse(WorkFlowResponseBean workFlowResponseBean, WorkFlowRequestBean workFlowRequestBean) {
        SeamlessACSPaymentResponse seamlessACSPaymentResponse = new SeamlessACSPaymentResponse();
        seamlessACSPaymentResponse.setMid(workFlowRequestBean.getPaytmMID());
        seamlessACSPaymentResponse.setOrderId(workFlowRequestBean.getOrderID());
        seamlessACSPaymentResponse.setStatus(ExternalTransactionStatus.TXN_ACCEPTED.name());
        seamlessACSPaymentResponse.setTxnId(workFlowResponseBean.getTransID());
        seamlessACSPaymentResponse.setTxnAmount(AmountUtils.getTransactionAmountInRupee(workFlowRequestBean
                .getTxnAmount()));
        setResponseCodeAndMessage(ResponseConstants.SUCCESS_RESPONSE_CODE, seamlessACSPaymentResponse);
        seamlessACSPaymentResponse.setAcsURL(acsUrlService.generateACSUrl(workFlowRequestBean.getPaytmMID(),
                workFlowRequestBean.getOrderID(), workFlowResponseBean.getQueryPaymentStatus().getWebFormContext()));
        workFlowResponseBean.setSeamlessACSPaymentResponse(seamlessACSPaymentResponse);
    }

    private SeamlessACSPaymentResponse generateAcsUnknownErrorResponse(PaymentRequestBean requestData) {
        SeamlessACSPaymentResponse response = new SeamlessACSPaymentResponse();
        response.setMid(requestData.getMid());
        response.setOrderId(requestData.getOrderId());
        response.setStatus(ExternalTransactionStatus.PENDING.name());
        setResponseCodeAndMessage(ResponseConstants.SYSTEM_ERROR, response);
        return response;
    }

    private SeamlessACSPaymentResponse generateAcsValidationErrorResponse(PaymentRequestBean requestData,
            ResponseConstants responseConstants) {
        SeamlessACSPaymentResponse response = new SeamlessACSPaymentResponse();
        response.setMid(requestData.getMid());
        response.setOrderId(requestData.getOrderId());
        response.setStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        setResponseCodeAndMessage(responseConstants, response);
        return response;
    }

    private void setResponseCodeAndMessage(ResponseConstants responseConstants, SeamlessACSPaymentResponse response) {
        ResponseCodeDetails responseCodeDetails = responseCodeUtil.getResponseCodeDetails(null,
                responseConstants.getSystemResponseCode(), response.getStatus());
        if (responseCodeDetails != null && StringUtils.isNotBlank(responseCodeDetails.getResponseCode())) {
            response.setRespCode(responseCodeDetails.getResponseCode());
            response.setRespMsg(responseCodeUtil.getResponseMsg(responseCodeDetails));
        } else {
            response.setRespCode(responseConstants.getCode());
            response.setRespMsg(responseConstants.getMessage());
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

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        return bizService.processWorkFlow(workFlowRequestBean, seamlessACSWorkflow);
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
