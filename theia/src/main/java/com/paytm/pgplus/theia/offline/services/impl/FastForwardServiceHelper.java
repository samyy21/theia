package com.paytm.pgplus.theia.offline.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.utils.AlipayRequestUtils;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.AutoDebitResponse;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.model.link.LinkDetailResponseBody;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IConfigurationService;
import com.paytm.pgplus.models.ExtendInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.AddressInfo;
import com.paytm.pgplus.payloadvault.theia.response.MerchantInfo;
import com.paytm.pgplus.payloadvault.theia.utils.AdditionalInfoUtil;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes;
import com.paytm.pgplus.theia.datamapper.impl.BizRequestResponseMapperImpl;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.model.request.FastForwardRequest;
import com.paytm.pgplus.theia.offline.model.request.FastForwardResponse;
import com.paytm.pgplus.theia.offline.model.request.FastForwardResponseBody;
import com.paytm.pgplus.theia.offline.model.response.ResponseHeader;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.utils.LinkPaymentUtil;
import com.paytm.pgplus.theia.utils.MerchantDataUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.AdditionalCallBackParam.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.REF_ID_REDIS_DEPENDENCY_REMOVAL_NEW_PATTERN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.REF_ID_REDIS_DEPENDENCY_REMOVAL_OLD_PATTERN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.OFFLINE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.PAYMENT_RETRY_INFO;

@Component
public class FastForwardServiceHelper {

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private LinkPaymentUtil linkPaymentUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    @Qualifier("configurationService")
    private IConfigurationService configurationServiceImpl;

