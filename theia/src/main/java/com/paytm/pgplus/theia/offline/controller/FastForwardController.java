package com.paytm.pgplus.theia.offline.controller;

import com.paytm.pgplus.biz.exception.BizMerchantVelocityBreachedException;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.enums.Channel;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.facade.linkService.services.ILinkService;
import com.paytm.pgplus.facade.merchantlimit.exceptions.MerchantLimitBreachedException;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.controllers.helper.ProcessTransactionControllerHelper;
import com.paytm.pgplus.theia.exceptions.TheiaControllerException;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.annotations.OfflineControllerAdvice;
import com.paytm.pgplus.theia.offline.constants.PropertyConstrant;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.*;
import com.paytm.pgplus.theia.offline.model.request.FastForwardRequest;
import com.paytm.pgplus.theia.offline.model.request.FastForwardResponse;
import com.paytm.pgplus.theia.offline.services.impl.FastForwardServiceHelper;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import com.paytm.pgplus.theia.offline.validation.FastForwardRequestValidator;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.IJsonResponsePaymentService;
import com.paytm.pgplus.theia.utils.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.paytm.pgplus.payloadvault.merchant.status.utils.ConstantsUtil.PARAMETERS.REQUEST.SSOTOKEN;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.CREATE_NON_QR_DEEP_LINK;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.FAST_FORWARD_API_DUPLICATE_CHECK;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SET_LINK_CALLBACKURL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS;

