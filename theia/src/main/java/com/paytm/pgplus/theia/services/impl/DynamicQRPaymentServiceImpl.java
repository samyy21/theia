/**
 *
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.enums.PaymentTypeIdEnum;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.UPIPSPUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.biz.workflow.service.util.LinkPaymentConsultUtil;
import com.paytm.pgplus.common.bankForm.model.RiskContent;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.model.link.PaymentConsultResponseBody;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.payloadvault.theia.enums.NativePaymentFailureType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.DccPaymentHelper;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankFormData;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankRedirectionDetail;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.model.base.BaseResponse;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.s2s.utils.BankFormParser;
import com.paytm.pgplus.theia.services.AbstractPaymentService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.session.utils.UpiInfoSessionUtil;
import com.paytm.pgplus.theia.utils.DynamicQRUtil;
import com.paytm.pgplus.theia.utils.EventUtils;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtendedInfoPay.AOA_DQR;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.THEIA_LINK_PAYMENT_CONSULT_ENABLED;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.FF4J.THEIA_LINK_PAYMENT_CONSULT_INVOICE_ENABLED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.V1_PTC;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_DISABLE_SETTING_PUSH_DATA_TO_DYNAMIC_QR;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_ENABLE_LINK_FLOW_ON_DQR;

@Service("dynamicQrPaymentService")
public class DynamicQRPaymentServiceImpl extends AbstractPaymentService {

    private static final long serialVersionUID = 817044223204123051L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicQRPaymentServiceImpl.class);

    @Autowired
    @Qualifier("dynamicQRPostScanFlow")
    private IWorkFlow dynamicQRPostScanFlow;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    private UpiInfoSessionUtil upiInfoSessionUtil;

    @Autowired
    private DynamicQRUtil dynamicQRUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    private DccPaymentHelper dccPaymentHelper;

    @Autowired
    private BankFormParser bankFormParser;

    @Autowired
    private EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private Ff4jUtils ff4JUtils;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private UPIPSPUtil upipspUtil;

    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;

    @Autowired
    LinkPaymentConsultUtil linkPaymentConsultUtil;

    @Override
    public PageDetailsResponse processPaymentRequest(PaymentRequestBean requestData, Model responseModel)
            throws TheiaServiceException {

        LOGGER.info("Processing payment request for dynamic QR flow  : {}", requestData);

        // Code for AOA2.0 DynamicQR
        if (ff4JUtils.isFeatureEnabledOnMid(requestData.getMid(), BizConstant.THEIA_AOAMID_TO_PGMID_ENABLED, false)
                && aoaUtils.isAOAMerchant(requestData.getMid())) {
            String mid = aoaUtils.getPgMidForAoaMid(requestData.getMid());
            requestData.setMid(mid);
            LOGGER.info("AOA mid converted to PG mid for AOA2.O : {}", requestData.getMid());
        }

        WorkFlowRequestBean workFlowRequestBean = null;
        try {
            /**
             * This requestType swapping is done for AOA transaction
             */
            String requestType = requestData.getRequestType();
            workFlowRequestBean = bizRequestResponseMapper.mapWorkFlowRequestData(requestData);
            workFlowRequestBean.setRequestType(ERequestType.getByRequestType(requestType));
        } catch (TheiaDataMappingException e) {
            LOGGER.error("SYSTEM_ERROR : ", e);
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData, e.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        requestData.setQrMerchantCallbackUrl(requestData.getCallbackUrl());

        LOGGER.info("DynamicQRPaymentServiceImpl:WorkFlowRequestBean : {}", workFlowRequestBean);

        if (workFlowRequestBean == null) {
            throw new TheiaServiceException("WorkFlowRequestBean is null");
        }
        if (dynamicQRUtil.isDynamicQREdcRequest(requestData) || dynamicQRUtil.checkIsEdcRequest(requestData)) {
            checkForRetry(requestData, workFlowRequestBean);
            workFlowRequestBean.setDynamicQREdcRequest(true);
        }

        String transId = dynamicQRUtil.getTransId(requestData, workFlowRequestBean);

        boolean dynamicQROrderOnAOA = false;
        if (transId == null || StringUtils.isBlank(transId)) {
            String mid = requestData.getMid();
            String orderId = requestData.getOrderId();
            try {
                dynamicQROrderOnAOA = upipspUtil.orderPresentOnAOA(mid, orderId);
                if (dynamicQROrderOnAOA) {
                    workFlowRequestBean.setTxnFlow(AOA_DQR);
                }
            } catch (FacadeCheckedException e) {
                LOGGER.error("AOA Order Status Look-up failed: ", e);
            }
        }
        LOGGER.info("DynamicQRPaymentServiceImpl:TransID : {}", transId);
        workFlowRequestBean.setTxnAmount(AmountUtils.getTransactionAmountInPaise(requestData.getTxnAmount()));
        workFlowRequestBean.setTransID(transId);
        // Hack to process payment request for dynamic QR from APP
        workFlowRequestBean.getEnvInfoReqBean().setTerminalType(ETerminalType.WAP);
        workFlowRequestBean.setChannelID(workFlowRequestBean.getEnvInfoReqBean().getTerminalType().getTerminal());

        /*
         * setting below flag for AOA explicitly as req-type is changed to
         * 'UNI_PAY' in AOA
         */
        if (aoaUtils.isAOAMerchant(workFlowRequestBean.getPaytmMID())
                && !ff4JUtils.isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(),
                        THEIA_DISABLE_SETTING_PUSH_DATA_TO_DYNAMIC_QR, false)) {
            workFlowRequestBean.setPushDataToDynamicQR(true);
        }

        // setting pcf enabled/disable flag
        boolean isSlabBasedMdr = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
        boolean isDynamicFeeMerchant = merchantPreferenceService
                .isDynamicFeeMerchant(workFlowRequestBean.getPaytmMID());
        if (merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID()) || isSlabBasedMdr
                || isDynamicFeeMerchant) {
            workFlowRequestBean.setPostConvenience(true);
            workFlowRequestBean.setSlabBasedMDR(isSlabBasedMdr);
            workFlowRequestBean.setDynamicFeeMerchant(isDynamicFeeMerchant);
        }
        if (StringUtils.isNotBlank(requestData.getAppIp())) {
            workFlowRequestBean.setClientIP(requestData.getAppIp());
            workFlowRequestBean.getEnvInfoReqBean().setClientIp(requestData.getAppIp());
            workFlowRequestBean.getExtendInfo().setClientIP(requestData.getAppIp());
        }
        if (PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())
                && workFlowRequestBean.isUpiPushFlow()) {
            workFlowRequestBean.setUpiPushExpressSupported(true);
            workFlowRequestBean.setPayOption(TheiaConstant.BasicPayOption.UPI_PUSH_EXPRESS);
            workFlowRequestBean.setVirtualPaymentAddress(requestData.getPaymentDetails());
        }

        if (PaymentTypeIdEnum.UPI.value.equals(workFlowRequestBean.getPaymentTypeId())
                && ff4JUtils.isFeatureEnabled(TheiaConstant.DynamicQRRetryConstant.THEIA_DYNAMIC_QR_RETRY_ENABLED,
                        false)) {
            EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) nativeSessionUtil
                    .getKey(enhancedCashierPageServiceHelper.fetchRedisKey(requestData.getMid(),
                            requestData.getOrderId()));
            if (enhanceCashierPageCachePayload == null) {
                checkForRetry(requestData, workFlowRequestBean);
                workFlowRequestBean.setUpiDynamicQrPaymentExceptEnhanceQR(true);
            }
        }

        LOGGER.info("DynamicQRPaymentServiceImpl:Modified WorkFlowRequestBean for dynamicQrFastForwardFlow : {}",
                workFlowRequestBean);

        if (ERequestType.DYNAMIC_QR.getType().equals(requestData.getRequestType())
                && dccPaymentHelper.dccPageTobeRendered(workFlowRequestBean)) {
            NativeJsonResponse dccBankForm = dccPaymentHelper.getNativePlusJsonDccBankform(workFlowRequestBean);
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse(true);
            pageDetailsResponse.setS2sResponse(getJsonString(dccBankForm));
            LOGGER.info("Final Response sent for dcc dynamic qr  : {}", dccBankForm);
            return pageDetailsResponse;
        }

        GenericCoreResponseBean<WorkFlowResponseBean> bizResponseBean = processBizWorkFlow(workFlowRequestBean);

        LOGGER.info("DynamicQRPaymentServiceImpl:WorkFlowResponseBean:BizResponseBean : {} ", bizResponseBean);

        /*
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
            String htmlPage = merchantResponseService.processMerchantFailResponse(requestData,
                    bizResponseBean.getResponseConstant());

            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            return pageDetailsResponse;
        }

        WorkFlowResponseBean workFlowResponseBean = bizResponseBean.getResponse();

        LOGGER.info("DynamicQRPaymentServiceImplBizResponseBean : {} ", bizResponseBean);

        if (workFlowResponseBean == null) {
            LOGGER.error("SYSTEM_ERROR, Reason :: {}", bizResponseBean.getFailureDescription());
            // throw new
            // TheiaServiceException("WorkFlowResponseBean is null, Reason : "
            // + bizResponseBean.getFailureDescription());
            if (StringUtils.isNotBlank(bizResponseBean.getRiskRejectUserMessage())) {
                failureLogUtil.setFailureMsgForDwhPush(
                        bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                                : null,
                        BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                                + bizResponseBean.getRiskRejectUserMessage(), null, true);
                throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant())
                        .isHTMLResponse(false).isRetryAllowed(true).setMsg(bizResponseBean.getRiskRejectUserMessage())
                        .build();
            }
            failureLogUtil.setFailureMsgForDwhPush(
                    bizResponseBean.getResponseConstant() != null ? bizResponseBean.getResponseConstant().getCode()
                            : null,
                    BizConstant.FailureLogs.WORKFLOW_REQUEST_BEAN_IS_NULL_REASON_IS
                            + bizResponseBean.getFailureDescription(), null, true);
            throw new NativeFlowException.ExceptionBuilder(bizResponseBean.getResponseConstant()).isHTMLResponse(false)
                    .setMsg(bizResponseBean.getFailureDescription()).build();
        }

        // bizRequestResponseMapper.mapWorkFlowResponseToSession(requestData,
        // workFlowResponseBean);

        // Set UPI transaction info in session
        upiInfoSessionUtil.generateUPISessionData(workFlowRequestBean, workFlowResponseBean, requestData,
                bizResponseBean.isSuccessfullyProcessed(), bizResponseBean.getResponse().getQueryPaymentStatus()
                        .getPaymentStatusValue());

        LOGGER.debug("WorkFlowResponseBean :{}", workFlowResponseBean);
        if (requestData.isPaymentCallFromDccPage() && workFlowRequestBean.getDccSupported()) {
            String responsePage = theiaResponseGenerator.getBankPage(workFlowResponseBean, requestData);
            LOGGER.debug("Response page for dcc  is : {}", responsePage);
            BaseResponse response = sendEnhancedNativeRedirectionResponse(responsePage, workFlowRequestBean,
                    requestData, workFlowResponseBean);
            return getPageDetailsResponse(response);

        }
        if (requestData.isNativeJsonRequest()) {
            workFlowResponseBean.setWorkFlowRequestBean(workFlowRequestBean);
            NativeJsonResponse nativeJsonResponse = theiaResponseGenerator.getNativeJsonResponse(workFlowResponseBean,
                    requestData, workFlowRequestBean);
            if (nativeJsonResponse != null && nativeJsonResponse.getBody() != null
                    && workFlowResponseBean.getRiskResult() != null) {
                nativeJsonResponse.getBody().setRiskContent(new RiskContent());
                nativeJsonResponse.getBody().getRiskContent()
                        .setEventLinkId(workFlowResponseBean.getRiskResult().getEventLinkId());
            }
            PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
            pageDetailsResponse.setS2sResponse(getJsonString(nativeJsonResponse));
            pageDetailsResponse.setSuccessfullyProcessed(true);
            return pageDetailsResponse;
        } else {
            String responsePage = theiaResponseGenerator.generateResponseForSeamless(workFlowResponseBean, requestData);
            LOGGER.debug("Response page for dynamic QR is : {}", responsePage);
            theiaSessionDataService.setRedirectPageInSession(requestData.getRequest(), responsePage);
            return new PageDetailsResponse(true);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.REQUEST_VALIDATION)
    @Override
    public ValidationResults validatePaymentRequest(PaymentRequestBean requestData)
            throws PaymentRequestValidationException {
        final boolean validateChecksum = validateChecksum(requestData);

        if (!validateChecksum) {
            return ValidationResults.CHECKSUM_VALIDATION_FAILURE;
        }

        return ValidationResults.VALIDATION_SUCCESS;
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> processBizWorkFlow(WorkFlowRequestBean workFlowRequestBean) {
        if (ff4JUtils.isFeatureEnabledOnMid(workFlowRequestBean.getPaytmMID(), THEIA_ENABLE_LINK_FLOW_ON_DQR, false)) {
            GenericCoreResponseBean<WorkFlowResponseBean> linkConsultResponse = checkIfDQRLinkPayment(workFlowRequestBean);
            if (linkConsultResponse != null) {
                LOGGER.info("LinkConsult Failed, Transaction won't be processed");
                return linkConsultResponse;
            }
        }
        return bizService.processWorkFlow(workFlowRequestBean, dynamicQRPostScanFlow);
    }

    private String getJsonString(Object obj) {
        String jsonString = "{}";
        try {
            jsonString = JsonMapper.mapObjectToJson(obj);
        } catch (FacadeCheckedException fce) {
            LOGGER.info("failed mapping object to Json");
        }
        return jsonString;
    }

    private void checkForRetry(PaymentRequestBean requestData, WorkFlowRequestBean workFlowRequestBean) {
        if (nativeRetryUtil.isRetryPossible(workFlowRequestBean)) {
            nativeRetryUtil.increaseRetryCount(workFlowRequestBean.getTxnToken(), requestData.getMid(),
                    requestData.getOrderId());

        } else {
            LOGGER.error("Retry count breached! dynamicQr");
            throw new NativeFlowException.ExceptionBuilder(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED).build();
        }
    }

    private BankFormData sendEnhancedNativeRedirectionResponse(String responsePage,
            WorkFlowRequestBean workFlowRequestBean, PaymentRequestBean requestData,
            WorkFlowResponseBean workFlowResponseBean) {
        BankRedirectionDetail body = new BankRedirectionDetail();
        BankFormData bankFormData = new BankFormData(body);
        if (StringUtils.isBlank(responsePage)) {
            LOGGER.error("Parameter validation failed : {}",
                    PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedCode());
            String errorMessage = PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg();
            body.setResultInfo(getFailureResultInfo(errorMessage, null));
        } else {
            bankFormData.setBody(bankFormParser.parseHTMLFormNative(responsePage));
            com.paytm.pgplus.common.model.ResultInfo resultInfo = OfflinePaymentUtils.resultInfoForSuccess();
            bankFormData.getBody().setResultInfo(
                    new ResultInfo(resultInfo.getResultStatus(), resultInfo.getResultCodeId(), resultInfo
                            .getResultMsg(), resultInfo.isRedirect()));
            if (workFlowResponseBean != null && workFlowResponseBean.getRiskResult() != null) {
                bankFormData.getBody().setRiskContent(new RiskContent());
                bankFormData.getBody().getRiskContent()
                        .setEventLinkId(workFlowResponseBean.getRiskResult().getEventLinkId());
            }
            checkBankFormRetrievalFailed(bankFormData, workFlowRequestBean, requestData);

        }
        if (workFlowResponseBean != null && workFlowResponseBean.isBankFormFetchFailed()) {
            bankFormData.getBody().getResultInfo().setRedirect(false);
            bankFormData.getBody().getResultInfo().setResultStatus(ResultCode.FAILED.getResultStatus());
            bankFormData.getBody().getResultInfo()
                    .setResultMsg(PaytmValidationExceptionType.INVALID_BANK_FORM.getValidationFailedMsg());
        }
        if (bankFormData != null && bankFormData.getBody() != null) {
            EventUtils.logResponseCode(V1_PTC, EventNameEnum.RESPONSE_CODE_SENT, bankFormData.getBody().getResultInfo()
                    .getResultCode(), bankFormData.getBody().getResultInfo().getResultMsg());
        }
        return bankFormData;
    }

    private void checkBankFormRetrievalFailed(BankFormData bankFormData, WorkFlowRequestBean workFlowRequestBean,
            PaymentRequestBean requestData) {
        if (NativePaymentFailureType.BANK_FORM_RETRIEVAL_FAILED.equals(workFlowRequestBean.getPaymentFailureType())
                && requestData.isNativeRetryEnabled()) {
            bankFormData.getBody().getResultInfo().setRedirect(false);
            bankFormData.getBody().getResultInfo().setResultStatus(ResultCode.FAILED.getResultStatus());
            bankFormData
                    .getBody()
                    .getResultInfo()
                    .setResultMsg(
                            bankFormData.getBody().getContent()
                                    .get(com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.RESPONSE_MSG));

        }

    }

    private ResultInfo getFailureResultInfo(String message, String code) {
        return new ResultInfo("F", code, message, true);
    }

    private GenericCoreResponseBean<WorkFlowResponseBean> checkIfDQRLinkPayment(WorkFlowRequestBean flowRequestBean) {
        String txnToken = nativeSessionUtil.getTxnToken(flowRequestBean.getPaytmMID(), flowRequestBean.getOrderID());
        if (StringUtils.isNotBlank(txnToken) && flowRequestBean.getPaymentRequestBean() != null) {
            String linkId = nativeSessionUtil.getLinkIdForQR(txnToken);
            String invoiceId = nativeSessionUtil.getInvoiceIdForQR(txnToken);
            if (StringUtils.isNotBlank(linkId)) {
                // To fetch and set linkId from redis for standard
                // Re-directional flow
                flowRequestBean.getPaymentRequestBean().setDqrLinkId(linkId);
                LOGGER.info("Link_Id Fetched from redis for Dynamic QR is : {}", linkId);
            } else if (StringUtils.isNotBlank(invoiceId)) {
                // To fetch and set InvoiceId from redis for standard
                // Re-directional flow
                flowRequestBean.getPaymentRequestBean().setDqrInvoiceId(invoiceId);
                LOGGER.info("Invoice_Id Fetched from redis for Dynamic QR is : {}", invoiceId);
            } else {
                // To fetch and set LinkId from OrderDetails for pure JS
                // checkout flow
                NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(txnToken);
                if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                    InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                    if (orderDetail != null && orderDetail.getLinkDetailsData() != null) {
                        if (StringUtils.isNotBlank(orderDetail.getLinkDetailsData().getLinkId())) {
                            flowRequestBean.getPaymentRequestBean().setDqrLinkId(
                                    orderDetail.getLinkDetailsData().getLinkId());
                        } else if (StringUtils.isNotBlank(orderDetail.getLinkDetailsData().getInvoiceId())) {
                            flowRequestBean.getPaymentRequestBean().setDqrInvoiceId(
                                    orderDetail.getLinkDetailsData().getInvoiceId());
                        }
                    }
                }
            }
        }
        if (ff4JUtils.isFeatureEnabled(THEIA_LINK_PAYMENT_CONSULT_ENABLED, false)
                && flowRequestBean != null
                && flowRequestBean.getPaymentRequestBean() != null
                && (StringUtils.isNotBlank(flowRequestBean.getPaymentRequestBean().getDqrLinkId()) || (StringUtils
                        .isNotBlank(flowRequestBean.getPaymentRequestBean().getDqrInvoiceId()) && ff4JUtils
                        .isFeatureEnabled(THEIA_LINK_PAYMENT_CONSULT_INVOICE_ENABLED, false)))) {
            PaymentConsultResponseBody paymentConsultResponseBody = linkPaymentConsultUtil
                    .getLinkPaymentConsultResponse(flowRequestBean.getPaymentRequestBean());
            LOGGER.info("LinkConsult triggered for Dynamic QR Transaction : {}", paymentConsultResponseBody);
            if (paymentConsultResponseBody == null) {
                return new GenericCoreResponseBean<>(ResponseConstants.SYSTEM_ERROR.getMessage(),
                        ResponseConstants.SYSTEM_ERROR);
            } else if (paymentConsultResponseBody.getResultInfo() != null
                    && FacadeConstants.FAIL.equalsIgnoreCase(paymentConsultResponseBody.getResultInfo()
                            .getResultStatus())) {
                if (ResponseConstants.LINK_PAYMENT_ALREADY_PROCESSED.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.LINK_PAYMENT_ALREADY_PROCESSED);
                } else if (ResponseConstants.LINK_PAYMENT_IN_PROCESS.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.LINK_PAYMENT_IN_PROCESS);
                } else if (ResponseConstants.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.TXN_AMT_AND_PAID_AMT_EXCEEDS_TOTAL_PAYMENT_AMT);
                } else if (ResponseConstants.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED.getCode().equals(
                        paymentConsultResponseBody.getResultInfo().getResultCode())) {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.TOTAL_PAYMENT_AMOUNT_LIMIT_REACHED);
                } else {
                    return new GenericCoreResponseBean<>(paymentConsultResponseBody.getResultInfo().getResultMsg(),
                            ResponseConstants.LINK_PAYMENT_CONSULT_FAILURE);
                }
            }
        }
        return null;
    }

    private PageDetailsResponse getPageDetailsResponse(BaseResponse response) {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        try {
            pageDetailsResponse.setS2sResponse(JsonMapper.mapObjectToJson(response));
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting obj to json : {}", e);
            pageDetailsResponse.setSuccessfullyProcessed(false);
            return pageDetailsResponse;
        }
        pageDetailsResponse.setSuccessfullyProcessed(true);
        return pageDetailsResponse;
    }

}
