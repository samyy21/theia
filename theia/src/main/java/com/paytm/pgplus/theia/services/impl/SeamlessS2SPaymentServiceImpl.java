package com.paytm.pgplus.theia.services.impl;

import com.google.gson.Gson;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.BankRedirectionDetail;
import com.paytm.pgplus.biz.workflow.model.PaymentS2SResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.facade.payment.enums.PaymentStatus;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.s2s.utils.BankFormParser;
import com.paytm.pgplus.theia.s2s.utils.PaymentS2SResponseUtil;
import com.paytm.pgplus.theia.services.IBizService;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.services.helper.NativeDirectBankPageHelper;
import com.paytm.pgplus.theia.utils.ChecksumService;
import com.paytm.pgplus.theia.utils.TransactionCacheUtils;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("seamlessS2SPaymentServiceImpl")
public class SeamlessS2SPaymentServiceImpl implements IJsonResponsePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeamlessS2SPaymentServiceImpl.class);

    @Autowired
    protected ChecksumService checksumService;

    @Autowired
    @Qualifier("seamlessflowservice")
    private IWorkFlow seamlessflowservice;

    @Autowired
    @Qualifier("coftPaymentService")
    private IWorkFlow coftPaymentService;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    @Qualifier("bizService")
    protected IBizService bizService;

    @Autowired
    private PaymentS2SResponseUtil responseUtil;

    @Autowired
    private BankFormParser bankFormParser;

    @Autowired
    @Qualifier("nativeDirectBankPageHelper")
    NativeDirectBankPageHelper nativeDirectBankPageHelper;

    @Autowired
    private TransactionCacheUtils transactionCacheUtils;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Override
    public WorkFlowResponseBean processPaymentRequest(PaymentRequestBean requestData) throws TheiaServiceException {
        LOGGER.info("Processing payment request for seamless s2s flow, order id :{}", requestData.getOrderId());

        WorkFlowRequestBean workFlowRequestBean = null;

        try {

            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);

        } catch (TheiaDataMappingException e) {

            PaymentS2SResponse failedResponse = responseUtil.processMerchantFailResponse(requestData,
                    e.getResponseConstant());
            WorkFlowResponseBean flowResponseBean = new WorkFlowResponseBean();
            flowResponseBean.setPaymentS2SResponse(failedResponse);
            return flowResponseBean;
        }

        LOGGER.debug("WorkFlowRequestBean :{}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }

        return processBizWorkFlow(workFlowRequestBean, requestData).getResponse();
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean;
        if (ff4jUtils.isCOFTEnabledOnAOA(requestData.getMid()) && requestData.isCoftTokenTxn()
                && ERequestType.SEAMLESS.getType().equals(requestData.getRequestType()))
            bizResponseBean = bizService.processWorkFlow(workFlowRequestBean, coftPaymentService);
        else
            bizResponseBean = bizService.processWorkFlow(workFlowRequestBean, seamlessflowservice);

        PaymentS2SResponse paymentS2SResponse = setResponseData(bizResponseBean, requestData, workFlowRequestBean);
        LOGGER.debug("S2S Payment response data : {}", paymentS2SResponse);
        if (bizResponseBean.getResponse() != null) {
            bizResponseBean.getResponse().setPaymentS2SResponse(paymentS2SResponse);
        } else {
            WorkFlowResponseBean workFlowResponseBean = new WorkFlowResponseBean();
            workFlowResponseBean.setPaymentS2SResponse(paymentS2SResponse);
            bizResponseBean = new GenericCoreResponseBean<WorkFlowResponseBean>(workFlowResponseBean);
        }

        /*
         * Required in ProcessTransactionS2sWithSessionController
         */
        bizResponseBean.getResponse().setWorkFlowRequestBean(workFlowRequestBean);

        return bizResponseBean;
    }

    private PaymentS2SResponse setResponseData(GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean,
            PaymentRequestBean paymentRequestBean, WorkFlowRequestBean workFlowRequestBean) {

        PaymentS2SResponse paymentS2SResponse = null;

        if (!bizResponseBean.isSuccessfullyProcessed() && bizResponseBean.getResponseConstant() != null
                && bizResponseBean.getResponse() == null) {

            LOGGER.error("Biz Call Unsuccessfull due to : {}", bizResponseBean.getFailureDescription());

            paymentS2SResponse = responseUtil.processMerchantFailResponse(paymentRequestBean,
                    bizResponseBean.getResponseConstant());

        } else if (bizResponseBean.getResponse().getQueryPaymentStatus() != null
                && PaymentStatus.REDIRECT.toString().equals(
                        bizResponseBean.getResponse().getQueryPaymentStatus().getPaymentStatusValue())) {

            if (StringUtils.isNotBlank(bizResponseBean.getResponse().getQueryPaymentStatus().getWebFormContext())) {

                transactionCacheUtils.putTransInfoInCache(bizResponseBean.getResponse().getTransID(),
                        paymentRequestBean.getMid(), paymentRequestBean.getOrderId(), false);

                if (StringUtils.isNotBlank(workFlowRequestBean.getFromAOARequest())
                        && workFlowRequestBean.getFromAOARequest().equalsIgnoreCase("true")
                        && checkIfJsonBankForm(bizResponseBean.getResponse().getQueryPaymentStatus()
                                .getWebFormContext())) {
                    WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();
                    String bankFormJson = workFlowResponseBean.getQueryPaymentStatus().getWebFormContext();
                    BankForm bankForm = new Gson().fromJson(bankFormJson, BankForm.class);
                    nativeDirectBankPageHelper.processForMerchantOwnedDirectBankPageForAOA(workFlowResponseBean,
                            workFlowRequestBean, bankForm);
                    paymentS2SResponse = responseUtil.generateMerchantResponseWithBankForm(paymentRequestBean, null,
                            bankForm);
                    setCashierRequestIdInCache(workFlowRequestBean, workFlowResponseBean);
                } else {
                    BankRedirectionDetail bankRequest = bankFormParser.parseHTMLForm(paymentRequestBean,
                            bizResponseBean.getResponse());
                    paymentS2SResponse = responseUtil.generateMerchantResponseWithBankForm(paymentRequestBean,
                            bankRequest, null);
                }
            } else {
                paymentS2SResponse = responseUtil.processMerchantFailResponse(paymentRequestBean,
                        ResponseConstants.SYSTEM_ERROR);
            }
        } else {
            paymentS2SResponse = responseUtil.processMerchantFailResponse(paymentRequestBean,
                    ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED);
        }
        return paymentS2SResponse;
    }

    private boolean checkIfJsonBankForm(String webFormContext) {
        try {
            new Gson().fromJson(webFormContext, BankForm.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private void setCashierRequestIdInCache(WorkFlowRequestBean workFlowRequestBean,
            WorkFlowResponseBean workFlowResponseBean) {
        nativeDirectBankPageHelper.setCashierRequestIdInCache(
                workFlowRequestBean.getPaytmMID() + workFlowRequestBean.getOrderID(),
                workFlowResponseBean.getCashierRequestId());
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        if (checksumService.validateChecksum(requestData)) {
            return ValidationResults.VALIDATION_SUCCESS;
        }
        return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
    }

    @Override
    public String getResponseWithChecksumForJsonResponse(String response, String clientId) {
        throw new TheiaServiceException("This operation is not supported for this flow");
    }

}