@OfflineControllerAdvice
@RestController
@RequestMapping("HANDLER_IVR")
public class FastForwardController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastForwardController.class);
    private static final Logger EVENT_LOGGER = LoggerFactory.getLogger("EVENT_LOGGER");

    @Autowired
    @Qualifier("autoDebitService")
    IJsonResponsePaymentService autoDebitService;

    @Autowired
    @Qualifier("fastForwardrequestValidator")
    FastForwardRequestValidator fastForwardRequestValidator;

    @Autowired
    @Qualifier("dynamicQrFastForwardService")
    IJsonResponsePaymentService dynamicQrFastForwardService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private ILinkService linkService;

    @Autowired
    private PRNUtils prnUtils;

    @Autowired
    private MerchantDataUtil merchantDataUtil;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    private DynamicQRUtil dynamicQRUtil;

    @Autowired
    private LinkPaymentUtil linkPaymentUtil;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    @Autowired
    @Qualifier("processTransactionControllerHelper")
    ProcessTransactionControllerHelper processTransactionControllerHelper;

    @Autowired
    FastForwardServiceHelper fastForwardServiceHelper;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @RequestMapping(value = "/CLW_APP_PAY/APP", method = RequestMethod.POST)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String processTransaction(final HttpServletRequest request, @RequestBody FastForwardRequest requestData) {
        final long startTime = System.currentTimeMillis();

        if (requestData == null || requestData.getHead() == null) {
            throw new TheiaControllerException("Fast Forward request can't be null");
        }

        try {
            String additionalInfo = (String) requestData.getBody().getExtendInfo()
                    .get(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO);
            if (StringUtils.isNotBlank(additionalInfo)) {
                int indexOfTagLine = additionalInfo.indexOf(TheiaConstant.ExtendedInfoKeys.TAG_LINE);
                int indexOfPipe = additionalInfo.indexOf("|", indexOfTagLine);
                int finalIndexOfTagLine = indexOfTagLine == 0 ? 0 : indexOfTagLine;
                String finalAdditionalInfo = null;
                if (indexOfTagLine == -1) {
                    finalAdditionalInfo = additionalInfo;
                } else if (indexOfPipe == -1) {
                    finalAdditionalInfo = additionalInfo.substring(0, (finalIndexOfTagLine - 1));
                } else {
                    finalAdditionalInfo = (additionalInfo.substring(0, (finalIndexOfTagLine))).trim()
                            + (additionalInfo.substring((indexOfPipe + 1), additionalInfo.length()));
                }

                /** Below method call is for Sounbox */
                finalAdditionalInfo = processTransactionControllerHelper
                        .getAdditionalInfoRequestTypeForSoundBox(finalAdditionalInfo);

                if (null != finalAdditionalInfo) {
                    requestData.getBody().getExtendInfo()
                            .put(TheiaConstant.ExtendedInfoKeys.ADDITIONAL_INFO, finalAdditionalInfo);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error orrcured while removing tagLine from additionalInfo : {}", e.getMessage());
        }
        String orderId = requestData.getBody() != null ? requestData.getBody().getOrderId() : null;
        OfflinePaymentUtils.setMDC(requestData.getHead().getMid(), orderId, requestData.getHead().getRequestId());

        WorkFlowResponseBean workFlowResponseBean = null;
        if (requestData.getBody().getTwoFAConfig() != null) {
            requestData.getBody().getTwoFAConfig().setFastForwardRequest(true);
        }
        LOGGER.info("FastForwardRequest : {}", requestData);
        nativePaymentUtil.logNativeRequests(requestData.toString());
        boolean isRequestFromAppCache = requestData.getBody() != null
                && StringUtils.isBlank(requestData.getBody().getOrderId());
        processTransactionUtil.pushPaymentEvent(isRequestFromAppCache ? EventNameEnum.OFFLINE_PAYMENT_REQUEST_CACHED
                : EventNameEnum.OFFLINE_PAYMENT_REQUEST, requestData.getBody().getPaymentMode());
        try {

            OfflinePaymentUtils.setRequestHeader(requestData.getHead());
            // Validating request data
            String errorMessage = fastForwardRequestValidator.validate(requestData);
            if (StringUtils.isNotBlank(errorMessage)) {
                return fastForwardServiceHelper.generateResponseForExceptionCases(requestData, errorMessage, null);
            }
            handleDuplicateRequest(requestData);
            // If all validations are successful
            PaymentRequestBean paymentRequestBean = fastForwardServiceHelper.preparePaymentRequestBean(requestData,
                    merchantDataUtil);

            paymentRequestBean.setCreateNonQRDeepLink(request.getParameter(CREATE_NON_QR_DEEP_LINK));

            if (StringUtils.isNotEmpty(paymentRequestBean.getLinkId())
                    || StringUtils.isNotEmpty(paymentRequestBean.getInvoiceId())) {
                EventUtils.pushLinkBasedPaymentInitiatedEvent(paymentRequestBean, Channel.APP.getName());

                linkPaymentUtil.getLinkDetailResponse(paymentRequestBean, true);

                if (Boolean.TRUE.toString().equals(ConfigurationUtil.getProperty(SET_LINK_CALLBACKURL))) {
                    String errorMsg = linkPaymentUtil.validateLinkAndSetCallBackURL(paymentRequestBean, true);
                    if (StringUtils.isNotBlank(errorMsg)) {
                        return fastForwardServiceHelper.generateResponseForExceptionCases(requestData, errorMsg,
                                paymentRequestBean);
                    }

                }
            }
            paymentRequestBean.setRequest(request);
            paymentRequestBean.setSessionRequired(false);
            paymentRequestBean.setOfflineFastForwardRequest(true);
            boolean isQREnabled = merchantPreferenceService.isQRCodePaymentEnabled(paymentRequestBean.getMid());
            boolean dynamicQR2FAEnabledWithPCF = merchantPreferenceService
                    .isDynamicQR2FAEnabledWithPCF(paymentRequestBean.getMid());
            boolean isQRWith2FAEnabled = merchantPreferenceService.isDynamicQR2FAEnabled(paymentRequestBean.getMid());
            boolean isDynamicQREdcRequest = dynamicQRUtil.isDynamicQREdcRequest(paymentRequestBean);
            boolean isOrderAlreadyCreated = dynamicQRUtil.isOrderAlreadyCreated(paymentRequestBean);
            boolean isAoaDqrOrder = dynamicQRUtil.isAoaDqrOrder(paymentRequestBean);
            LOGGER.info(
                    "Found isQREnabled => {} , isQRWith2FAEnabled => {} , isDynamicQREdcRequest => {} isOrderAlreadyCreated => {}",
                    isQREnabled, isQRWith2FAEnabled, isDynamicQREdcRequest, isOrderAlreadyCreated);

            boolean isDefaultFFWebsiteEnabled = merchantPreferenceService.isDefaultFFWebsiteEnabled(paymentRequestBean
                    .getMid());

            if (isDefaultFFWebsiteEnabled) {
                paymentRequestBean.setWebsite(TheiaConstant.ExtraConstants.DEFAULT_WEBSITE_FF);
            }

            if ((isQREnabled || isQRWith2FAEnabled || isDynamicQREdcRequest || dynamicQR2FAEnabledWithPCF
                    || isOrderAlreadyCreated || isAoaDqrOrder)
                    && !ERequestType.LINK_BASED_PAYMENT.getType().equals(paymentRequestBean.getRequestType())
                    && !ERequestType.LINK_BASED_PAYMENT_INVOICE.getType().equals(paymentRequestBean.getRequestType())) {

                if (isQRWith2FAEnabled || dynamicQR2FAEnabledWithPCF) {
                    LOGGER.info("Changing Request Type to {}", ERequestType.DYNAMIC_QR_2FA.getType());
                    paymentRequestBean.setRequestType(ERequestType.DYNAMIC_QR_2FA.getType());
                } else {
                    // LOGGER.info("Changing Request Type to {}",
                    // ERequestType.DYNAMIC_QR.getType());
                    paymentRequestBean.setRequestType(ERequestType.DYNAMIC_QR.getType());
                    String website = dynamicQRUtil.getWebsiteName(paymentRequestBean);
                    if (StringUtils.isNotBlank(website)) {
                        LOGGER.info("Changing Website type to {}", website);
                        paymentRequestBean.setWebsite(website);
                    }
                    String callbackUrl = dynamicQRUtil.getCallbackUrl(paymentRequestBean);
                    if (StringUtils.isNotBlank(callbackUrl)) {
                        LOGGER.info("Changing CallbckUrl type to {}", callbackUrl);
                        paymentRequestBean.setCallbackUrl(callbackUrl);
                    }
                    String peonUrl = dynamicQRUtil.getPeonUrl(paymentRequestBean);
                    if (StringUtils.isNotBlank(peonUrl)) {
                        LOGGER.info("Changing peonUrl type to {}", callbackUrl);
                        paymentRequestBean.setPeonURL(peonUrl);
                    }
                }
                // for offline requests, excluding all dynamic QR cases
                paymentRequestBean.setScanAndPayFlow(false);
                fastForwardServiceHelper.checkAndSetIfScanAndPayFlow(paymentRequestBean);
                workFlowResponseBean = dynamicQrFastForwardService.processPaymentRequest(paymentRequestBean);

            } else {
                workFlowResponseBean = autoDebitService.processPaymentRequest(paymentRequestBean);
            }

            // Generating response
            FastForwardResponse fastForwardResponse = fastForwardServiceHelper.generateFastForwardResponseData(
                    paymentRequestBean, workFlowResponseBean, requestData);
            if (StringUtils.isNotEmpty(paymentRequestBean.getLinkId())
                    || StringUtils.isNotEmpty(paymentRequestBean.getInvoiceId())) {
                String mercUniqRef = null;
                if (workFlowResponseBean != null && workFlowResponseBean.getQueryTransactionStatus() != null
                        && workFlowResponseBean.getQueryTransactionStatus().getExtendInfo() != null) {
                    mercUniqRef = workFlowResponseBean.getQueryTransactionStatus().getExtendInfo()
                            .get(TheiaConstant.ExtendedInfoKeys.MERCH_UNQ_REF);
                }
                TransactionResponse response = new TransactionResponse();
                if (fastForwardResponse.getBody() != null && fastForwardResponse.getBody().getResultInfo() != null) {
                    response.setTransactionStatus(fastForwardResponse.getBody().getResultInfo().getResultStatus());
                }
                response.setRequestType(paymentRequestBean.getRequestType());
                response.setMid(paymentRequestBean.getMid());
                response.setOrderId(paymentRequestBean.getOrderId());
                response.setPaymentMode(fastForwardResponse.getBody().getPaymentMode());
                response.setBankName(fastForwardResponse.getBody().getBankName());
                response.setTxnAmount(fastForwardResponse.getBody().getTxnAmount());
                response.setTxnId(fastForwardResponse.getBody().getTxnId());
                response.setMerchUniqueReference(mercUniqRef);
                response.setResponseCode(fastForwardResponse.getBody().getResponseCode());
                response.setResponseMsg(fastForwardResponse.getBody().getResponseMessage());
                response.setRetryInfo(fastForwardResponse.getBody().getRetryInfo());
                EventUtils.pushLinkBasedPaymentCompletedEvent(response, paymentRequestBean);
            }
            String paytmMID = requestData.getHead().getMid();

            if ("TXN_SUCCESS".equals(fastForwardResponse.getBody().getResultInfo().getResultStatus())
                    && prnUtils.checkIfPRNEnabled(paytmMID)) {
                fastForwardResponse.getBody().setPrn(prnUtils.fetchPRN(paytmMID, workFlowResponseBean.getTransID()));
            }

            String responseStr = FastForwardServiceHelper.convertObjectToJson(fastForwardResponse);
            LOGGER.info("FastForwardResponse data : {}", MaskingUtil.maskObject(responseStr, SSOTOKEN, 6, 4));
            EVENT_LOGGER.info("FastForwardResponse data : {}", MaskingUtil.maskObject(responseStr, SSOTOKEN, 6, 4));
            return responseStr;

        } catch (OrderIdGenerationException e) {
            LOGGER.error("OrderIdGenerationException - {}", e);
            throw e;
        } catch (DuplicatePaymentRequestException e) {
            LOGGER.error("DuplicatePaymentRequestException in fastforward : {}", e.getMessage());
            throw e;
        } catch (BizMerchantVelocityBreachedException e) {
            LOGGER.error("BizMerchantVelocityBreachedException : {}", e.getMessage());
            throw MerchantVelocityBreachException.getException(e.getLimitType(), e.getLimitDuration());
        } catch (MerchantLimitBreachedException e) {
            LOGGER.error("MerchantLimitBreachedException : {} {}", e.getLimitDuration(), e.getLimitType());
            throw MerchantVelocityBreachException.getException(e.getLimitType(), e.getLimitDuration());
        } catch (NativeFlowException nfe) {
            LOGGER.error("NativeFlowException in /CLW_APP_PAY/APP: ", nfe);
            throw nfe;
        } catch (PaymentRequestProcessingException e) {
            throw e;
        } catch (PaymentRequestQrException e) {
            throw PaymentRequestQrException.getException(e.getResponseConstants());
        } catch (BaseException e) {
            if (e.getResultInfo() != null && e.getResultInfo().getResultCode() != null
                    && e.getResultInfo().getResultCode().equals(ResultCode.INVALID_SSO_TOKEN_FF.getCode())) {
                LOGGER.error("InvalidSSO : {} ", e.getMessage());
                throw PaymentRequestProcessingException.getException(ResultCode.INVALID_SSO_TOKEN_FF);
            } else {
                LOGGER.error("MERCHANT_VELOCITY_LIMIT_BREACH error : {} ", e.getMessage());
                throw new TheiaControllerException("MERCHANT_VELOCITY_LIMIT_BREACH");
            }
        } catch (final Exception e) {
            LOGGER.error("SYSTEM_ERROR :", e);
        } finally {
            LOGGER.info("Total time taken for FastForwardController API : {}ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
        return fastForwardServiceHelper.generateResponseForExceptionCases(requestData, null, null);
    }

    private void handleDuplicateRequest(FastForwardRequest requestData) {
        try {
            String amount = requestData.getBody().getTxnAmount();
            String ssoToken = requestData.getHead().getToken();
            String mid = requestData.getHead().getMid();
            String requestIdentifier = getKey(mid, ssoToken, amount);
            if (!setEntryIfNotExist(requestIdentifier)) {
                throw DuplicatePaymentRequestException.getException();
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Exception in deduplication of request", e);
        }

    }

    private boolean setEntryIfNotExist(String requestIdentifier) {
        String ttl = ConfigurationUtil.getProperty(PropertyConstrant.OFFLINE_FASTFORWARD_DEDUPLICATE_INTERVAL, "5");
        return setInRedisIfNotExist(requestIdentifier, Long.parseLong(ttl));
    }

    private boolean setInRedisIfNotExist(String key, long ttlInSeconds) {
        boolean present = theiaSessionRedisUtil.setnx(key, true, ttlInSeconds);

        if (!ff4jUtils.isFeatureEnabledOnMid(FAST_FORWARD_API_DUPLICATE_CHECK,
                THEIA_STATIC_REDIS_MIGRATION_SESSION_REDIS, false)) {
            LOGGER.info("operation on static redis, {}", FAST_FORWARD_API_DUPLICATE_CHECK);
            present = theiaTransactionalRedisUtil.setnx(key, true, ttlInSeconds);
        }

        return present;
    }

    private String getKey(String mid, String ssoToken, String amount) {
        return new StringBuilder().append(mid).append("_").append(amount).append("_").append(ssoToken).toString();
    }
}
