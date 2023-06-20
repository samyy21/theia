/**
 * 
 */
package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginRequestBizBean;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginResponseBizBean;
import com.paytm.pgplus.biz.core.user.service.ILogin;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.facade.enums.UIMicroserviceUrl;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.datamapper.IBizRequestResponseMapper;
import com.paytm.pgplus.theia.datamapper.impl.WorkflowRequestGenerator;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;
import com.paytm.pgplus.theia.helper.UIMicroserviceHelper;
import com.paytm.pgplus.theia.models.uimicroservice.request.UIMicroserviceRequest;
import com.paytm.pgplus.theia.models.uimicroservice.response.UIMicroserviceResponse;
import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageCachePayload;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.theia.services.IOAuthResponseService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.helper.EnhancedCashierPageServiceHelper;
import com.paytm.pgplus.theia.services.helper.FF4JHelper;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.utils.PaymentOTPService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.FEATURE_UI_MICROSERVICE_ENHANCED;

/**
 * @author vaishakhnair
 *
 */
@Service("oAuthResponseService")
public class OAuthResponseServiceImpl implements IOAuthResponseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthResponseServiceImpl.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("workflowRequestGenerator")
    private WorkflowRequestGenerator workflowRequestGenerator;

    @Autowired
    @Qualifier("defaultPostLoginAtCashierFlow")
    private IWorkFlow defaultPostLoginAtCashierFlow;

    @Autowired
    @Qualifier("buyerPaysChargePostLoginAtCashierFlow")
    private IWorkFlow buyerPaysChargePostLoginAtCashierFlow;

    @Autowired
    @Qualifier("subscriptionPostLoginAtCashierFlow")
    private IWorkFlow subscriptionPostLoginAtCashierFlow;

    @Autowired
    @Qualifier("bizRequestResponseMapper")
    private IBizRequestResponseMapper bizRequestResponseMapper;

    @Autowired
    private PaymentOTPService paymentOTPUtil;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    EnhancedCashierPageServiceHelper enhancedCashierPageServiceHelper;

    @Autowired
    @Qualifier("enhancedCashierPageService")
    EnhancedCashierPageServiceImpl enhancedCashierPageService;

    @Autowired
    @Qualifier("loginService")
    private ILogin loginServiceBiz;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Autowired
    private FF4JHelper ff4JHelper;

    @Autowired
    private UIMicroserviceHelper uiMicroserviceHelper;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Override
    public void processLoginResponse(PaymentRequestBean paymentRequestData) {

        WorkFlowRequestBean workFlowRequestBean = workflowRequestGenerator
                .generateRequestBeanForPostLogin(paymentRequestData);

        LOGGER.debug("Request object to be sent to Post Login Workflow is :: {}", workFlowRequestBean);
        GenericCoreResponseBean<WorkFlowResponseBean> response;
        if (workFlowRequestBean.getSubsTypes() != null) {
            response = subscriptionPostLoginAtCashierFlow.process(workFlowRequestBean);
        } else {
            boolean slabBased = merchantPreferenceService.isSlabBasedMDREnabled(workFlowRequestBean.getPaytmMID());
            if (merchantPreferenceService.isPostConvenienceFeesEnabled(workFlowRequestBean.getPaytmMID()) || slabBased) {
                workFlowRequestBean.setSlabBasedMDR(slabBased);
                response = buyerPaysChargePostLoginAtCashierFlow.process(workFlowRequestBean);
            } else {
                response = defaultPostLoginAtCashierFlow.process(workFlowRequestBean);
            }
        }
        LOGGER.debug("Response from workflow is :: {} ", response);
        if (response == null || !response.isSuccessfullyProcessed()) {
            throw new TheiaServiceException(response == null ? "Null Response" : response.getFailureMessage());
        }
        WorkFlowResponseBean responseBean = response.getResponse();
        bizRequestResponseMapper.mapPostLoginResponseToSession(paymentRequestData, responseBean);
        LOGGER.debug("Successfully mapped all necessary data to session");

        /*
         * Generate paymentOTP if required
         */
        try {
            paymentOTPUtil.generateIfPaymentOTP(paymentRequestData);
        } catch (PaytmValidationException e) {
            LOGGER.error("Exception In generating Payment OTP", e);
        }
    }

    @Override
    public void incrementLoginRetryCount(PaymentRequestBean paymentRequestData) {
        LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(paymentRequestData.getRequest());
        if (loginInfo != null) {
            loginInfo.setLoginRetryCount(loginInfo.getLoginRetryCount() + 1);
            if (loginInfo.getLoginRetryCount() >= 5) {
                loginInfo.setLoginFlag(true);
                loginInfo.setShowLoginSuccess(false);
            }
        }
    }

    @Override
    public String processLoginForEnhancedCashierFlow(final HttpServletRequest request) throws Exception {

        String[] oAuthDetails = fetchOauthDetails(request);

        setMDC(oAuthDetails);

        String paytmSSOToken = null;

        GenericCoreResponseBean<VerifyLoginResponseBizBean> verifyLoginResponseBean = fetchValidateAndFetchPaytmToken(
                request, oAuthDetails);
        if (!verifyLoginResponseBean.isSuccessfullyProcessed()) {
            paytmSSOToken = StringUtils.EMPTY;
        } else {
            paytmSSOToken = verifyLoginResponseBean.getResponse().getPaytmToken().getToken();
        }

        EnhanceCashierPageCachePayload enhanceCashierPageCachePayload = (EnhanceCashierPageCachePayload) theiaTransactionalRedisUtil
                .get(enhancedCashierPageServiceHelper.fetchRedisKey(oAuthDetails[0], oAuthDetails[1]));

        if (StringUtils.isBlank(paytmSSOToken)) {
            LOGGER.error("Exception occurred while validating Token from OAuth");

            EnhancedCashierPage enhancedCashierPage = enhanceCashierPageCachePayload.getEnhancedCashierPage();
            String enhancedCashierPageJson = JsonMapper.mapObjectToJson(enhancedCashierPage);
            String channelId = theiaSessionDataService.getChannel(request, false);

            String htmlPage = null;

            PaymentRequestBean requestData = enhanceCashierPageCachePayload.getMerchantRequestData();

            // fetching data from ui- microservice
            String mid = null;
            if (requestData != null) {
                mid = requestData.getMid();
            }

            UIMicroserviceRequest uiMicroserviceRequest = new UIMicroserviceRequest(enhancedCashierPageJson, channelId,
                    "false", UIMicroserviceUrl.ENHANCED_CASHIER_URL);
            UIMicroserviceResponse uiMicroserviceResponse = uiMicroserviceHelper.getHtmlPageFromUI(
                    uiMicroserviceRequest, FEATURE_UI_MICROSERVICE_ENHANCED, mid);
            htmlPage = uiMicroserviceResponse.getHtmlPage();

            // if blank retrieve from old method
            if (StringUtils.isBlank(htmlPage)) {

                htmlPage = enhancedCashierPageServiceHelper.getEnhancedCashierTheme(channelId);

                if (org.apache.commons.lang.StringUtils.isNotBlank(htmlPage)) {
                    htmlPage = htmlPage
                            .replace(
                                    com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.EnhancedCashierPageKeys.REPLACE_STRING,
                                    enhancedCashierPageJson);
                } else {
                    throw new TheiaServiceException("Unable to fetch html template from redis");
                }
            }
            return htmlPage;
        }

        PaymentRequestBean paymentRequestBean = enhanceCashierPageCachePayload.getMerchantRequestData();
        InitiateTransactionResponse initiateTransactionResponse = enhanceCashierPageCachePayload
                .getInitiateTransactionResponse();

        String transactionToken = initiateTransactionResponse.getBody().getTxnToken();
        NativeInitiateRequest nativeInitiateRequest = nativeSessionUtil.validate(transactionToken);
        InitiateTransactionRequestBody initiateTransactionRequestBody = nativeInitiateRequest.getInitiateTxnReq()
                .getBody();

        paymentRequestBean.setPaytmToken(paytmSSOToken);
        initiateTransactionRequestBody.setPaytmSsoToken(paytmSSOToken);

        if (verifyLoginResponseBean.getResponse() != null
                && verifyLoginResponseBean.getResponse().getUserDetails() != null) {
            String userName = verifyLoginResponseBean.getResponse().getUserDetails().getUserName();
            String mobileNo = verifyLoginResponseBean.getResponse().getUserDetails().getMobileNo();
            paymentRequestBean.setUserName(userName);
            paymentRequestBean.setMobileNo(mobileNo);
        }

        initiateTransactionResponse.getBody().setAuthenticated(true);

        theiaTransactionalRedisUtil.hset(transactionToken, "orderDetail", nativeInitiateRequest,
                nativeSessionUtil.getTokenExpiryTime(false));

        String htmlResponse = enhancedCashierPageService.fetchPageDetailsResponse(paymentRequestBean,
                initiateTransactionResponse).getHtmlPage();

        return htmlResponse;
    }

    private void setMDC(String[] oAuthDetails) {
        MDC.put(TheiaConstant.RequestParams.MID, oAuthDetails[0]);
        MDC.put(TheiaConstant.RequestParams.ORDER_ID, oAuthDetails[1]);
    }

    public GenericCoreResponseBean<VerifyLoginResponseBizBean> fetchValidateAndFetchPaytmToken(
            final HttpServletRequest request, final String[] oAuthDetails) {

        for (String param : oAuthDetails) {

            if (StringUtils.isBlank(param)) {
                LOGGER.error("Invalid Oauth Response, MID, orderId or Code is blank");
                return new GenericCoreResponseBean<>("Invalid Oauth Response, MID, orderId or Code is blank",
                        ResponseConstants.SYSTEM_ERROR);
            }
        }

        String mid = oAuthDetails[0];

        String[] clientIDAndSecret = workflowRequestGenerator.fetchClientIDAndClientSecret(mid, request
                .getParameterMap().get("state")[0]);

        String oAuthCode = oAuthDetails[2];
        String clientId = clientIDAndSecret[0];
        String secretKey = clientIDAndSecret[1];
        String custId = null;

        boolean isStoreCardPreference = false;

        final VerifyLoginRequestBizBean verifyLoginReqBean = new VerifyLoginRequestBizBean();
        verifyLoginReqBean.setClientID(clientId);
        verifyLoginReqBean.setoAuthCode(oAuthCode);
        verifyLoginReqBean.setSecretKey(secretKey);
        verifyLoginReqBean.setCustId(custId);
        verifyLoginReqBean.setmId(mid);
        verifyLoginReqBean.setStoreCardPrefEnabled(isStoreCardPreference);

        GenericCoreResponseBean<VerifyLoginResponseBizBean> responseBean = loginServiceBiz
                .verfifyLogin(verifyLoginReqBean);

        if (!responseBean.isSuccessfullyProcessed()) {
            LOGGER.error(responseBean.getFailureDescription());
            return new GenericCoreResponseBean<>(responseBean.getFailureDescription(),
                    responseBean.getResponseConstant());
        }

        return responseBean;
    }

    /*
     * Returns :: 1- Mid 2- OrderId 3- OAuthCode
     */
    String[] fetchOauthDetails(final HttpServletRequest request) {

        Map<String, String[]> paramMap = request.getParameterMap();

        String[] result = new String[3];

        for (Map.Entry<String, String[]> entrySet : paramMap.entrySet()) {

            if (entrySet.getKey().equals("state")) {
                String[] state = entrySet.getValue()[0].split(":");
                result[0] = state[1];
                result[1] = state[0];

            }

            if (entrySet.getKey().equals("code")) {
                result[2] = entrySet.getValue()[0];
            }
        }

        return result;
    }
}
