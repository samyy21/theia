package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.biz.core.risk.RiskConstants;
import com.paytm.pgplus.biz.core.risk.RiskOAuthValidatedData;
import com.paytm.pgplus.biz.core.risk.RiskVerifierPayload;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.enums.UIMicroserviceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.risk.models.response.RiskVerifierDoViewResponseBody;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankFormData;
import com.paytm.pgplus.theia.nativ.model.enhancenative.BankRedirectionDetail;
import com.paytm.pgplus.theia.nativ.model.risk.RiskVerificationPageData;
import com.paytm.pgplus.theia.nativ.model.risk.request.DoViewResponseBody;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.OAUTH_BASE_URL;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_UI_MICROSERVICE_ENHANCED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_UI_MICROSERVICE_RISK;

@Component
public class RiskVerificationUtil {

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("merchantResponseService")
    private MerchantResponseService merchantResponseService;

    @Autowired
    private EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    private UIMicroserviceHelper uiMicroserviceHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskVerificationUtil.class);

    public PageDetailsResponse handleRiskVerificationForEnhance(PaymentRequestBean requestData, String transId) {
        StringBuilder sb = new StringBuilder();
        sb.append("?mid=").append(requestData.getMid());
        sb.append("&orderId=").append(requestData.getOrderId());
        BankFormData bankFormData = new BankFormData();
        BankRedirectionDetail bankRedirectionDetail = new BankRedirectionDetail();
        bankRedirectionDetail.setMethod("POST");
        Map<String, String> content = new HashMap<>();
        content.put("token", transId);
        bankRedirectionDetail.setContent(content);
        String callbackUrl = ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL)
                + RiskConstants.SHOW_VERIFICATION_PAGE_URL + sb.toString();
        bankRedirectionDetail.setCallbackUrl(callbackUrl);
        bankFormData.setHead(new ResponseHeader());
        bankFormData.setBody(bankRedirectionDetail);
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setRiskVerificationRequired(true);
        pageDetailsResponse.setTransId(transId);
        try {
            pageDetailsResponse.setS2sResponse(JsonMapper.mapObjectToJson(bankFormData));
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting  risk obj to json : {}", e);
            pageDetailsResponse.setSuccessfullyProcessed(false);
        }
        return pageDetailsResponse;
    }

    public PageDetailsResponse handleRiskVerificationForRedirection(PaymentRequestBean requestData, String transId) {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        try {
            String callbackUrl = ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL) + RiskConstants.PTC_V1_URL;
            String htmlPage = getRiskVerifierHtmlPage(transId, requestData.getMid(), requestData.getOrderId(),
                    callbackUrl);
            if (StringUtils.isBlank(htmlPage)) {
                throw new TheiaServiceException("Risk Verification html page not present!");
            }
            pageDetailsResponse.setHtmlPage(htmlPage);
            pageDetailsResponse.setRiskVerificationRequired(true);
            pageDetailsResponse.setTransId(transId);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting  risk obj to json : {}", e);
            pageDetailsResponse.setSuccessfullyProcessed(false);
            return pageDetailsResponse;
        }
        return pageDetailsResponse;
    }

    public PageDetailsResponse handleRiskVerificationForNativePlus(PaymentRequestBean requestData, String transId) {
        StringBuilder sb = new StringBuilder();
        sb.append("?mid=").append(requestData.getMid());
        sb.append("&orderId=").append(requestData.getOrderId());
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
        NativeJsonResponseBody nativeJsonResponseBody = new NativeJsonResponseBody();
        nativeJsonResponseBody.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
        String actionUrl = ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL)
                + RiskConstants.SHOW_VERIFICATION_PAGE_URL + sb.toString();
        BankForm bankForm = new BankForm();
        bankForm.setPageType("redirect");
        FormDetail formDetail = new FormDetail();
        formDetail.setActionUrl(actionUrl);
        formDetail.setMethod("POST");
        formDetail.setType("redirect");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        formDetail.setHeaders(headers);
        Map<String, String> content = new HashMap<>();
        content.put("token", transId);
        formDetail.setContent(content);
        bankForm.setRedirectForm(formDetail);
        nativeJsonResponseBody.setBankForm(bankForm);
        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(nativeJsonResponseBody);
        String jsonResponse = "{}";
        try {
            jsonResponse = JsonMapper.mapObjectToJson(nativeJsonResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Failed to map NativeJson Risk object to json", e);
        }
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setS2sResponse(jsonResponse);
        pageDetailsResponse.setRiskVerificationRequired(true);
        pageDetailsResponse.setTransId(transId);
        return pageDetailsResponse;
    }

    public String getRiskVerifierHtmlPage(String transId, String mid, String orderId, String callbackUrl)
            throws FacadeCheckedException {

        RiskVerifierPayload riskVerifierPayload = nativeSessionUtil.getRiskVerificationData(transId);
        return getRiskVerifierHtmlPage(riskVerifierPayload, mid, orderId, transId, callbackUrl);
    }

    public String getRiskVerifierHtmlPage(RiskVerifierPayload riskVerifierPayload, String mid, String orderId,
            String transId, String callbackUrl) throws FacadeCheckedException {

        if (null == riskVerifierPayload) {
            LOGGER.error("riskVerifierPayload not present in cache");
            throw SessionExpiredException.getException();
        }

        RiskVerifierDoViewResponseBody body = riskVerifierPayload.getRiskVerifierDoViewResponseBody();
        DoViewResponseBody doViewResponseBody = new DoViewResponseBody(body.getMethod(), body.getRenderData());
        filterRenderData(doViewResponseBody);
        String cancelTransactionUrl = ConfigurationUtil.getProperty(RiskConstants.THEIA_BASE_URL)
                + RiskConstants.CANCEL_TXN_URL;
        RiskVerificationPageData riskVerificationPageData = new RiskVerificationPageData(transId, mid, orderId,
                doViewResponseBody, callbackUrl, cancelTransactionUrl);
        riskVerificationPageData.setTxnToken(riskVerifierPayload.getTxnToken());
        populatePasswordMethodData(riskVerificationPageData);
        String jsonRiskVerificationPageData = JsonMapper.mapObjectToJson(riskVerificationPageData);
        String uiJsonRiskVerificationPageData = jsonRiskVerificationPageData;
        jsonRiskVerificationPageData = jsonRiskVerificationPageData.replace("'", "\\'");

        String htmlPage = null;

        // check for ff4j and get HTML from ui-microservice

        UIMicroserviceRequest uiMicroserviceRequest = new UIMicroserviceRequest(uiJsonRiskVerificationPageData,
                UIMicroserviceUrl.RISK_VERIFICATION_URL);
        UIMicroserviceResponse uiMicroserviceResponse = uiMicroserviceHelper.getHtmlPageFromUI(uiMicroserviceRequest,
                FEATURE_UI_MICROSERVICE_RISK, mid);
        htmlPage = uiMicroserviceResponse.getHtmlPage();

        if (StringUtils.isBlank(htmlPage)) {

            htmlPage = ConfigurationUtil.getRiskVerificationPage();

            if (StringUtils.isNotBlank(htmlPage)) {
                htmlPage = htmlPage.replace(RiskConstants.PUSH_APP_DATA, jsonRiskVerificationPageData);
            } else {
                throw new TheiaServiceException("Risk Verification html page not present!");
            }
        }
        return htmlPage;
    }

    private void populatePasswordMethodData(RiskVerificationPageData riskVerificationPageData) {
        if (RiskConstants.RiskVerifyMethod.PASSWORD.equalsIgnoreCase(riskVerificationPageData.getFirstDoviewResponse()
                .getMethod())
                && riskVerificationPageData.getFirstDoviewResponse().getRenderData()
                        .containsKey(RiskConstants.RISK_STATE_PARAM)) {
            riskVerificationPageData.setOpenIframe(true);
            riskVerificationPageData.setIframeUrl(ConfigurationUtil.getProperty(OAUTH_BASE_URL)
                    + RiskConstants.OAUTH_PASSWORD_VERIFY_URL);
        }
    }

    public void sendFailureResponseToMerchant(PaymentRequestBean paymentRequestData, HttpServletResponse response)
            throws IOException {
        LOGGER.info("Risk_not_verified request. Failing!");
        String html = merchantResponseService.processMerchantFailResponse(paymentRequestData,
                ResponseConstants.MERCHANT_FAILURE_RESPONSE);
        response.getOutputStream().print(html);
        response.setContentType("text/html");
        return;
    }

    private void filterRenderData(DoViewResponseBody doViewResponseBody) {
        doViewResponseBody.getRenderData().remove(RiskConstants.ACCOUNT_ENCRYPT_PUB_KEY);
        doViewResponseBody.getRenderData().remove(RiskConstants.ACCOUNT_ENCRYPT_SALT);
    }

    public PageDetailsResponse handleRiskVerifiedEnhanceRetry(PaymentRequestBean requestData,
            EnhancedCashierPage enhancedCashierPage) {
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        try {
            if (enhancedCashierPage.getRetryData() != null) {
                enhancedCashierPage.getRetryData().setRetryErrorMsg(requestData.getNativeRetryErrorMessage());
            }
            String channelId = requestData.getChannelId();
            String enhanceJson = JsonMapper.mapObjectToJson(enhancedCashierPage);

            String htmlPage = null;

            // try fetching htmlPage from uiMicroservice and ask about encoded
            // flag
            UIMicroserviceRequest uiMicroserviceRequest = new UIMicroserviceRequest(enhanceJson,
                    requestData.getChannelId(), "false", UIMicroserviceUrl.ENHANCED_CASHIER_URL);
            UIMicroserviceResponse uiMicroserviceResponse = uiMicroserviceHelper.getHtmlPageFromUI(
                    uiMicroserviceRequest, FEATURE_UI_MICROSERVICE_ENHANCED, requestData.getMid());
            htmlPage = uiMicroserviceResponse.getHtmlPage();

            if (StringUtils.isBlank(htmlPage)) {

                htmlPage = enhancedCashierPageServiceHelper.getEnhancedCashierTheme(channelId);
                if (htmlPage != null) {
                    htmlPage = htmlPage
                            .replace(
                                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.REPLACE_STRING,
                                    enhanceJson);
                } else {
                    throw new TheiaServiceException("Unable to fetch enhance template");
                }
            }

            pageDetailsResponse.setHtmlPage(htmlPage);
            pageDetailsResponse.setSuccessfullyProcessed(false);
            return pageDetailsResponse;
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting  enhance payload to json : {}", e);
            pageDetailsResponse.setSuccessfullyProcessed(false);
        }
        return pageDetailsResponse;
    }

    public boolean isDoViewRetryAllowed(String token, String resultCode) {
        if (com.paytm.pgplus.common.enums.ResultCode.VALIDATE_CODE_SEND_TIMES_LIMIT.getCode().equals(resultCode)
                || com.paytm.pgplus.common.enums.ResultCode.VALIDATE_CODE_SEND_FAILURE.getCode().equals(resultCode)) {
            return false;
        }
        String key = RiskConstants.DO_VIEW_RETRY_COUNT + token;
        return checkforRetry(key);
    }

    public boolean isDoVerifyRetryAllowed(String token) {
        String key = RiskConstants.DO_VERIFY_RETRY_COUNT + token;
        return checkforRetry(key);
    }

    private boolean checkforRetry(String key) {
        Integer currentRetryCount = (Integer) nativeSessionUtil.getKey(key);
        if (currentRetryCount >= 3) {
            LOGGER.info("DoView Retry count breached!");
            return false;
        }
        nativeSessionUtil.setKey(key, currentRetryCount + 1, 600);
        return true;
    }

    public String getRiskVerifierPayloadKey(String transId) {
        return RiskConstants.DO_VIEW_CACHE_KEY_PREFIX + transId;
    }

    public String getSecurityIdFromPayReuest(HttpServletRequest request) {
        String token = request.getParameter(RiskConstants.TOKEN);
        if (StringUtils.isBlank(token)) {
            throw new TheiaServiceException("token blank for risk-verified request");
        }
        Object securityId = nativeSessionUtil.getKey(RiskConstants.IS_RISK_VERIFIED_PREFIX + token);
        if (securityId == null) {
            return null;
        }
        return (String) securityId;
    }

    public void setCashierRequestInCache(CashierRequest cashierRequest) {
        String key = RiskConstants.RISK_VERIFICATION_KEY_PREFIX + cashierRequest.getAcquirementId();
        nativeSessionUtil.setKey(key, cashierRequest, 900);
    }

    public CashierRequest getCashierRequestFromCache(String token) {
        String key = RiskConstants.RISK_VERIFICATION_KEY_PREFIX + token;
        return (CashierRequest) nativeSessionUtil.getKey(key);
    }

    public String getRiskVerifierRedirectionPage(RiskOAuthValidatedData riskOAuthValidatedData) {
        String htmlPage = ConfigurationUtil.getRiskVerifierRedirectionPage();
        try {
            String jsonRiskVerificationPageData = JsonMapper.mapObjectToJson(riskOAuthValidatedData);
            if (StringUtils.isNotBlank(htmlPage)) {
                htmlPage = htmlPage.replace(RiskConstants.PUSH_APP_DATA, jsonRiskVerificationPageData);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while converting  risk validated obj to json : {}", e);
        }
        return htmlPage;
    }
}
