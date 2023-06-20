/**
 *
 */
package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.cache.model.txnstatus.utils.TxnStatusConstants;
import com.paytm.pgplus.common.enums.*;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.common.util.PaymentModeMapperUtil;
import com.paytm.pgplus.dynamicwrapper.utils.CommonUtils;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.acquiring.enums.AcquirementStatusType;
import com.paytm.pgplus.facade.acquiring.models.PaymentView;
import com.paytm.pgplus.facade.acquiring.models.StatusDetail;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequest;
import com.paytm.pgplus.facade.acquiring.models.request.QueryByMerchantTransIdRequestBody;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponseBody;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.enums.ApiFunctions;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.payment.models.PayOptionInfo;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.RequestHeaderGenerator;
import com.paytm.pgplus.mappingserviceclient.service.IResponseCodeService;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.AdditionalParam;
import com.paytm.pgplus.payloadvault.theia.response.MerchantInfo;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.enums.ExternalTransactionStatus;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.MappingMerchantUrlInfo;
import com.paytm.pgplus.pgproxycommon.models.MerchantUrlInput;
import com.paytm.pgplus.pgproxycommon.utils.AmountUtils;
import com.paytm.pgplus.pgproxycommon.utils.DateUtils;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.subscription.model.response.SubscriptionCheckStatusResponseBody;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.cache.IMerchantUrlService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.models.ProcessedBmResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedNativeErrorResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedNativeErrorResponseBody;
import com.paytm.pgplus.theia.nativ.model.common.NativeRetryInfo;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoServiceRequest;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeRetryUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.services.helper.EncryptedParamsRequestServiceHelper;
import com.paytm.pgplus.theia.utils.helper.TheiaResponseGeneratorHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.SET_ADDITIONAL_PARAM_IN_RESPONSE;
import static com.paytm.pgplus.common.statistics.StatisticConstants.WALLET;
import static com.paytm.pgplus.payloadvault.theia.constant.EnumValueToMask.SSOTOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.BasicPayOption.PPBL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.GvConsent.PTR_URL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHANNEL_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.ORDER_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.TXN_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestTypes.OFFLINE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.ResponseCodes.SUBSCRIPTION_PENDING_RESPONSE_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.ResponseCodes.SUBSCRIPTION_SUCCESS_RESPONSE_CODE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ResponseConstants.SUBSCRIPTION_PENDING_MESSAGE;

/**
 * @author amitdubey
 * @date Nov 8, 2016
 */
