/**
 *
 */
package com.paytm.pgplus.theia.session.utils;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.EMAIL;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MSISDN;

import com.paytm.pgplus.theia.nativ.model.common.EnhanceCashierPageOAuthInfo;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.listener.LoggingErrorHandler;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.cache.model.PaytmProperty;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.facade.utils.PropertiesUtil;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.SessionAttributes;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author amit.dubey
 *
 */
@SuppressWarnings("Duplicates")
@Component("oAuthInfoSessionUtil")
public class OAuthInfoSessionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    Environment environment;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    /**
     * @param requestData
     * @param responseData
     */
    public void setOAuthInfo(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData) {
        JSONObject jsonObject = new JSONObject();
        String oauthWebClient = getPaytmProperty(OAUTH_CLIENT_ID);
        String oauthWapClient = getPaytmProperty(OAUTH_CLIENT_WAP_ID);
        String noCookieMids = getPaytmProperty(OAUTH_NOCOOKIE_CLIENT_MIDS);
        String oauthOtpMids = getPaytmProperty(OAUTH_OTP_CLIENT_MIDS);

        if (noCookieMids.contains(requestData.getMid())) {
            oauthWebClient = getPaytmProperty(OAUTH_NOCOOKIE_CLIENT_ID);
            oauthWapClient = oauthWebClient;
        } else if (oauthOtpMids.contains(requestData.getMid())) {
            oauthWebClient = getPaytmProperty(OAUTH_OTP_CLIENT_ID);
        }

        if (StringUtils.isNotBlank(noCookieMids) && noCookieMids.contains(requestData.getMid())) {
            PaytmProperty noCookieClientMID = configurationDataService.getPaytmProperty(OAUTH_NOCOOKIE_CLIENT_ID);
            if (noCookieClientMID != null) {
                oauthWebClient = noCookieClientMID.getValue();
            }
            oauthWapClient = oauthWebClient;
        } else if (StringUtils.isNotBlank(oauthOtpMids) && oauthOtpMids.contains(requestData.getMid())) {
            PaytmProperty oTPClientID = configurationDataService.getPaytmProperty(OAUTH_OTP_CLIENT_ID);
            if (oTPClientID != null) {
                oauthWebClient = oTPClientID.getValue();
            }
            oauthWapClient = oauthWebClient;
        }

        String oAuthHost = PropertiesUtil.getProperties().getProperty(OAUTH_BASE_URL);
        jsonObject.put(JSON_WEB_CLIENT_ID, oauthWebClient);
        jsonObject.put(JSON_WAP_CLIENT_ID, oauthWapClient);
        jsonObject.put(ExtraConstants.JSON_OAUTH_HOST, oAuthHost);

        String loginTheme = requestData.getLoginTheme();
        String responseUrl = "";
        HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();

        LOGGER.info("host is in oauth : " + httpServletRequest.getHeader(HttpHeaders.HOST));

        if (httpServletRequest != null
                && httpServletRequest.getHeader(HttpHeaders.HOST) != null
                && httpServletRequest.getHeader(HttpHeaders.HOST).contains(
                        ConfigurationUtil.getProperty(ExtraConstants.OLD_PG_BASE_URL_SKIP))) {

            responseUrl = ConfigurationUtil.getProperty(SessionAttributes.OAUTH_RES_SERVER_URI_OLD_PG);
            LOGGER.info("oauth responsereturn  old pg url is : " + responseUrl);

        } else {
            responseUrl = ConfigurationUtil.getProperty(SessionAttributes.OAUTH_RES_SERVER_URI);
        }

        jsonObject.put(EMAIL, StringUtils.isNotBlank(requestData.getEmail()) ? requestData.getEmail() : "");
        jsonObject.put(ADDRESS1, StringUtils.isNotBlank(requestData.getAddress1()) ? requestData.getAddress1() : "");
        jsonObject.put(ADDRESS2, StringUtils.isNotBlank(requestData.getAddress2()) ? requestData.getAddress2() : "");
        jsonObject.put(MSISDN, StringUtils.isNotBlank(requestData.getMsisdn()) ? requestData.getMsisdn() : "");
        jsonObject.put(ORDERID, requestData.getOrderId());
        jsonObject.put(MID, requestData.getMid());
        jsonObject.put(OAUTH_RETURN_URL, responseUrl);
        jsonObject.put(LOGINTHEME, loginTheme);

        final LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(requestData.getRequest(), true);
        loginInfo.setOauthInfo(jsonObject.toString());
        loginInfo.setoAuthInfoHost(jsonObject.getString(ExtraConstants.JSON_OAUTH_HOST));
        loginInfo.setoAuthInfoClientID(jsonObject.getString(JSON_WEB_CLIENT_ID));
        loginInfo.setoAuthInfoWAPClientID(jsonObject.getString(JSON_WAP_CLIENT_ID));
        loginInfo.setoAuthInfoReturnURL(jsonObject.getString(OAUTH_RETURN_URL));

        loginInfo.setLoginTheme(loginTheme);

        if (isAutoLoginEnable(requestData)) {
            jsonObject.put(AUTO_SIGNUP, true);
            loginInfo.setAutoLoginCreate(true);
        } else {
            loginInfo.setAutoLoginCreate(false);
            jsonObject.put(AUTO_SIGNUP, false);
        }
        if (StringUtils.isNotBlank(noCookieMids) && noCookieMids.contains(requestData.getMid())) {
            loginInfo.setAutoLoginAttempt(false);
        }
    }

    public void setPostLoginOAuthInfoToSession(final PaymentRequestBean requestData,
            final WorkFlowResponseBean responseData) {

        final LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(requestData.getRequest(), true);
        loginInfo.setLogoutAllowed(true);

        String authLogoutReturnURL = ConfigurationUtil.getProperty(OAUTH_LOGOUT_RETURN_URL);
        HttpServletRequest httpServletRequest = EnvInfoUtil.httpServletRequest();

        LOGGER.info("host is in oauth : " + httpServletRequest.getHeader(HttpHeaders.HOST));

        if (httpServletRequest != null
                && httpServletRequest.getHeader(HttpHeaders.HOST) != null
                && httpServletRequest.getHeader(HttpHeaders.HOST).contains(
                        ConfigurationUtil.getProperty(ExtraConstants.OLD_PG_BASE_URL_SKIP))) {

            authLogoutReturnURL = ConfigurationUtil.getProperty(OAUTH_LOGOUT_RETURN_URL_OLD_PG);
            LOGGER.info("authlogoutnreturl is : " + authLogoutReturnURL);
        }

        String authUrl = ConfigurationUtil.getProperty(OAUTH_LOGOUT_PROPERTY);
        if (StringUtils.isBlank(authUrl)) {
            authUrl = OAUTH_LOGOUT_URL;
        }

        String oAuthInfoJson = loginInfo.getOauthInfo();
        JSONObject jsonObject = new JSONObject(oAuthInfoJson);

        jsonObject.put(AUTH_LOGOUT_RETURN_URL, authLogoutReturnURL);
        jsonObject.put(PAYTM_TOKEN, responseData.getUserDetails().getUserToken());
        jsonObject.put(AUTH_LOGOUT_URL, authUrl);
        jsonObject.put(AUTO_LOGIN, AUTO_LOGIN_VALUE);

        loginInfo.setOauthInfo(jsonObject.toString());

    }

    private String getPaytmProperty(String propertyName) {
        PaytmProperty paytmProperty = configurationDataService.getPaytmProperty(propertyName);
        if (paytmProperty != null) {
            return paytmProperty.getValue();
        }
        return StringUtils.EMPTY;
    }

    private boolean isAutoLoginEnable(PaymentRequestBean requestData) {
        return merchantPreferenceService.isAutoLoginEnable(requestData.getMid());
    }

    public EnhanceCashierPageOAuthInfo fetchOAuthInfoForEnhancedCashierPage(final PaymentRequestBean requestData) {

        EnhanceCashierPageOAuthInfo enhanceCashierPageOAuthInfo = new EnhanceCashierPageOAuthInfo();

        String oauthWebClient = getPaytmProperty(OAUTH_CLIENT_ID);
        String oauthWapClient = getPaytmProperty(OAUTH_CLIENT_WAP_ID);
        String noCookieMids = getPaytmProperty(OAUTH_NOCOOKIE_CLIENT_MIDS);
        String oauthOtpMids = getPaytmProperty(OAUTH_OTP_CLIENT_MIDS);

        if (noCookieMids.contains(requestData.getMid())) {
            oauthWebClient = getPaytmProperty(OAUTH_NOCOOKIE_CLIENT_ID);
            oauthWapClient = oauthWebClient;
        } else if (oauthOtpMids.contains(requestData.getMid())) {
            oauthWebClient = getPaytmProperty(OAUTH_OTP_CLIENT_ID);
        }

        if (StringUtils.isNotBlank(noCookieMids) && noCookieMids.contains(requestData.getMid())) {

            PaytmProperty noCookieClientMID = configurationDataService.getPaytmProperty(OAUTH_NOCOOKIE_CLIENT_ID);
            if (noCookieClientMID != null) {
                oauthWebClient = noCookieClientMID.getValue();
            }

            oauthWapClient = oauthWebClient;

        } else if (StringUtils.isNotBlank(oauthOtpMids) && oauthOtpMids.contains(requestData.getMid())) {
            PaytmProperty oTPClientID = configurationDataService.getPaytmProperty(OAUTH_OTP_CLIENT_ID);
            if (oTPClientID != null) {
                oauthWebClient = oTPClientID.getValue();
            }
            oauthWapClient = oauthWebClient;
        }

        String oAuthHost = PropertiesUtil.getProperties().getProperty(OAUTH_BASE_URL);

        String logoutReturnUrl = ConfigurationUtil.getProperty(OAUTH_LOGOUT_RETURN_URL);

        String paytmToken = requestData.getPaytmToken();

        String logoutUrl = ConfigurationUtil.getProperty(OAUTH_LOGOUT_PROPERTY);

        if (StringUtils.isBlank(logoutUrl)) {
            logoutUrl = OAUTH_LOGOUT_URL;
        }

        String autoLogin = AUTO_LOGIN_VALUE;

        enhanceCashierPageOAuthInfo.setoAuthLogoutReturnUrl(logoutReturnUrl);
        enhanceCashierPageOAuthInfo.setoAuthLogoutUrl(logoutUrl);
        enhanceCashierPageOAuthInfo.setPaytmToken(paytmToken);
        enhanceCashierPageOAuthInfo.setAutoLogin(autoLogin);

        enhanceCashierPageOAuthInfo.setWebClientId(oauthWebClient);
        enhanceCashierPageOAuthInfo.setWapClientId(oauthWapClient);
        enhanceCashierPageOAuthInfo.setoAuthBaseUrl(oAuthHost);

        String loginTheme = requestData.getLoginTheme();
        String responseUrl = ConfigurationUtil.getProperty(SessionAttributes.OAUTH_RES_SERVER_URI_ENHANCED_CASHIER);

        enhanceCashierPageOAuthInfo.setEmail(StringUtils.isNotBlank(requestData.getEmail()) ? requestData.getEmail()
                : "");
        enhanceCashierPageOAuthInfo.setAddress1(StringUtils.isNotBlank(requestData.getAddress1()) ? requestData
                .getAddress1() : "");
        enhanceCashierPageOAuthInfo.setAddress2(StringUtils.isNotBlank(requestData.getAddress2()) ? requestData
                .getAddress2() : "");
        enhanceCashierPageOAuthInfo.setMsisdn(StringUtils.isNotBlank(requestData.getMsisdn()) ? requestData.getMsisdn()
                : "");
        enhanceCashierPageOAuthInfo.setOrderId(requestData.getOrderId());
        enhanceCashierPageOAuthInfo.setMid(requestData.getMid());
        enhanceCashierPageOAuthInfo.setoAuthResponseUrl(responseUrl);
        enhanceCashierPageOAuthInfo.setLoginTheme(loginTheme);
        enhanceCashierPageOAuthInfo.setAutoLoginEnabled(merchantPreferenceService.isAutoLoginEnable(requestData
                .getMid()));

        return enhanceCashierPageOAuthInfo;
    }
}