    private static final Logger LOGGER = LoggerFactory.getLogger(FastForwardServiceHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FastForwardServiceHelper.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String RISK_EXTENDED_INFO = "risk_extended_info";
    private static final String ORDER_ADDITIONAL_INFO = "orderAdditionalInfo";
    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final String ADDITIONAL_INFO_KEY_VAL_SEPARATOR = ":";

    public PaymentRequestBean preparePaymentRequestBean(FastForwardRequest requestData,
            MerchantDataUtil merchantDataUtil) {
        /*
         * No need to set AutoMode,Channel, TokenType here. Channel will be set
         * later using terminalType details which is SYSTEM in this case. AppIP
         * will be set directly in clientIp in EnvInfo
         */
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        setOrderId(paymentRequestBean, requestData, merchantDataUtil);
        OfflinePaymentUtils.updateOrderIdInMDC(paymentRequestBean.getOrderId());
        paymentRequestBean.setRequestType(requestData.getBody().getReqType());
        paymentRequestBean.setMid(requestData.getHead().getMid());
        paymentRequestBean.setTxnAmount(requestData.getBody().getTxnAmount());
        paymentRequestBean.setTipAmount(requestData.getBody().getTipAmount() != null ? requestData.getBody()
                .getTipAmount().getValue() : null);
        paymentRequestBean.setCustId(requestData.getBody().getCustomerId());
        paymentRequestBean.setCurrency(requestData.getBody().getCurrency());
        paymentRequestBean.setDeviceId(requestData.getBody().getDeviceId());
        paymentRequestBean.setSsoToken(requestData.getHead().getToken());
        paymentRequestBean.setPaymentTypeId(requestData.getBody().getPaymentMode());
        paymentRequestBean.setIndustryTypeId(requestData.getBody().getIndustryType());
        paymentRequestBean.setAppIp(requestData.getBody().getAppIP());
        Map<String, Object> requestDataExtendInfo = requestData.getBody().getExtendInfo();
        if (!CollectionUtils.isEmpty(requestDataExtendInfo)) {
            Object additionalInfo = requestDataExtendInfo.get("additionalInfo");
            Map<String, String> additionalInfoMapFromString = AdditionalInfoUtil
                    .generateMapFromAdditionalInfoString(String.valueOf(additionalInfo));
            String productCode = additionalInfoMapFromString.get("PRODUCT_CODE");
            String qrCodeId = additionalInfoMapFromString.get("qr_code_id");
            paymentRequestBean.setProductCode(productCode);
            paymentRequestBean.setQrCodeId(qrCodeId);
        }
        paymentRequestBean.setPeonURL(OfflinePaymentUtils.getExtendInfoVal(requestData.getBody(),
                TheiaConstant.ExtendedInfoKeys.PEON_URL));
        paymentRequestBean.setMerchUniqueReference(getMercUnqRef(requestData));
        paymentRequestBean.setLinkDescription(OfflinePaymentUtils.getExtendInfoVal(requestData.getBody(),
                TheiaConstant.ExtendedInfoKeys.LINK_DESCRIPTION));
        paymentRequestBean.setUdf1(OfflinePaymentUtils.getExtendInfoVal(requestData.getBody(),
                TheiaConstant.ExtendedInfoKeys.UDF_1));
        paymentRequestBean.setUdf2(OfflinePaymentUtils.getExtendInfoVal(requestData.getBody(),
                TheiaConstant.ExtendedInfoKeys.UDF_2));
        paymentRequestBean.setUdf3(OfflinePaymentUtils.getExtendInfoVal(requestData.getBody(),
                TheiaConstant.ExtendedInfoKeys.UDF_3));
        paymentRequestBean.setAdditionalInfo(OfflinePaymentUtils.getExtendInfoVal(requestData.getBody(),
                TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO));
        paymentRequestBean.setTwoFAConfig(requestData.getBody().getTwoFAConfig());
        if (OfflinePaymentUtils.gethttpServletRequest() != null
                && StringUtils.isNotBlank(OfflinePaymentUtils.gethttpServletRequest().getQueryString())) {
            paymentRequestBean.setQueryParams(OfflinePaymentUtils.gethttpServletRequest().getQueryString());
        }
        // paymentRequestBean.setExtraParamsMap(requestData.getBody().getExtendInfo());
        paymentRequestBean.setRequestType(ERequestType.OFFLINE.getType());
        if (RequestTypes.LINK_BASED_PAYMENT_INVOICE.equals(requestData.getBody().getReqType())) {
            paymentRequestBean.setInvoiceId(paymentRequestBean.getMerchUniqueReference());
            paymentRequestBean.setRequestType(ERequestType.LINK_BASED_PAYMENT_INVOICE.getType());
        }
        if (RequestTypes.LINK_BASED_PAYMENT.equals(requestData.getBody().getReqType())) {
            paymentRequestBean.setLinkId(paymentRequestBean.getMerchUniqueReference());
            paymentRequestBean.setRequestType(ERequestType.LINK_BASED_PAYMENT.getType());
        }
        if (OfflinePaymentUtils.gethttpServletRequest() != null
                && StringUtils.isNotBlank(OfflinePaymentUtils.gethttpServletRequest().getHeader(RISK_EXTENDED_INFO))) {
            String riskInfo = OfflinePaymentUtils.gethttpServletRequest().getHeader(RISK_EXTENDED_INFO);
            LOGGER.info("Risk info : {}", riskInfo);
            paymentRequestBean.setRiskExtendedInfo(riskInfo);
        }
        /*
         * Done for PRN Generation
         */
        if (RequestTypes.AUTO_DEBIT.equals(requestData.getBody().getReqType())) {
            paymentRequestBean.setFastForwardRequest(true);
        }

        if (RequestTypes.AUTO_DEBIT.equals(requestData.getBody().getReqType())
                || RequestTypes.LINK_BASED_PAYMENT.equals(requestData.getBody().getReqType())
                || RequestTypes.LINK_BASED_PAYMENT_INVOICE.equals(requestData.getBody().getReqType())) {
            paymentRequestBean.setParseRiskExtendInfoAsJson(true);
        }

        MerchantBussinessLogoInfo merchantBussinessLogoInfo = null;
        try {
            merchantBussinessLogoInfo = configurationServiceImpl.getMerchantlogoInfoFromMidV2(requestData.getHead()
                    .getMid());
            EXT_LOGGER.customInfo("Mapping response - MerchantBussinessLogoInfo :: {}", merchantBussinessLogoInfo);
        } catch (MappingServiceClientException e) {
            LOGGER.info("Got Exception while fetching merchant logo: {}", e.getErrorMessage());
        }
        if (merchantBussinessLogoInfo != null) {
            paymentRequestBean.setMerchantDisplayName(merchantBussinessLogoInfo.getMerchantDisplayName());
        }
        paymentRequestBean.setSimSubscriptionId(requestData.getBody().getSimSubscriptionId());

        // setting extendInfo in paymentRequestBean
        try {
            if (requestData.getBody().getExtendInfo() != null) {
                ExtendInfo extendInfo = new ExtendInfo();
                if (requestData.getBody().getExtendInfo().containsKey(ORDER_ADDITIONAL_INFO)) {
                    Map<String, String> orderAdditionalInfo = (Map<String, String>) requestData.getBody()
                            .getExtendInfo().get(ORDER_ADDITIONAL_INFO);
                    extendInfo.setOrderAdditionalInfo(orderAdditionalInfo);
                }
                paymentRequestBean.setExtendInfo(extendInfo);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set extendInfo in PaymentRequestBean for fastforward");
        }
        checkAndSetIfScanAndPayFlow(paymentRequestBean);

        return paymentRequestBean;
    }

    private String getMercUnqRef(FastForwardRequest request) {
        String mid = request.getHead().getMid();
        if (aoaUtils.isAOAMerchant(mid)) {
            String txnToken = nativeSessionUtil.getTxnToken(mid, request.getBody().getOrderId());
            if (StringUtils.isNotBlank(txnToken)) {
                NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.getNativeInitiateRequest(txnToken);
                if (nativeInitiateRequest != null && nativeInitiateRequest.getInitiateTxnReq() != null) {
                    InitiateTransactionRequestBody orderDetail = nativeInitiateRequest.getInitiateTxnReq().getBody();
                    if (orderDetail != null && orderDetail.getExtendInfo() != null
                            && StringUtils.isNotBlank(orderDetail.getExtendInfo().getMercUnqRef())) {
                        return orderDetail.getExtendInfo().getMercUnqRef();
                    }
                }
            }
        }
        return OfflinePaymentUtils.getExtendInfoVal(request.getBody(), TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF);
    }

    private static ResponseHeader generateResponseHeader(FastForwardRequest requestData) {

        ResponseHeader head = new ResponseHeader();
        head.setMid(requestData.getHead().getMid());
        head.setVersion(requestData.getHead().getVersion());
        head.setClientId(requestData.getHead().getClientId());
        head.setRequestId(requestData.getHead().getRequestId());
        head.setResponseTimestamp(System.currentTimeMillis());

        return head;
    }

    public String generateResponseForExceptionCases(FastForwardRequest requestData, String errorMessage,
            PaymentRequestBean paymentRequestBean) {
        FastForwardResponse fastForwardResponse = new FastForwardResponse();
        LinkDetailResponseBody linkDetailResponseBody = null;
        if (paymentRequestBean != null) {
            linkDetailResponseBody = linkPaymentUtil.getLinkDetailCachedResponse(paymentRequestBean);
        }
        // Setting response body info
        FastForwardResponseBody body = new FastForwardResponseBody();
        body.setCustId(requestData.getBody().getCustomerId());
        body.setPaymentMode(requestData.getBody().getPaymentMode());
        body.setTxnAmount(requestData.getBody().getTxnAmount());
        body.setSignature(requestData.getBody().getSignature());
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        if (linkDetailResponseBody != null) {
            if ("TXN_FAILURE".equals(resultInfo.getResultStatus())) {
                if (linkDetailResponseBody.getCustomPaymentFailureMessage() != null) {
                    body.setCustomPaymentFailureMessage(linkDetailResponseBody.getCustomPaymentFailureMessage());
                }
                if (linkDetailResponseBody.getRedirectionUrlFailure() != null) {
                    body.setRedirectionUrlFailure(linkDetailResponseBody.getRedirectionUrlFailure());
                }
            } else if ("TXN_SUCCESS".equals(resultInfo.getResultStatus())) {
                if (linkDetailResponseBody.getCustomPaymentSuccessMessage() != null) {
                    body.setCustomPaymentSuccessMessage(linkDetailResponseBody.getCustomPaymentSuccessMessage());
                }
                if (linkDetailResponseBody.getRedirectionUrlSuccess() != null) {
                    body.setRedirectionUrlSuccess(linkDetailResponseBody.getRedirectionUrlSuccess());
                }
            }
        }
        if (StringUtils.isBlank(errorMessage)) {
            // Any exception occurred in system
            resultInfo.setResultCode(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR.getCode());
            resultInfo.setResultMsg(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.SYSTEM_ERROR.getMessage());
        } else {
            // When any input validation failed
            resultInfo
                    .setResultCode(com.paytm.pgplus.pgproxycommon.enums.ResponseConstants.INVALID_JSON_DATA.getCode());
            resultInfo.setResultMsg(errorMessage);
        }
        body.setResultInfo(resultInfo);

        // Preparing final response data
        fastForwardResponse.setHead(generateResponseHeader(requestData));
        fastForwardResponse.setBody(body);
        String jsonResponse = null;
        try {
            jsonResponse = JsonMapper.mapObjectToJson(fastForwardResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("JSON mapping exception", e);
        }

        LOGGER.info("JSON Response generated : {}", jsonResponse);
        return jsonResponse;
    }

    public FastForwardResponse generateFastForwardResponseData(PaymentRequestBean requestData,
            WorkFlowResponseBean workFlowResponseBean, FastForwardRequest request) {
        /*
         * BankTxnId & MBID wont be available in Fast forward flow
         */
        LinkDetailResponseBody linkDetailResponseBody = linkPaymentUtil.getLinkDetailCachedResponse(requestData);

        FastForwardResponse fastForwardResponse = new FastForwardResponse();
        // Setting response body info
        FastForwardResponseBody body = new FastForwardResponseBody();
        body.setBankName(ExtraConstants.AUTO_DEBIT_BANK_NAME);
        body.setCustId(requestData.getCustId());
        body.setOrderId(requestData.getOrderId());
        body.setPaymentMode(requestData.getPaymentTypeId());
        body.setTxnAmount(requestData.getTxnAmount());
        if (linkDetailResponseBody != null) {
            if ("TXN_SUCCESS".equals(workFlowResponseBean.getAutoDebitResponse().getStatus())) {
                {
                    if (linkDetailResponseBody.getCustomPaymentSuccessMessage() != null) {
                        body.setCustomPaymentSuccessMessage(linkDetailResponseBody.getCustomPaymentSuccessMessage());
                    }
                    if (linkDetailResponseBody.getRedirectionUrlFailure() != null) {
                        body.setRedirectionUrlSuccess(linkDetailResponseBody.getRedirectionUrlSuccess());
                    }
                }
            } else if ("TXN_FAILURE".equals(workFlowResponseBean.getAutoDebitResponse().getStatus())) {
                if (linkDetailResponseBody.getCustomPaymentFailureMessage() != null) {
                    body.setCustomPaymentFailureMessage(linkDetailResponseBody.getCustomPaymentFailureMessage());
                }
                if (linkDetailResponseBody.getRedirectionUrlFailure() != null) {
                    body.setRedirectionUrlFailure(linkDetailResponseBody.getRedirectionUrlFailure());
                }
            }
        }
        body.setSignature(request.getBody().getSignature());
        if (workFlowResponseBean != null
                && workFlowResponseBean.getQueryPaymentStatus() != null
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions() != null
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0) != null
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo() != null
                && workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo()
                        .get("referenceNo") != null) {
            body.setBankTxnId(workFlowResponseBean.getQueryPaymentStatus().getPayOptions().get(0).getExtendInfo()
                    .get("referenceNo"));
        }

        if (isPaymentRetryInfoEnabled(requestData)) {
            if (workFlowResponseBean != null && workFlowResponseBean.getWorkFlowRequestBean() != null) {
                if (nativeRetryUtil.isRetryPossible(workFlowResponseBean.getWorkFlowRequestBean())) {
                    LOGGER.info("Setting Retry Info in Final Response of FastForward Request");
                    body.setRetryInfo(workFlowResponseBean.getRetryInfo());
                }
            }
        }
        if (workFlowResponseBean != null && workFlowResponseBean.getWorkFlowRequestBean() != null
                && workFlowResponseBean.getWorkFlowRequestBean().isPostConvenience()
                && StringUtils.isNotBlank(workFlowResponseBean.getWorkFlowRequestBean().getChargeAmount())) {
            body.setChargeAmount(AmountUtils.getTransactionAmountInRupee(workFlowResponseBean.getWorkFlowRequestBean()
                    .getChargeAmount()));
        }
        AutoDebitResponse bizResponse = workFlowResponseBean.getAutoDebitResponse();

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultStatus(bizResponse.getStatus());
        resultInfo.setResultCode(bizResponse.getResponseCode());
        resultInfo.setResultMsg(bizResponse.getResponseMessage());

        body.setPrn(StringUtils.EMPTY);// TODO

        String currentTxnCount = null;
        if (null != workFlowResponseBean.getQueryTransactionStatus()) {
            currentTxnCount = workFlowResponseBean.getQueryTransactionStatus().getCurrentTxnCount();
        }

        if (null != workFlowResponseBean.getQueryPaymentStatus()
                && !CollectionUtils.isEmpty(workFlowResponseBean.getQueryPaymentStatus().getExtendInfo())) {
            Map<String, String> extendInfo = workFlowResponseBean.getQueryPaymentStatus().getExtendInfo();
            decodeAdditionalInfo(extendInfo);
            body.setExtendInfo(OfflinePaymentUtils.toStringObjectMap(extendInfo));
            body.setMerchantInfo(getMerchantInfo(extendInfo, currentTxnCount));
        }
        body.setResultInfo(resultInfo);
        body.setTxnId(bizResponse.getTxnId());

        // Preparing final response data
        fastForwardResponse.setHead(generateResponseHeader(request));
        fastForwardResponse.setBody(body);
        if (ResponseConstants.INVALID_SSO_TOKEN.getCode().equals(
                workFlowResponseBean.getAutoDebitResponse().getResponseCode())) {
            throw PaymentRequestProcessingException.getException(ResultCode.INVALID_SSO_TOKEN_FF);
        }
        return fastForwardResponse;
    }

    private static void decodeAdditionalInfo(Map<String, String> extendInfo) {
        extendInfo.put(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO, AlipayRequestUtils
                .decodeCommentInAdditionalInfo(extendInfo.get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO)));
    }

    public static String convertObjectToJson(FastForwardResponse fastForwardResponse) {
        String jsonResponse = null;
        try {
            jsonResponse = JsonMapper.mapObjectToJson(fastForwardResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("JSON mapping exception", e);
        }
        return jsonResponse;
    }

    private static MerchantInfo getMerchantInfo(Map<String, String> extendedInfoMap, String currentTxnCount) {
        extendedInfoMap.put(ADDRESS_1, AlipayRequestUtils.decode(extendedInfoMap.get(ADDRESS_1)));
        MerchantInfo merchantData = new MerchantInfo();
        merchantData.setName(extendedInfoMap.get(MERCHANT_NAME));
        AddressInfo address = new AddressInfo(extendedInfoMap.get(ADDRESS_1), extendedInfoMap.get(ADDRESS_2),
                extendedInfoMap.get(AREA_NAME), extendedInfoMap.get(CITY_NAME), extendedInfoMap.get(ZIP_CODE),
                extendedInfoMap.get(STATE_NAME), extendedInfoMap.get(COUNTRY_NAME));
        merchantData.setAddress(address);
        merchantData.setPhone(extendedInfoMap.get(PHONE));
        merchantData.setMccCode(extendedInfoMap.get(MCC_CODE));
        merchantData.setCategory(extendedInfoMap.get(CATEGORY));
        merchantData.setSubCategory(extendedInfoMap.get(SUB_CATEGORY));
        merchantData.setQrDisplayName(extendedInfoMap.get(QR_DISPLAY_NAME));
        merchantData.setCurrentTxnCount(currentTxnCount);
        return merchantData;
    }

    private void setOrderId(PaymentRequestBean paymentRequestBean, FastForwardRequest requestData,
            MerchantDataUtil merchantDataUtil) {
        if (requestData == null || requestData.getBody() == null || paymentRequestBean == null) {
            return;
        }
        if (StringUtils.isNotEmpty(requestData.getBody().getOrderId())) {
            LOGGER.info("FastForward Request with orderId = {}", requestData.getBody().getOrderId());
            paymentRequestBean.setOrderId(requestData.getBody().getOrderId());
        } else {
            // LOGGER.info("FastForward Request without orderId");

            String aggregatorMid = merchantDataUtil.getAggregatorMid(requestData.getHead().getMid());

            paymentRequestBean.setOrderId(OfflinePaymentUtils.generateOrderId(aggregatorMid));
        }
        /*
         * Check PGP-16564. RefId is sent by App in case orderId is not
         * available. This refId is later user to fetch txn status in
         * merchantStatus API.
         */
        if (StringUtils.isNotBlank(requestData.getBody().getRefId())
                && StringUtils.isNotBlank(paymentRequestBean.getOrderId())) {
            String key = "REF_ID_" + requestData.getBody().getRefId();
            if (ff4jUtils.isFeatureEnabledOnMid(requestData.getHead().getMid(),
                    REF_ID_REDIS_DEPENDENCY_REMOVAL_NEW_PATTERN, false)) {
                String newKey = key + "_ORDER_ID_MAPPING";
                nativeSessionUtil.setRefIdOrderIdMapping(newKey, paymentRequestBean.getOrderId(), requestData.getHead()
                        .getMid(), requestData.getHead().getToken());
            }
            if (ff4jUtils.isFeatureEnabledOnMid(requestData.getHead().getMid(),
                    REF_ID_REDIS_DEPENDENCY_REMOVAL_OLD_PATTERN, true)) {
                nativeSessionUtil.setKey(key, paymentRequestBean.getOrderId(), 900);
            }
        }
    }

    public boolean isPaymentRetryInfoEnabled(PaymentRequestBean request) {
        Map<String, Object> context = new HashMap<>();
        context.put("mid", request.getMid());
        context.put("merchantType", OFFLINE);
        context.put("custId", request.getCustId());
        if (iPgpFf4jClient.checkWithdefault(PAYMENT_RETRY_INFO, context, false)) {
            LOGGER.info("Feature paymentRetryInfo is enabled");
            return Boolean.TRUE;

        }
        return Boolean.FALSE;
    }

    public void checkAndSetIfScanAndPayFlow(PaymentRequestBean paymentRequestData) {
        String requestType = paymentRequestData.getRequestType();
        if (!(StringUtils.equals(requestType, ERequestType.DYNAMIC_QR.getType()) && !(StringUtils.equals(requestType,
                ERequestType.DYNAMIC_QR_2FA.getType())))) {
            if (BizRequestResponseMapperImpl.isQRCodeRequest(paymentRequestData)) {
                paymentRequestData.setScanAndPayFlow(true);
            }
        }
        setScanAndPayFlag(paymentRequestData.getMid() + paymentRequestData.getOrderId(),
                paymentRequestData.isScanAndPayFlow());
    }

    private Boolean setScanAndPayFlag(String token, Boolean flag) {
        return theiaSessionRedisUtil.hsetIfExist(token, "scanAndPayFlag", flag);
    }

}
