package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.common.bankForm.model.BankForm;
import com.paytm.pgplus.common.bankForm.model.FormDetail;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.enums.UIMicroserviceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.models.GvConsentPagePayload;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.models.NativeJsonResponseBody;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.RequestParams.GV_CONSENT_FLOW;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_UI_MICROSERVICE_GVCONSENT;

@Component
public class AddMoneyToGvConsentUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddMoneyToGvConsentUtil.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private UIMicroserviceHelper uiMicroserviceHelper;

    @Autowired
    private FF4JHelper ff4JHelper;

    public PageDetailsResponse showConsentPageForRedirection(String mid, String orderId, boolean isAddMoneyExpress) {
        String callbackUrl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_URL)
                + TheiaConstant.GvConsent.PTC_V1_URL;
        String cancelUrl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_URL)
                + TheiaConstant.GvConsent.CANCEL_TXN_URL;
        String token = generateToken(mid, orderId);
        boolean isExpressAddMoneyToGv = false;
        if (isAddMoneyExpress) {
            isExpressAddMoneyToGv = true;
            callbackUrl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_URL)
                    + TheiaConstant.GvConsent.PTR_URL;
        }
        GvConsentPagePayload payload = new GvConsentPagePayload(mid, orderId, callbackUrl, cancelUrl, token,
                isExpressAddMoneyToGv, TheiaConstant.GvConsent.GV_CONSENT_FLOW);
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        try {
            String jsonPayload = JsonMapper.mapObjectToJson(payload);

            String htmlPage = null;

            // check for ff4j and get HTML from ui-microservice

            UIMicroserviceRequest uiMicroserviceRequest = new UIMicroserviceRequest(jsonPayload,
                    UIMicroserviceUrl.GV_CONSENT_URL);
            UIMicroserviceResponse uiMicroserviceResponse = uiMicroserviceHelper.getHtmlPageFromUI(
                    uiMicroserviceRequest, FEATURE_UI_MICROSERVICE_GVCONSENT, mid);
            htmlPage = uiMicroserviceResponse.getHtmlPage();

            if (StringUtils.isBlank(htmlPage)) {
                htmlPage = ConfigurationUtil.getGvConsentPage();
                htmlPage = htmlPage.replace(TheiaConstant.GvConsent.PUSH_APP_DATA, jsonPayload);
            }
            pageDetailsResponse.setSuccessfullyProcessed(false);
            pageDetailsResponse.setHtmlPage(htmlPage);
            pageDetailsResponse.setAddMoneyToGvConsentKey(token);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Failed to map object to json in native plus gvConsent!");
            pageDetailsResponse.setSuccessfullyProcessed(false);
        }
        return pageDetailsResponse;
    }

    public PageDetailsResponse showConsentPageForNativePlus(String mid, String orderId) {
        StringBuilder sb = new StringBuilder();
        sb.append("?mid=").append(mid);
        sb.append("&orderId=").append(orderId);
        NativeJsonResponse nativeJsonResponse = new NativeJsonResponse();
        NativeJsonResponseBody nativeJsonResponseBody = new NativeJsonResponseBody();
        nativeJsonResponseBody.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.SUCCESS));
        String actionUrl = ConfigurationUtil.getProperty(TheiaConstant.ExtraConstants.THEIA_BASE_URL)
                + TheiaConstant.GvConsent.SHOW_ADD_MONEY_TO_GV_CONSENT_PAGE_URL + sb.toString();
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
        String token = generateToken(mid, orderId);
        content.put("token", token);

        formDetail.setContent(content);
        bankForm.setRedirectForm(formDetail);
        nativeJsonResponseBody.setBankForm(bankForm);
        nativeJsonResponse.setHead(new ResponseHeader());
        nativeJsonResponse.setBody(nativeJsonResponseBody);
        String jsonResponse = "{}";
        try {
            jsonResponse = JsonMapper.mapObjectToJson(nativeJsonResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Failed to map NativeJson gv consent object to json", e);
        }
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse();
        pageDetailsResponse.setS2sResponse(jsonResponse);
        pageDetailsResponse.setAddMoneyToGvConsentKey(token);
        return pageDetailsResponse;
    }

    private String generateToken(String mid, String orderId) {
        SecureRandom number = new SecureRandom();
        String suffix = String.format("%06d", Math.abs(number.nextInt()));
        StringBuilder sb = new StringBuilder();
        sb.append(mid).append(TheiaConstant.GvConsent.UNDERSCORE).append(orderId)
                .append(TheiaConstant.GvConsent.UNDERSCORE).append(suffix);
        return sb.toString();
    }

    public boolean bypassGvConsentPage() {
        return Boolean.TRUE.toString().equals(
                ConfigurationUtil.getProperty(TheiaConstant.GvConsent.BYPASS_GV_CONSENT_PAGE));
    }

    public void setAttributesForGvConsentFlow(HttpServletRequest request) {
        String token = request.getParameter(TheiaConstant.GvConsent.TOKEN);
        if (StringUtils.isBlank(token)) {
            throw new TheiaServiceException("GV consent flow token empty.");
        }
        Object object = nativeSessionUtil.getKey(token);
        if (object == null) {
            throw new TheiaServiceException("Session expired for GV consent flow.");
        }
        Map<String, String[]> additionalParams = (Map<String, String[]>) (object);
        for (Map.Entry entry : additionalParams.entrySet()) {
            if (entry.getValue() != null && ((String[]) entry.getValue()).length != 0) {
                request.setAttribute((String) entry.getKey(), ((String[]) entry.getValue())[0]);
            }
        }
        request.setAttribute(GV_CONSENT_FLOW, Boolean.TRUE.toString());
    }

    public void expireGvConsentFlowSession(String token) {
        nativeSessionUtil.deleteKey(token);
    }
}