@Service("merchantResponseService")
public class MerchantResponseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MerchantResponseService.class);
    private static final String EMPTY_STRING = "";

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    private TheiaResponseGenerator theiaResponseGenerator;

    @Autowired
    @Qualifier("merchantUrlService")
    private IMerchantUrlService merchantUrlService;

    @Autowired
    private IResponseCodeService responseCodeService;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private IAcquiringOrder acquiringOrder;

    @Autowired
    @Qualifier("merchantMappingService")
    private IMerchantMappingService merchantMappingService;

    @Autowired
    private NativeRetryUtil nativeRetryUtil;

    @Autowired
    private ResponseCodeUtil responseCodeUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private EncryptedParamsRequestServiceHelper encryptedParamsRequestServiceHelper;

    @Autowired
    private TheiaResponseGeneratorHelper theiaResponseGeneratorHelper;

    @Autowired
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private RouterUtil routerUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private MerchantInfoService merchantInfoService;

    public String processMerchantFailResponse(PaymentRequestBean requestData, ResponseConstants responseConstant,
            String riskRejectMessage) {
        TransactionResponse transactionResponse = createRequestForMerchant(requestData, responseConstant, null);
        transactionResponse.setResponseMsg(riskRejectMessage);
        return sendResponseToMerchant(transactionResponse);
    }

    public String processMerchantFailResponse(PaymentRequestBean requestData, ResponseConstants responseConstant) {
        // session invalidate in case of no retry allowed in native
        nativeRetryUtil.invalidateSessionByRequestType(requestData, ERequestType.NATIVE);
        TransactionResponse transactionResponse = createRequestForMerchant(requestData, responseConstant, null);
        return sendResponseToMerchant(transactionResponse);
    }

    private TransactionResponse createRequestForMerchant(PaymentRequestBean requestData,
            ResponseConstants responseConstant, ResultInfo resultInfo) {

        TransactionResponse transactionResponse = new TransactionResponse();
        TransactionResponse cancelledTransactionResponse = null;

        if (StringUtils.isNotEmpty(requestData.getClientId()))
            transactionResponse.setClientId(requestData.getClientId());
        transactionResponse.setMid(requestData.getMid());
        transactionResponse.setOrderId(requestData.getOrderId());
        if (responseConstant != null) {
            try {
                if (ResponseConstants.TRANS_CLOSED.name().equalsIgnoreCase(responseConstant.name())) {
                    try {
                        cancelledTransactionResponse = handleCancelledTransactionForNativeAndEnhanced(
                                requestData.getMid(), requestData.getOrderId(), requestData.getInternalErrorCode());
                        if (cancelledTransactionResponse != null) {
                            transactionResponse.setResponseCode(cancelledTransactionResponse.getResponseCode());
                            transactionResponse.setResponseMsg(cancelledTransactionResponse.getResponseMsg());
                        }
                    } catch (Exception e) {
                        LOGGER.info("exception occured while getting cancelledTransaction response from acqiring order.");
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Exception occured while fetching response Code details ", e);
            }
        } else if (resultInfo != null) {
            if (ResponseConstants.TRANS_CLOSED.name().equalsIgnoreCase(resultInfo.getResultCode())) {
                try {
                    cancelledTransactionResponse = handleCancelledTransactionForNativeAndEnhanced(requestData.getMid(),
                            requestData.getOrderId(), requestData.getInternalErrorCode());
                    if (cancelledTransactionResponse != null) {
                        transactionResponse.setResponseCode(cancelledTransactionResponse.getResponseCode());
                        transactionResponse.setResponseMsg(cancelledTransactionResponse.getResponseMsg());
                    }
                } catch (Exception e) {
                    LOGGER.info("exception occured while getting cancelledTransaction response from acqiring order.");
                }
            }
        }

        /*
         * When validation returns resultinfo with exception instead of response
         * constants we will send System Error code with validation failure
         * message
         */

        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(requestData.getTxnAmount()));
        if (merchantPreferenceService.isReturnPrepaidEnabled(requestData.getMid()) && requestData.isPrepaidCard()) {
            transactionResponse.setPrepaidCard(String.valueOf(requestData.isPrepaidCard()));
        }
        // Set Response-Code and Response-Message
        if (transactionResponse.getResponseCode() == null) {
            responseCodeUtil.setRespMsgeAndCode(transactionResponse,
                    StringUtils.isNotBlank(requestData.getInternalErrorCode()) ? requestData.getInternalErrorCode()
                            : resultInfo != null ? resultInfo.getResultCode() : "",
                    responseConstant != null ? responseConstant.getSystemResponseCode()
                            : SystemResponseCode.SYSTEM_ERROR);
        }

        if (ERequestType.SUBSCRIBE.getType().equals(requestData.getRequestType()) && null != resultInfo
                && null != resultInfo.getResultMsg()) {
            transactionResponse.setResponseMsg(resultInfo.getResultMsg());
        }

        boolean isEnhancePaymentCall = (requestData != null) && requestData.isEnhancedCashierPaymentRequest();
        boolean isEnhanceCashierPageRequest = (requestData != null) && requestData.isEnhancedCashierPageRequest();
        String callbackUrl = getCallbackUrl(requestData);
        if (ERequestType.NATIVE.getType().equals(requestData.getRequestType()) && !isEnhancePaymentCall
                && !isEnhanceCashierPageRequest) {
            StringBuilder sb = new StringBuilder(callbackUrl);
            if (sb.indexOf("?") != -1) {
                sb.append("&");
            } else {
                sb.append("?");
            }
            sb.append("retryAllowed=");
            sb.append(requestData.isNativeRetryEnabled());
            if ((ResponseConstants.RISK_REJECT.equals(responseConstant) || ResponseConstants.MERCHANT_RISK_REJECT
                    .equals(responseConstant)) && StringUtils.isNotBlank(requestData.getNativeRetryErrorMessage())) {
                sb.append("&errorMessage=").append(requestData.getNativeRetryErrorMessage());
            } else {
                sb.append("&errorMessage=").append(transactionResponse.getResponseMsg());
            }
            sb.append("&errorCode=").append(transactionResponse.getResponseCode());
            callbackUrl = sb.toString();
        }
        if (ERequestType.DYNAMIC_QR.getType().equals(requestData.getRequestType())
                || ERequestType.DYNAMIC_QR_2FA.getType().equals(requestData.getRequestType())) {
            callbackUrl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_STATIC_CALLBACK_URL)
                    + requestData.getOrderId();
        }
        if (ResponseConstants.ACCOUNT_NUMBER_MISMATCH.equals(responseConstant)) {
            transactionResponse.setFundSourceVerificationSuccess("false");
        }
        transactionResponse.setCallbackUrl(callbackUrl);
        CommonUtils.setExtraParamsMapFromReqToResp(requestData, transactionResponse);
        // setting splitSettlementInfo in response if present
        if (requestData.getSplitSettlementInfoData() != null) {
            try {
                transactionResponse.setSplitSettlementInfo(JsonMapper.mapObjectToJson(requestData
                        .getSplitSettlementInfoData()));
            } catch (FacadeCheckedException ex) {
                LOGGER.error("exception occured in parsing splitSettlementInfo {} ", ex);
            }
        }

        if ((ResponseConstants.RISK_REJECT.equals(responseConstant) || ResponseConstants.MERCHANT_RISK_REJECT
                .equals(responseConstant)) && StringUtils.isNotBlank(requestData.getNativeRetryErrorMessage())) {
            transactionResponse.setResponseMsg(requestData.getNativeRetryErrorMessage());
        }
        transactionResponse.setRequestedTimestamp(requestData.getRequestedTimeStamp());
        return transactionResponse;
    }

    private String sendResponseToMerchant(TransactionResponse transactionResponse) {
        String responsePage = theiaResponseGenerator.getFinalHtmlResponse(transactionResponse);
        if (StringUtils.isNotBlank(responsePage)) {
            return responsePage;
        }
        throw new TheiaServiceException("Unable to Send Response to Merchant");
    }

    private String getCallbackUrl(PaymentRequestBean requestData) {
        String callbackUrl = null;
        if (StringUtils.isNotBlank(requestData.getCallbackUrl())) {
            callbackUrl = requestData.getCallbackUrl();
        } else {
            String website = requestData.getWebsite();
            String channel = requestData.getChannelId();
            if (StringUtils.isNotBlank(channel) && StringUtils.isNotBlank(website)) {
                final MerchantUrlInput input = new MerchantUrlInput(requestData.getMid(),
                        MappingMerchantUrlInfo.UrlTypeId.RESPONSE, requestData.getWebsite());
                final MappingMerchantUrlInfo merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
                if (merchantUrlInfo != null) {
                    callbackUrl = merchantUrlInfo.getPostBackurl();
                }
            }
        }
        return callbackUrl;
    }

    public String getCallbackUrl(String website, String mid) {
        if (StringUtils.isNotBlank(mid) && StringUtils.isNotBlank(website)) {
            final MerchantUrlInput input = new MerchantUrlInput(mid, MappingMerchantUrlInfo.UrlTypeId.RESPONSE, website);
            final MappingMerchantUrlInfo merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
            if (merchantUrlInfo != null) {
                return merchantUrlInfo.getPostBackurl();
            }
        }
        return null;
    }

    public String processEnhancedCashierPageResponse(PaymentRequestBean requestData, String txnToken) {
        TransactionResponse transactionResponse = createResponseForEnhancedCashierPage(requestData, txnToken);
        return sendResponseToMerchant(transactionResponse);
    }

    private TransactionResponse createResponseForEnhancedCashierPage(PaymentRequestBean requestData, String txnToken) {

        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setMid(requestData.getMid());
        transactionResponse.setOrderId(requestData.getOrderId());
        transactionResponse.setTxnToken(txnToken);
        return transactionResponse;
    }

    public String processResponseForNativeRequestValidationError(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, NativeRetryInfo retryInfo, ResultInfo resultInfo, String customCallbackMessage) {
        TransactionResponse transactionResponse = createNativeRequestForMerchant(orderDetail, request, retryInfo,
                resultInfo, false, customCallbackMessage);

        if (StringUtils.contains(transactionResponse.getCallbackUrl(), "retryAllowed=false")) {
            nativePaymentUtil.invalidateNativeSessionData(request.getParameter(Native.TXN_TOKEN),
                    request.getParameter(Native.MID), request.getParameter(Native.ORDER_ID));
        }

        return sendResponseToMerchant(transactionResponse);
    }

    public TransactionResponse createErrorResponseMerchantForDcc(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, NativeRetryInfo retryInfo, ResultInfo resultInfo) {

        TransactionResponse transactionResponse = new TransactionResponse();
        String mid;
        String orderId;
        String txnAmount;
        String callBackUrl;
        String website;
        String channelId;
        String aggMid = null;
        String cardHash = null;

        if (orderDetail != null) {
            mid = orderDetail.getMid();
            orderId = orderDetail.getOrderId();
            txnAmount = orderDetail.getTxnAmount().getValue();
            callBackUrl = orderDetail.getCallbackUrl();
            website = orderDetail.getWebsiteName();
            channelId = request.getParameter(CHANNEL_ID);
            aggMid = orderDetail.getAggMid();
            cardHash = orderDetail.getCardHash();
        } else {
            /*
             * This is the case when the call is for
             * enhancedCashierPage(merchantcheckout page), so we don't have
             * orderDetail in redis
             */
            mid = request.getParameter(MID);
            if (StringUtils.isBlank(mid)) {
                mid = MDC.get(TheiaConstant.RequestParams.MID);
            }
            orderId = request.getParameter(ORDER_ID);
            if (StringUtils.isBlank(orderId)) {
                orderId = MDC.get(TheiaConstant.RequestParams.ORDER_ID);
            }
            txnAmount = request.getParameter(TXN_AMOUNT);
            callBackUrl = request.getParameter(CALLBACK_URL);
            website = request.getParameter(WEBSITE);
            channelId = request.getParameter(CHANNEL_ID);
        }

        // This is used in case of InvalidInputException: in case orderId is
        // changed
        if (StringUtils.isBlank(orderId))
            orderId = request.getParameter(ORDER_ID);

        transactionResponse.setMid(mid);
        transactionResponse.setOrderId(orderId);
        transactionResponse.setAggMid(aggMid);
        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());

        transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(txnAmount));
        if (StringUtils.isBlank(transactionResponse.getTxnAmount())) {
            transactionResponse.setCurrency(null);
        }
        transactionResponse.setCardHash(cardHash);

        callBackUrl = checkCallback(callBackUrl, website, channelId, mid);

        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(mid);

        callBackUrl = errorCallBackForDccPayment(request, callBackUrl, retryInfo);

        if (StringUtils.isNotBlank(callBackUrl) && !isAES256Encrypted) {
            StringBuilder sb = new StringBuilder(callBackUrl);
            if (sb.indexOf("?") != -1) {
                sb.append("&");
            } else {
                sb.append("?");
            }

            sb.append("retryAllowed=");

            boolean retryAllow = false;
            String retryMsg = "";

            if (retryInfo != null) {
                retryAllow = retryInfo.isRetryAllowed();
                if (StringUtils.isNotBlank(retryInfo.getRetryMessage())) {
                    retryMsg = retryInfo.getRetryMessage();
                } else {
                    if (StringUtils.isNotBlank(resultInfo.getResultMsg())) {
                        retryMsg = resultInfo.getResultMsg();
                    } else if (StringUtils.isNotBlank(transactionResponse.getResponseMsg())) {
                        retryMsg = transactionResponse.getResponseMsg();
                    }
                }

                if (resultInfo.isRedirect() != null && !resultInfo.isRedirect()) {
                    retryAllow = true;
                }
            }
            sb.append(retryAllow);
            sb.append("&errorMessage=");
            if (StringUtils.isNotBlank(transactionResponse.getResponseMsg())) {
                sb.append(transactionResponse.getResponseMsg());
            } else {
                sb.append(retryMsg);
                transactionResponse.setResponseMsg(retryMsg);
            }
            if (StringUtils.isBlank(transactionResponse.getResponseCode())) {

                transactionResponse.setResponseCode(resultInfo.getResultCodeId());

            }
            sb.append("&errorCode=").append(transactionResponse.getResponseCode());
            callBackUrl = sb.toString();
        }
        transactionResponse.setCallbackUrl(callBackUrl);
        if (processTransactionUtil.isOfflinePaymentRequestV1Ptc(request)) {
            transactionResponse.setOfflineRequest(Boolean.TRUE);
        }
        return transactionResponse;
    }

    private TransactionResponse createNativeRequestForMerchant(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, NativeRetryInfo retryInfo, ResultInfo resultInfo,
            boolean isEnhancePaymentRequest, String customCallbackMsg) {

        TransactionResponse transactionResponse = new TransactionResponse();
        addRegionalFieldInPTCResponse(resultInfo);

        String mid;
        String orderId;
        String txnAmount;
        String callBackUrl;
        String website;
        String channelId;
        String aggMid = null;
        String cardHash = null;

        if (orderDetail != null) {
            mid = orderDetail.getMid();
            orderId = orderDetail.getOrderId();
            txnAmount = orderDetail.getTxnAmount().getValue();
            callBackUrl = orderDetail.getCallbackUrl();
            website = orderDetail.getWebsiteName();
            channelId = request.getParameter(TheiaConstant.RequestParams.Native.CHANNEL_ID);
            aggMid = orderDetail.getAggMid();
            cardHash = orderDetail.getCardHash();
        } else {
            /*
             * This is the case when the call is for
             * enhancedCashierPage(merchantcheckout page), so we don't have
             * orderDetail in redis
             */
            mid = request.getParameter(MID);
            if (StringUtils.isBlank(mid)) {
                mid = MDC.get(TheiaConstant.RequestParams.MID);
            }
            orderId = request.getParameter(ORDER_ID);
            if (StringUtils.isBlank(orderId)) {
                orderId = MDC.get(TheiaConstant.RequestParams.ORDER_ID);
            }
            txnAmount = request.getParameter(TXN_AMOUNT);
            callBackUrl = request.getParameter(CALLBACK_URL);
            website = request.getParameter(WEBSITE);
            channelId = request.getParameter(CHANNEL_ID);
        }

        // This is used in case of InvalidInputException: in case orderId is
        // changed
        if (StringUtils.isBlank(orderId))
            orderId = request.getParameter(TheiaConstant.RequestParams.Native.ORDER_ID);

        transactionResponse.setMid(mid);
        transactionResponse.setOrderId(orderId);
        transactionResponse.setAggMid(aggMid);
        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        responseCodeUtil.setRespMsgeAndCode(transactionResponse, resultInfo.getResultCode(),
                ResponseConstants.SYSTEM_ERROR.getSystemResponseCode());
        // Replacing existing RespMsg corresponding to ResultCode with
        // CustomCallback Message
        if (StringUtils.isNotBlank(transactionResponse.getResponseMsg()) && StringUtils.isNotBlank(customCallbackMsg)) {
            transactionResponse.setResponseMsg(customCallbackMsg);
        }
        HttpServletRequest servletRequest = EnvInfoUtil.httpServletRequest();
        if (StringUtils.isNotEmpty((String) servletRequest
                .getAttribute(TheiaConstant.RequestParams.NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE))) {
            transactionResponse.setResponseMsg((String) servletRequest
                    .getAttribute(TheiaConstant.RequestParams.NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE));
        }

        transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(txnAmount));
        if (StringUtils.isBlank(transactionResponse.getTxnAmount())) {
            transactionResponse.setCurrency(null);
        }
        transactionResponse.setCardHash(cardHash);

        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(mid);

        /*
         * TODO needs mapping of codes of response constant ResponseCodeDetails
         * respnseCodeDetails = null; try { respnseCodeDetails =
         * responseCodeService.getPaytmResponseCodeDetails(""); if
         * (respnseCodeDetails != null) {
         * transactionResponse.setResponseMsg(respnseCodeDetails.getRemark()); }
         * } catch (Exception e) {
         * LOGGER.error("Exception occured while fetching response Code details "
         * , e); }
         */

        callBackUrl = checkCallback(callBackUrl, website, channelId, mid);

        // PGP-39948 added WEB and LinkPayment check for enhance flow ,error msg
        // in callback url is not being appended
        if (StringUtils.isNotBlank(callBackUrl)
                && !isAES256Encrypted
                && (!isEnhancePaymentRequest || ((EChannelId.WEB.getValue().equalsIgnoreCase(channelId) || EChannelId.WEB
                        .getValue().equalsIgnoreCase(orderDetail == null ? "" : orderDetail.getChannelId())) && LinkPaymentUtil
                        .isLinkBasedPayment(request, orderDetail)))) {
            StringBuilder sb = new StringBuilder(callBackUrl);
            if (sb.indexOf("?") != -1) {
                sb.append("&");
            } else {
                sb.append("?");
            }

            sb.append("retryAllowed=");

            boolean retryAllow = false;
            String retryMsg = "";

            if (retryInfo != null) {
                retryAllow = retryInfo.isRetryAllowed();
                if (StringUtils.isNotBlank(retryInfo.getRetryMessage())) {
                    retryMsg = retryInfo.getRetryMessage();
                } else {
                    if (StringUtils.isNotBlank(resultInfo.getResultMsg())) {
                        retryMsg = resultInfo.getResultMsg();
                    } else if (StringUtils.isNotBlank(transactionResponse.getResponseMsg())) {
                        retryMsg = transactionResponse.getResponseMsg();
                    }
                }

                if (resultInfo.isRedirect() != null && !resultInfo.isRedirect()) {
                    retryAllow = true;
                }
            }
            sb.append(retryAllow);
            sb.append("&errorMessage=");
            if (StringUtils.isNotBlank(transactionResponse.getResponseMsg())) {
                sb.append(transactionResponse.getResponseMsg());
            } else {
                sb.append(retryMsg);
            }
            sb.append("&errorCode=").append(transactionResponse.getResponseCode());
            callBackUrl = sb.toString();
        }

        transactionResponse.setCallbackUrl(callBackUrl);
        if (processTransactionUtil.isOfflinePaymentRequestV1Ptc(request)) {
            transactionResponse.setOfflineRequest(Boolean.TRUE);
        }

        if (OFFLINE.equalsIgnoreCase(request.getHeader(SOURCE))
                && ff4jUtils.isFeatureEnabledOnMid(mid, SET_ADDITIONAL_PARAM_IN_RESPONSE, false)) {
            AdditionalParam additionalParam = new AdditionalParam();
            MerchantInfo merchantData = new MerchantInfo();
            MerchantInfoServiceRequest merchantDetailsRequest = new MerchantInfoServiceRequest(mid);
            MerchantBussinessLogoInfo merchantDetailsResponse = merchantInfoService
                    .getMerchantInfo(merchantDetailsRequest);

            if (merchantDetailsResponse != null) {
                if (StringUtils.isNotBlank(merchantDetailsResponse.getMerchantBusinessName())) {
                    merchantData.setName(merchantDetailsResponse.getMerchantBusinessName());
                } else {
                    merchantData.setName(merchantDetailsResponse.getMerchantDisplayName());
                }
                if (merchantDetailsResponse.getMerchantImageName() != null)
                    merchantData.setLogoUrl(merchantDetailsResponse.getMerchantImageName());
            }

            additionalParam.setMerchantInfo(merchantData);
            transactionResponse.setAdditionalParam(additionalParam);
        }

        // CommonUtils.setExtraParamsMapFromReqToResp(requestData,
        // transactionResponse);

        return transactionResponse;

    }

    public TransactionResponse createNativeRequestForMerchant(InitiateTransactionRequestBody orderDetail,
            NativeRetryInfo retryInfo, ResultInfo resultInfo, boolean isEnhancePaymentRequest, String customCallbackMsg) {

        TransactionResponse transactionResponse = new TransactionResponse();

        String mid = null;
        String orderId = null;
        String txnAmount = null;
        String callBackUrl = null;
        String website = null;
        String channelId = null;
        String aggMid = null;
        String cardHash = null;

        if (orderDetail != null) {

            mid = orderDetail.getMid();
            orderId = orderDetail.getOrderId();
            txnAmount = orderDetail.getTxnAmount().getValue();
            callBackUrl = orderDetail.getCallbackUrl();
            website = orderDetail.getWebsiteName();
            channelId = orderDetail.getChannelId();
            aggMid = orderDetail.getAggMid();
            cardHash = orderDetail.getCardHash();
        }

        // This is used in case of InvalidInputException: in case orderId is
        // changed

        transactionResponse.setMid(mid);
        transactionResponse.setOrderId(orderId);
        transactionResponse.setAggMid(aggMid);
        transactionResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
        responseCodeUtil.setRespMsgeAndCode(transactionResponse, resultInfo.getResultCode(),
                ResponseConstants.SYSTEM_ERROR.getSystemResponseCode());
        // Replacing existing RespMsg corresponding to ResultCode with
        // CustomCallback Message
        if (StringUtils.isNotBlank(transactionResponse.getResponseMsg()) && StringUtils.isNotBlank(customCallbackMsg)) {
            transactionResponse.setResponseMsg(customCallbackMsg);
        }
        HttpServletRequest servletRequest = EnvInfoUtil.httpServletRequest();
        if (StringUtils.isNotEmpty((String) servletRequest
                .getAttribute(TheiaConstant.RequestParams.NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE))) {
            transactionResponse.setResponseMsg((String) servletRequest
                    .getAttribute(TheiaConstant.RequestParams.NATIVE_SUBSCRIPTION_RESULT_ERROR_MESSAGE));
        }

        transactionResponse.setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(txnAmount));
        if (StringUtils.isBlank(transactionResponse.getTxnAmount())) {
            transactionResponse.setCurrency(null);
        }
        transactionResponse.setCardHash(cardHash);

        boolean isAES256Encrypted = merchantPreferenceService.isAES256EncRequestEnabled(mid);

        /*
         * TODO needs mapping of codes of response constant ResponseCodeDetails
         * respnseCodeDetails = null; try { respnseCodeDetails =
         * responseCodeService.getPaytmResponseCodeDetails(""); if
         * (respnseCodeDetails != null) {
         * transactionResponse.setResponseMsg(respnseCodeDetails.getRemark()); }
         * } catch (Exception e) {
         * LOGGER.error("Exception occured while fetching response Code details "
         * , e); }
         */

        callBackUrl = checkCallback(callBackUrl, website, channelId, mid);

        if (StringUtils.isNotBlank(callBackUrl) /* && !isAES256Encrypted */&& !isEnhancePaymentRequest) {
            StringBuilder sb = new StringBuilder(callBackUrl);
            if (sb.indexOf("?") != -1) {
                sb.append("&");
            } else {
                sb.append("?");
            }

            sb.append("retryAllowed=");

            boolean retryAllow = false;
            String retryMsg = "";

            if (retryInfo != null) {
                retryAllow = retryInfo.isRetryAllowed();
                if (StringUtils.isNotBlank(retryInfo.getRetryMessage())) {
                    retryMsg = retryInfo.getRetryMessage();
                } else {
                    if (StringUtils.isNotBlank(resultInfo.getResultMsg())) {
                        retryMsg = resultInfo.getResultMsg();
                    } else if (StringUtils.isNotBlank(transactionResponse.getResponseMsg())) {
                        retryMsg = transactionResponse.getResponseMsg();
                    }
                }

                if (resultInfo.isRedirect() != null && !resultInfo.isRedirect()) {
                    retryAllow = true;
                }
            }
            sb.append(retryAllow);
            sb.append("&errorMessage=");
            if (StringUtils.isNotBlank(transactionResponse.getResponseMsg())) {
                sb.append(transactionResponse.getResponseMsg());
            } else {
                sb.append(retryMsg);
            }
            sb.append("&errorCode=").append(transactionResponse.getResponseCode());
            callBackUrl = sb.toString();
        }

        transactionResponse.setCallbackUrl(callBackUrl);

        // CommonUtils.setExtraParamsMapFromReqToResp(requestData,
        // transactionResponse);

        return transactionResponse;

    }

    public String checkCallback(String inputCallBackUrl, String inputWebsite, String inputChannelId, String mid) {
        String callbackUrl = null;
        if (StringUtils.isNotBlank(inputCallBackUrl)) {
            callbackUrl = inputCallBackUrl;
        } else {
            String website = inputWebsite;
            String channel = inputChannelId;

            if (StringUtils.isNotBlank(channel) && StringUtils.isNotBlank(website)) {
                final MerchantUrlInput input = new MerchantUrlInput(mid, MappingMerchantUrlInfo.UrlTypeId.RESPONSE,
                        website);
                final MappingMerchantUrlInfo merchantUrlInfo = merchantUrlService.getMerchantUrlInfo(input);
                if (merchantUrlInfo != null) {
                    callbackUrl = merchantUrlInfo.getPostBackurl();
                }
            }
        }
        return callbackUrl;
    }

    public String generateResponseForMerchant(String merchantId, String merchantOrderId) {
        String pageResponse = null;
        String alipayMerchantId = StringUtils.EMPTY;
        MappingMerchantData merchantData = merchantMappingService.getMappingMerchantData(merchantId);
        if (merchantData != null) {
            alipayMerchantId = merchantData.getAlipayId();
        }
        try {
            QueryByMerchantTransIdRequestBody orderQueryRequestBody = new QueryByMerchantTransIdRequestBody(
                    alipayMerchantId, merchantOrderId, true);
            QueryByMerchantTransIdRequest queryByMerchantTransIdRequest = new QueryByMerchantTransIdRequest(
                    RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID), orderQueryRequestBody);
            orderQueryRequestBody.setRoute(routerUtil.getRoute(merchantId, merchantOrderId, null, null,
                    "merchantResponse"));
            queryByMerchantTransIdRequest.getHead().setMerchantId(merchantId);
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = acquiringOrder
                    .queryByMerchantTransId(queryByMerchantTransIdRequest);
            LOGGER.info(
                    "query response of order.query : {}",
                    MaskingUtil.maskObject(queryByMerchantTransIdResponse.getBody(), SSOTOKEN.getFieldName(),
                            SSOTOKEN.getPrex(), SSOTOKEN.getEndx()));
            if (queryByMerchantTransIdResponse.getBody() != null
                    && queryByMerchantTransIdResponse.getBody().getStatusDetail() != null) {
                StatusDetail statusDetail = queryByMerchantTransIdResponse.getBody().getStatusDetail();
                if (AcquirementStatusType.INIT.equals(statusDetail.getAcquirementStatus())) {
                    pageResponse = generateMerchantResponse(queryByMerchantTransIdResponse,
                            ResponseConstants.CORE_SESSION_EXPIRED_FAILURE, ExternalTransactionStatus.PENDING.name());
                } else if (AcquirementStatusType.SUCCESS.equals(statusDetail.getAcquirementStatus())) {
                    pageResponse = generateMerchantResponse(queryByMerchantTransIdResponse,
                            ResponseConstants.SUCCESS_RESPONSE_CODE, ExternalTransactionStatus.TXN_SUCCESS.name());
                }
            }
            if (pageResponse == null)
                pageResponse = generateMerchantResponse(queryByMerchantTransIdResponse,
                        ResponseConstants.MERCHANT_FAILURE_RESPONSE, ExternalTransactionStatus.TXN_FAILURE.name());
        } catch (Exception e) {
            LOGGER.error("Exception occured {}", e);
        }
        return pageResponse;
    }

    public TransactionResponse handleCancelledTransactionForNativeAndEnhanced(String merchantId, String merchantOrderId) {
        return handleCancelledTransactionForNativeAndEnhanced(merchantId, merchantOrderId, null);
    }

    public TransactionResponse handleCancelledTransactionForNativeAndEnhanced(String merchantId,
            String merchantOrderId, String internalErrorCode) {

        TransactionResponse pageResponse = null;

        String alipayMerchantId = StringUtils.EMPTY;

        MappingMerchantData merchantData = merchantMappingService.getMappingMerchantData(merchantId);

        if (merchantData != null) {
            alipayMerchantId = merchantData.getAlipayId();
        }

        try {
            QueryByMerchantTransIdRequestBody orderQueryRequestBody = new QueryByMerchantTransIdRequestBody(
                    alipayMerchantId, merchantOrderId, true);
            QueryByMerchantTransIdRequest queryByMerchantTransIdRequest = new QueryByMerchantTransIdRequest(
                    RequestHeaderGenerator.getHeader(ApiFunctions.QUERY_BY_MERCHANT_TRANS_ID), orderQueryRequestBody);
            QueryByMerchantTransIdResponse queryByMerchantTransIdResponse = acquiringOrder
                    .queryByMerchantTransId(queryByMerchantTransIdRequest);
            LOGGER.info(
                    "query response of order.query : {}",
                    MaskingUtil.maskObject(queryByMerchantTransIdResponse.getBody(), SSOTOKEN.getFieldName(),
                            SSOTOKEN.getPrex(), SSOTOKEN.getEndx()));

            if (queryByMerchantTransIdResponse.getBody() != null
                    && queryByMerchantTransIdResponse.getBody().getStatusDetail() != null) {

                StatusDetail statusDetail = queryByMerchantTransIdResponse.getBody().getStatusDetail();

                if (AcquirementStatusType.INIT.equals(statusDetail.getAcquirementStatus())) {

                    pageResponse = createResponseForSession(queryByMerchantTransIdResponse.getBody(),
                            ResponseConstants.CORE_SESSION_EXPIRED_FAILURE, ExternalTransactionStatus.PENDING.name(),
                            internalErrorCode);

                } else if (AcquirementStatusType.SUCCESS.equals(statusDetail.getAcquirementStatus())) {

                    pageResponse = createResponseForSession(queryByMerchantTransIdResponse.getBody(),
                            ResponseConstants.SUCCESS_RESPONSE_CODE, ExternalTransactionStatus.TXN_SUCCESS.name());
                }
            }

            if (pageResponse == null) {
                return createResponseForSession(queryByMerchantTransIdResponse.getBody(),
                        ResponseConstants.MERCHANT_FAILURE_RESPONSE, ExternalTransactionStatus.TXN_FAILURE.name(),
                        internalErrorCode);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured {}", e);
        }
        return null;
    }

    private String generateMerchantResponse(QueryByMerchantTransIdResponse queryByMerchantTransIdResponse,
            ResponseConstants responseConstants, String status) {
        TransactionResponse transactionResponse = createResponseForSession(queryByMerchantTransIdResponse.getBody(),
                responseConstants, status);
        String responsePage = sendResponseToMerchant(transactionResponse);
        return responsePage;
    }

    private TransactionResponse createResponseForSession(QueryByMerchantTransIdResponseBody response,
            ResponseConstants responseConstant, String status) {
        return createResponseForSession(response, responseConstant, status, null);
    }

    private TransactionResponse createResponseForSession(QueryByMerchantTransIdResponseBody response,
            ResponseConstants responseConstant, String status, String internalErrorCode) {
        TransactionResponse transactionResponse = new TransactionResponse();
        List<PaymentView> paymentViewsList = null;
        transactionResponse.setMid(response.getExtendInfo().get(TxnStatusConstants.EXTENDED_INFO_PAYTM_MERCHANT_ID));
        transactionResponse.setOrderId(response.getMerchantTransId());
        transactionResponse.setResponseCode(responseConstant.getCode());
        transactionResponse.setResponseMsg(responseConstant.getMessage());
        transactionResponse.setTxnId(response.getAcquirementId());
        if (response.getAmountDetail() != null && response.getAmountDetail().getChargeAmount() != null) {
            transactionResponse.setChargeAmount(response.getAmountDetail().getChargeAmount().getAmount());
        }
        if (response.getTimeDetail() != null) {
            transactionResponse.setTxnDate(DateUtils.format(response.getTimeDetail().getCreatedTime()));
        }
        if (CollectionUtils.isNotEmpty(response.getPaymentViews())) {
            paymentViewsList = response.getPaymentViews();
        }

        if (CollectionUtils.isNotEmpty(paymentViewsList)
                && MapUtils.isNotEmpty(paymentViewsList.get(0).getExtendInfo())
                && TheiaConstant.ExtraConstants.EXTENDED_INFO_VALUE_TOPUPANDPAY.equals(paymentViewsList.get(0)
                        .getExtendInfo().get(TheiaConstant.ExtraConstants.EXTENDED_INFO_KEY_TOPUPANDPAY))) {
            transactionResponse.setGateway(WALLET);
            transactionResponse.setBankName(WALLET);
            transactionResponse.setPaymentMode(PayMethod.BALANCE.getOldName());

            List<PayOptionInfo> payOptionInfoList = paymentViewsList.get(0).getPayOptionInfos();
            if (payOptionInfoList != null && payOptionInfoList.get(0) != null
                    && payOptionInfoList.get(0).getExtendInfo() != null) {
                String bankTxnId = payOptionInfoList.get(0).getExtendInfo()
                        .get(TheiaConstant.ExtendedInfoKeys.PaymentStatusKeys.BANK_TXN_ID);
                if (StringUtils.isNotBlank(bankTxnId)) {
                    transactionResponse.setBankTxnId(bankTxnId);
                }
            }
        }

        else if (CollectionUtils.isNotEmpty(paymentViewsList)
                && CollectionUtils.isNotEmpty(paymentViewsList.get(0).getPayOptionInfos())
                && MapUtils.isNotEmpty(paymentViewsList.get(0).getPayOptionInfos().get(0).getExtendInfo())) {
            Map<String, String> extendedInfo = response.getPaymentViews().get(0).getPayOptionInfos().get(0)
                    .getExtendInfo();

            transactionResponse.setBankTxnId(extendedInfo
                    .get(TheiaConstant.ExtendedInfoKeys.PaymentStatusKeys.BANK_TXN_ID));
            transactionResponse.setBankName(extendedInfo.get(TheiaConstant.ResponseConstants.BANKNAME));
            transactionResponse.setPaymentMode(PaymentModeMapperUtil.getNewPayModeDisplayName(getPaymentMode(response
                    .getPaymentViews().get(0).getPayOptionInfos())));
            transactionResponse.setGateway(extendedInfo.get(TheiaConstant.ExtendedInfoKeys.PaymentStatusKeys.GATEWAY));
        }

        transactionResponse.setTransactionStatus(status);

        // Set Response-Code and Response-Message
        responseCodeUtil.setRespMsgeAndCode(transactionResponse, internalErrorCode,
                responseConstant.getSystemResponseCode());

        if (response.getAmountDetail() != null && response.getAmountDetail().getOrderAmount() != null) {
            transactionResponse.setTxnAmount(AmountUtils.getTransactionAmountInRupee(response.getAmountDetail()
                    .getOrderAmount().getValue()));
        }
        String callbackUrl = response.getExtendInfo().get(TheiaConstant.ExtraConstants.EXTENDED_INFO_KEY_CALLBACK_URL);
        transactionResponse.setCallbackUrl(callbackUrl);

        // setting merchantUniqueReference
        if (response.getExtendInfo() != null) {
            // for enhanced
            if (response.getExtendInfo().containsKey(TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF)) {
                transactionResponse.setMerchUniqueReference(response.getExtendInfo().get(
                        TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF));
            }
            // for native
            if (response.getExtendInfo().containsKey(Native.MERC_UNQ_REF)) {
                transactionResponse.setMerchUniqueReference(response.getExtendInfo().get(Native.MERC_UNQ_REF));
                if (StringUtils.isBlank(transactionResponse.getMerchUniqueReference())
                        && CollectionUtils.isNotEmpty(paymentViewsList)
                        && null != paymentViewsList.get(0).getPayRequestExtendInfo()) {
                    transactionResponse.setMerchUniqueReference(paymentViewsList.get(0).getPayRequestExtendInfo()
                            .get(TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF));
                }
            }
        }

        return transactionResponse;
    }

    public void makeReponseToMerchantEnhancedNative(TransactionResponse transactionResponse,
            Map<String, String> paramMap) {
        if (transactionResponse != null) {
            theiaResponseGenerator.fillTransactionResponseMap(transactionResponse, new StringBuilder(), paramMap);
        }
    }

    public EnhancedNativeErrorResponse getErrorResponseForDcc(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, NativeRetryInfo retryInfo, ResultInfo resultInfo, boolean isAES256Encrypted) {
        TransactionResponse txnResp = createErrorResponseMerchantForDcc(orderDetail, request, retryInfo, resultInfo);
        Map<String, String> paramMap;

        if (isAES256Encrypted) {
            paramMap = new HashMap<>();
            String errorResponseHtml = theiaResponseGenerator.getFinalHtmlResponse(txnResp);
            makeReponseToEncryptedMerchantEnhancedNative(errorResponseHtml, paramMap);
        } else {
            paramMap = new TreeMap<>();
            makeReponseToMerchantEnhancedNative(txnResp, paramMap);
        }

        EnhancedNativeErrorResponseBody body = new EnhancedNativeErrorResponseBody();
        com.paytm.pgplus.response.ResultInfo nativeResultInfo = new com.paytm.pgplus.response.ResultInfo(
                resultInfo.getResultStatus(), resultInfo.getResultCodeId(), resultInfo.getResultMsg(),
                resultInfo.isRedirect());
        body.setResultInfo(nativeResultInfo);
        body.setRetryInfo(retryInfo);

        if (request.getAttribute("NATIVE_ENHANCED_FLOW") != null
                && BooleanUtils.toBoolean(request.getAttribute("NATIVE_ENHANCED_FLOW").toString())
                && retryInfo.isRetryAllowed()) {
            paramMap.put("paymentCallFromDccPage", "true");
            paramMap.put(TXN_TOKEN, request.getAttribute(TXN_TOKEN).toString());
            paramMap.put(MID, txnResp.getMid());
            paramMap.put(ORDER_ID, txnResp.getOrderId());
            // removing the default MID and ORDERID key as per
            // v1/showPaymentPage api (case-sensitive)
            paramMap.remove(TheiaConstant.RequestParams.MID);
            paramMap.remove(ORDERID);
        }

        body.setContent(paramMap);
        body.setCallbackUrl(txnResp.getCallbackUrl());

        EnhancedNativeErrorResponse errorResponse = new EnhancedNativeErrorResponse();
        errorResponse.setHead(new ResponseHeader());
        errorResponse.setBody(body);
        return errorResponse;
    }

    public EnhancedNativeErrorResponse getEnhancedNativeErrorResp(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, NativeRetryInfo retryInfo, ResultInfo resultInfo, boolean isRedirect,
            boolean isAES256Encrypted, String customCallbackMsg) {

        com.paytm.pgplus.response.ResultInfo nativeResultInfo = new com.paytm.pgplus.response.ResultInfo(
                resultInfo.getResultStatus(), resultInfo.getResultCodeId(), resultInfo.getResultMsg(),
                resultInfo.isRedirect());
        nativeResultInfo.setRedirect(isRedirect);

        TransactionResponse txnResp = createNativeRequestForMerchant(orderDetail, request, retryInfo, resultInfo, true,
                customCallbackMsg);

        // this makes paramMap with keys to UpperCase like being done in normal
        // flow
        Map<String, String> paramMap;

        if (isAES256Encrypted) {
            paramMap = new HashMap<>();
            String errorResponseHtml = theiaResponseGenerator.getFinalHtmlResponse(txnResp);
            makeReponseToEncryptedMerchantEnhancedNative(errorResponseHtml, paramMap);
        } else {
            paramMap = new TreeMap<>();
            makeReponseToMerchantEnhancedNative(txnResp, paramMap);
        }

        addRegionalFieldInPTCResponse(nativeResultInfo);
        EnhancedNativeErrorResponseBody body = new EnhancedNativeErrorResponseBody();
        body.setResultInfo(nativeResultInfo);
        body.setRetryInfo(retryInfo);
        body.setContent(paramMap);
        body.setCallbackUrl(txnResp.getCallbackUrl());

        EnhancedNativeErrorResponse errorResponse = new EnhancedNativeErrorResponse();
        errorResponse.setHead(new ResponseHeader());
        errorResponse.setBody(body);

        return errorResponse;
    }

    public EnhancedNativeErrorResponse getEnhancedNativeErrorRespForMandateMerchant(String callBackUrl,
            ProcessedBmResponse processedMandateResponse, ResultInfo resultInfo, PaymentRequestBean requestBean,
            boolean isRedirect) {

        com.paytm.pgplus.response.ResultInfo nativeResultInfo = new com.paytm.pgplus.response.ResultInfo(
                resultInfo.getResultStatus(), resultInfo.getResultCodeId(), resultInfo.getResultMsg(),
                resultInfo.isRedirect());
        nativeResultInfo.setRedirect(isRedirect);

        TransactionResponse merchantMandateResponse = prepareMerchantMandateResponse(processedMandateResponse,
                resultInfo, requestBean);
        String mid = null != requestBean ? requestBean.getMid() : null;

        Map<String, String> paramMap = new TreeMap<>();
        StringBuilder builder = new StringBuilder();

        if (StringUtils.isEmpty(callBackUrl)) {
            // for mandate merchants the response would be sent to the
            // registered url for both the cases either paper or e mandate
            callBackUrl = getCallbackUrl(MERCHANT_URL_INFO_WEBSITE_FOR_BM, mid);
        }

        if (StringUtils.isNotBlank(callBackUrl)) {
            theiaResponseGenerator.fillTransactionResponseMap(merchantMandateResponse, builder, paramMap);
        }

        EnhancedNativeErrorResponseBody body = new EnhancedNativeErrorResponseBody();
        body.setResultInfo(nativeResultInfo);
        body.setRetryInfo(new NativeRetryInfo(false, "Retry Not Allowed for mandates"));
        body.setContent(paramMap);
        body.setCallbackUrl(callBackUrl);

        EnhancedNativeErrorResponse errorResponse = new EnhancedNativeErrorResponse();
        errorResponse.setHead(new ResponseHeader());
        errorResponse.setBody(body);

        LOGGER.info("Callback data send to merchant : {} at callBackUrl: {}", builder.toString(), callBackUrl);

        return errorResponse;
    }

    protected String getPaymentMode(List<PayOptionInfo> payOptionInfos) {
        if ((payOptionInfos == null) || payOptionInfos.isEmpty()) {
            return EMPTY_STRING;
        }

        if (payOptionInfos.size() == 1) {
            return payOptionInfos.get(0).getPayMethod().getOldName();
        }
        String paymentMode = EPayMethod.BALANCE.getOldName();
        for (PayOptionInfo payOptionInfo : payOptionInfos) {
            if (payOptionInfo.getExtendInfo() != null) {
                String topupAndPay = payOptionInfo.getExtendInfo().get(
                        TheiaConstant.ExtraConstants.EXTENDED_INFO_KEY_TOPUPANDPAY);
                if (TheiaConstant.ExtraConstants.EXTENDED_INFO_VALUE_TOPUPANDPAY.equals(topupAndPay)) {
                    return paymentMode;
                }
            }
        }
        return EPayMethod.HYBRID_PAYMENT.getOldName();

    }

    public String processMerchantFailResponse(PaymentRequestBean requestData, ResultInfo resultInfo) {
        TransactionResponse transactionResponse = createRequestForMerchant(requestData, null, resultInfo);
        return sendResponseToMerchant(transactionResponse);
    }

    public NativeJsonResponse getErrorNativeJsonResponse(InitiateTransactionRequestBody orderDetail,
            HttpServletRequest request, NativeRetryInfo retryInfo, ResultInfo resultInfo, String customCallbackMessage) {

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();

        com.paytm.pgplus.response.ResultInfo nativeResultInfo = NativePaymentUtil.resultInfo(ResultCode.FAILED);

        /*
         * by default setting retry as false
         */
        nativeResultInfo.setRetry(false);
        setRetry(nativeResultInfo, resultInfo);

        if (retryInfo.isRetryAllowed()) {
            nativeResultInfo.setRetry(true);
        }

        if (StringUtils.isNotBlank(resultInfo.getResultMsg())) {
            nativeResultInfo.setResultMsg(resultInfo.getResultMsg());
        }

        /*
         * put respMesg in resultInfo as retryMessage
         */
        if (StringUtils.isNotBlank(retryInfo.getRetryMessage())) {
            nativeResultInfo.setResultMsg(retryInfo.getRetryMessage());
        }

        TransactionResponse txnResp = null;
        Map<String, String> paramMap = null;

        try {
            txnResp = createNativeRequestForMerchant(orderDetail, request, retryInfo, resultInfo, false,
                    customCallbackMessage);
            paramMap = new TreeMap<>();
            makeReponseToMerchantEnhancedNative(txnResp, paramMap);
        } catch (Exception e) {
            LOGGER.error("Exception creating ErrorNativeJsonResponse {}", e);
        }

        addRegionalFieldInPTCResponse(nativeResultInfo);
        body.setResultInfo(nativeResultInfo);
        body.setTxnInfo(paramMap);
        if (processTransactionUtil.isRequestOfType(V1_PTC)
                && resultInfo.getResultCodeId().equals(ResponseConstants.NATIVE_RETRY_COUNT_BREACHED.getCode())
                && merchantPreferenceService.isIdempotencyEnabledOnUi(orderDetail.getMid(), false)) {
            if (body.getAdditionalInfo() == null)
                body.setAdditionalInfo(new HashMap<>());
            body.getAdditionalInfo().put(TheiaConstant.ExtraConstants.idempotentTransaction, String.valueOf(true));
        }
        setRetryInfo(body, txnResp);
        if (txnResp != null) {
            body.setCallBackUrl(txnResp.getCallbackUrl());
            setDeepLinkErrorResponse(request, body, txnResp);
        }

        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(body);
        return nativeJsonResponse;
    }

    private void setDeepLinkErrorResponse(HttpServletRequest request, NativeJsonResponseBody body,
            TransactionResponse txnResp) {
        if (isUPIIntentPayment(request)) {
            LOGGER.info("Setting deepLink info for failure response");
            body.setDeepLinkInfo(getDeepLinkInfo("", txnResp.getOrderId(), "", ""));
        }
    }

    private void setRetryInfo(NativeJsonResponseBody body, TransactionResponse txnResponse) {

        if (theiaResponseGenerator.isPaymentRetryInfoEnabled(txnResponse)) {
            body.setRetryInfo(txnResponse.getRetryInfo());
        }
    }

    private Map<String, String> getDeepLinkInfo(String deepLink, String orderId, String cashierRequestId, String transId) {
        Map<String, String> deepLinkInfo = new LinkedHashMap<>();
        deepLinkInfo.put(TheiaConstant.ResponseConstants.DEEP_LINK, StringUtils.isNotBlank(deepLink) ? deepLink : "");
        deepLinkInfo.put(TheiaConstant.ResponseConstants.ORDERID, StringUtils.isNotBlank(orderId) ? orderId : "");
        deepLinkInfo.put(TheiaConstant.ResponseConstants.CASHIER_REQUEST_ID,
                StringUtils.isNotBlank(cashierRequestId) ? cashierRequestId : "");
        deepLinkInfo.put(TheiaConstant.ResponseConstants.TRANS_ID, StringUtils.isNotBlank(transId) ? transId : "");
        return deepLinkInfo;
    }

    private void setRetry(com.paytm.pgplus.response.ResultInfo resultInfo, ResultInfo originalResultInfo) {
        if (originalResultInfo.isRedirect() != null && originalResultInfo.isRedirect()) {
            resultInfo.setRetry(false);
            return;
        }
        if (originalResultInfo.isRedirect() != null && !originalResultInfo.isRedirect()) {
            resultInfo.setRetry(true);
            return;
        }
    }

    public boolean isUPIIntentPayment(HttpServletRequest request) {
        if (EPayMethod.UPI_INTENT.getMethod().equals(
                request.getParameter(TheiaConstant.RequestParams.Native.PAYMENT_MODE))
                || Boolean
                        .valueOf(String.valueOf(request
                                .getAttribute(com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ChannelInfoKeys.IS_DEEP_LINK_REQ)))) {
            return true;
        }
        return false;
    }

    public String getResponseForMandateMerchant(String callBackUrl, ProcessedBmResponse processedMandateResponse,
            ResultInfo resultInfo, PaymentRequestBean requestBean) {

        TransactionResponse merchantMandateResponse = null;
        String mid = processedMandateResponse != null ? processedMandateResponse.getMid() : null;

        if (mid == null) {
            mid = requestBean != null ? requestBean.getMid() : null;
        }

        if (processedMandateResponse != null
                && ff4jUtils.isFeatureEnabledOnMid(mid, TheiaConstant.FF4J.CREATE_MANDATE_CALLBACK_USING_STATUS_API,
                        false)) {
            merchantMandateResponse = prepareMerchantMandateResponse(processedMandateResponse.getSubscriptionId(),
                    processedMandateResponse.getMid(), processedMandateResponse.getMerchantCustId(),
                    processedMandateResponse.getOrderId());
        } else {
            merchantMandateResponse = prepareMerchantMandateResponse(processedMandateResponse, resultInfo, requestBean);
        }

        if (processedMandateResponse != null && StringUtils.isNotBlank(processedMandateResponse.getTxnAmount())) {
            merchantMandateResponse.setTxnAmount(processedMandateResponse.getTxnAmount());
        } else if (requestBean != null && StringUtils.isNotBlank(requestBean.getTxnAmount())) {
            merchantMandateResponse
                    .setTxnAmount(AmountUtils.formatNumberToTwoDecimalPlaces(requestBean.getTxnAmount()));
        }

        // addRegionalFieldInPTCResponse(merchantMandateResponse);

        String website = null != requestBean ? requestBean.getWebsite() : null;
        callBackUrl = getCallbackUrlForCheckoutJs(merchantMandateResponse);
        if (StringUtils.isEmpty(callBackUrl)) {
            // for mandate merchants the response would be sent to the
            // registered url for both the cases either paper or e mandate
            callBackUrl = getCallbackUrl(MERCHANT_URL_INFO_WEBSITE_FOR_BM, mid);
        }

        LOGGER.info("Redirection url for the mid {} is {}", mid, callBackUrl);

        if (StringUtils.isNotBlank(callBackUrl)) {
            StringBuilder builder = new StringBuilder();
            Map<String, String> paramMap = new TreeMap<>();
            theiaResponseGenerator.fillTransactionResponseMap(merchantMandateResponse, builder, paramMap);
            return theiaResponseGenerator.getFinalHtmlResponse(callBackUrl, builder, null, null, null);
        }
        return null;
    }

    public TransactionResponse prepareMerchantMandateResponse(String subscriptionId, String mid, String custId,
            String orderId) {
        TransactionResponse merchantMandateResponse = new TransactionResponse();

        SubscriptionCheckStatusResponseBody status = nativeSubscriptionHelper.getSubscriptionStatus(subscriptionId,
                mid, custId, orderId);

        merchantMandateResponse.setSubsId(status.getSubsId());
        merchantMandateResponse.setMerchantCustId(status.getCustId());
        merchantMandateResponse.setOrderId(status.getOrderId());
        merchantMandateResponse.setMid(status.getMid());
        merchantMandateResponse.setPaymentMode(SubsPaymentMode.BANK_MANDATE.getePayMethodName());
        merchantMandateResponse.setMandateType(status.getSubsPaymentInstDetails().getMandateType());
        merchantMandateResponse.setGateway(PPBL);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        merchantMandateResponse.setTxnDate(simpleDateFormat.format(status.getCreatedDate()));
        merchantMandateResponse.setCallbackUrl(getCallbackUrl(MERCHANT_URL_INFO_WEBSITE_FOR_BM, mid));

        switch (status.getStatus()) {
        case "AUTHORIZED":
            merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
            merchantMandateResponse.setResponseCode(SUBSCRIPTION_SUCCESS_RESPONSE_CODE);
            merchantMandateResponse.setResponseMsg(SUCCESS.toUpperCase());
            break;
        case "AUTHORIZATION_FAILED":
            merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
            merchantMandateResponse.setResponseCode(status.getRespCode());
            merchantMandateResponse.setResponseMsg(status.getRespMsg());
            break;
        case "IN_AUTHORIZATION":
        default:
            merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.PENDING.name());
            merchantMandateResponse.setResponseCode(SUBSCRIPTION_PENDING_RESPONSE_CODE);
            merchantMandateResponse.setResponseMsg(SUBSCRIPTION_PENDING_MESSAGE);
        }

        addRegionalFieldInPTCResponse(merchantMandateResponse);
        LOGGER.info("Response which is to be sent to mandate merchant is {}", merchantMandateResponse);
        return merchantMandateResponse;
    }

    private TransactionResponse prepareMerchantMandateResponse(ProcessedBmResponse processedMandateResponse,
            ResultInfo resultInfo, PaymentRequestBean requestBean) {
        TransactionResponse merchantMandateResponse = new TransactionResponse();
        if (null != processedMandateResponse) {
            merchantMandateResponse.setAcceptedRefNo(processedMandateResponse.getAcceptedRefNo());
            merchantMandateResponse.setAccepted(processedMandateResponse.getIsAccepted());
            merchantMandateResponse.setRejectedBy(processedMandateResponse.getRejectedBy());
            merchantMandateResponse.setSubsId(processedMandateResponse.getSubscriptionId());
            merchantMandateResponse.setMerchantCustId(processedMandateResponse.getMerchantCustId());
            merchantMandateResponse.setOrderId(processedMandateResponse.getOrderId());
            merchantMandateResponse.setMid(processedMandateResponse.getMid());
            merchantMandateResponse.setMandateType(processedMandateResponse.getMandateType());
            merchantMandateResponse.setGateway(StringUtils.isBlank(processedMandateResponse.getGatewayCode()) ? PPBL
                    : processedMandateResponse.getGatewayCode());
        }
        if (null != resultInfo) {
            merchantMandateResponse.setResponseCode(resultInfo.getResultCode());
            merchantMandateResponse.setResponseMsg(resultInfo.getResultMsg());
            if (processedMandateResponse != null && processedMandateResponse.isAoa()) {
                if (processedMandateResponse.getIsAccepted())
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
                else
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
            } else {
                switch (resultInfo.getResultStatus()) {
                case "S":
                case "A":
                case "SUCCESS":
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_SUCCESS.name());
                    break;
                case "F":
                case "U":
                case "FAILURE":
                    merchantMandateResponse.setTransactionStatus(ExternalTransactionStatus.TXN_FAILURE.name());
                    break;
                default:
                    merchantMandateResponse.setTransactionStatus(resultInfo.getResultStatus());
                }
            }
        }

        /*
         * data from payment request bean would be populated only in case
         * processed mandate response is null else the same fields would be
         * populated from processed mandate response
         */
        if (null != requestBean && null == processedMandateResponse) {
            merchantMandateResponse.setOrderId(requestBean.getOrderId());
            merchantMandateResponse.setMerchantCustId(requestBean.getCustId());
            merchantMandateResponse.setMid(requestBean.getMid());
            merchantMandateResponse.setMandateType(requestBean.getMandateType());
            merchantMandateResponse.setSubsId(requestBean.getSubscriptionID());
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        merchantMandateResponse.setTxnDate(simpleDateFormat.format(new Date()));
        merchantMandateResponse.setPaymentMode(SubsPaymentMode.BANK_MANDATE.getePayMethodName());
        addRegionalFieldInPTCResponse(merchantMandateResponse);
        LOGGER.info("Response which is to be sent to mandate merchant is {}", merchantMandateResponse);
        return merchantMandateResponse;
    }

    private void makeReponseToEncryptedMerchantEnhancedNative(String errorHTML, Map<String, String> paramMap) {
        Document doc = Jsoup.parse(errorHTML);
        Element element = doc.select("FORM").first();
        Iterator<Element> it = element.children().iterator();
        while (it.hasNext()) {
            Element inputTag = it.next();
            if (StringUtils.isNotBlank(inputTag.attr("name"))) {
                paramMap.put(inputTag.attr("name"), inputTag.attr("value"));
            }
        }
    }

    public NativeJsonResponse getErrorNativeJsonResponseForBM(String callbackUrl,
            ProcessedBmResponse processedMandateResponse, ResultInfo resultInfo, PaymentRequestBean requestBean) {

        NativeJsonResponseBody body = new NativeJsonResponseBody();
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();

        com.paytm.pgplus.response.ResultInfo nativeResultInfo = new com.paytm.pgplus.response.ResultInfo(
                resultInfo.getResultStatus(), resultInfo.getResultCodeId(), resultInfo.getResultMsg());

        /*
         * by default setting retry as false
         */
        nativeResultInfo.setRetry(false);
        setRetry(nativeResultInfo, resultInfo);
        NativeRetryInfo retryInfo = new NativeRetryInfo(false, "Retry Not Allowed for Mandates");
        if (retryInfo.isRetryAllowed()) {
            nativeResultInfo.setRetry(true);
        }

        if (StringUtils.isNotBlank(resultInfo.getResultMsg())) {
            nativeResultInfo.setResultMsg(resultInfo.getResultMsg());
        }

        /*
         * put respMesg in resultInfo as retryMessage
         */
        if (StringUtils.isNotBlank(retryInfo.getRetryMessage())) {
            nativeResultInfo.setResultMsg(retryInfo.getRetryMessage());
        }

        TransactionResponse txnResp = null;
        Map<String, String> paramMap = null;
        String mid = requestBean.getMid();
        try {
            txnResp = prepareMerchantMandateResponse(processedMandateResponse, resultInfo, requestBean);
            paramMap = new TreeMap<>();
            if (StringUtils.isEmpty(callbackUrl)) {
                callbackUrl = getCallbackUrl(MERCHANT_URL_INFO_WEBSITE_FOR_BM, mid);
            }
            if (StringUtils.isNotBlank(callbackUrl)) {
                makeReponseToMerchantEnhancedNative(txnResp, paramMap);
            }
        } catch (Exception e) {
            LOGGER.error("Exception creating ErrorNativeJsonResponse {}", e);
        }

        body.setResultInfo(nativeResultInfo);
        body.setTxnInfo(paramMap);
        setRetryInfo(body, txnResp);
        body.setCallBackUrl(callbackUrl);

        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(body);
        return nativeJsonResponse;
    }

    public void addRegionalFieldInPTCResponse(Object response) {
        if (processTransactionUtil.isRequestOfType(PTR_URL)) {
            localeFieldAspect.addLocaleFieldsInObject(response, PTR_URL);
        } else if (processTransactionUtil.isRequestOfType(V1_PTC)) {
            localeFieldAspect.addLocaleFieldsInObject(response, V1_PTC);
        } else if (processTransactionUtil.isRequestOfType(V1_DIRECT_BANK_REQUEST)) {
            localeFieldAspect.addLocaleFieldsInObject(response, V1_DIRECT_BANK_REQUEST);
        }
    }

    private String errorCallBackForDccPayment(HttpServletRequest request, String callbackurl, NativeRetryInfo retryInfo) {
        if (request.getAttribute("NATIVE_ENHANCED_FLOW") != null
                && BooleanUtils.toBoolean(request.getAttribute("NATIVE_ENHANCED_FLOW").toString())) {

            if (retryInfo.isRetryAllowed()) {
                callbackurl = ConfigurationUtil.getProperty(THEIA_BASE_URL) + NATIVE_APP_INVOKE_URL;
            }
        }
        return callbackurl;

    }

    public void encryptedResponseJson(String mid, Map<String, String> txnInfo, TransactionResponse transactionResponse,
            boolean isAES256Encrypted, boolean encRequestEnabled) {
        txnInfo.put(TheiaConstant.ResponseConstants.M_ID, mid);
        try {
            if (!isAES256Encrypted && encRequestEnabled) {
                String encParams = theiaResponseGeneratorHelper.encryptedResponse(transactionResponse,
                        isAES256Encrypted, true).toString();
                int pipeLastIndex = encParams.lastIndexOf("|");
                if (pipeLastIndex != -1) {
                    String checksum = encParams.substring(pipeLastIndex + 1);
                    encParams = encParams.substring(0, pipeLastIndex);
                    if (StringUtils.isNotBlank(checksum))
                        txnInfo.put(TheiaConstant.ResponseConstants.CHECKSUM, checksum);
                }
                txnInfo.put(TheiaConstant.ResponseConstants.ENC_PARAMS, encParams);
            } else {
                txnInfo.put(TheiaConstant.ResponseConstants.ENC_PARAMS,
                        theiaResponseGeneratorHelper.encryptedResponse(transactionResponse, isAES256Encrypted, true)
                                .toString());
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while encrypting response  ", e);
        }
    }

    public String getCallbackUrlForCheckoutJs(TransactionResponse merchantMandateResponse) {
        String callbackUrl = null;
        if (MandateMode.E_MANDATE.name().equals(merchantMandateResponse.getMandateType())
                && StringUtils.isNotBlank(merchantMandateResponse.getMid())
                && StringUtils.isNotBlank(merchantMandateResponse.getOrderId())) {
            String midOrderIdKey = nativeSessionUtil.getMidOrderIdKeyForTxnTokenWorkflow(
                    merchantMandateResponse.getMid(), merchantMandateResponse.getOrderId());
            String workflow = nativeSessionUtil.getTxnTokenAndWorkflowOnMidOrderId(midOrderIdKey, Native.WORKFLOW);
            if (StringUtils.isNotBlank(workflow) && Native.CHECKOUT.equals(workflow)) {
                LOGGER.info("Setting dummy callback Url for Mandate CheckOut Js flow");
                callbackUrl = com.paytm.pgplus.common.config.ConfigurationUtil
                        .getProperty(CHECKOUT_JS_STATIC_CALLBACK_URL)
                        + merchantMandateResponse.getOrderId()
                        + "&MID="
                        + merchantMandateResponse.getMid();
            }
        }
        return callbackUrl;
    }
}
