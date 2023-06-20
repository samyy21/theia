/**
 *
 */
package com.paytm.pgplus.theia.controllers;

import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import com.paytm.pgplus.pgproxycommon.models.SavedCardInfo;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.SessionDataAttributes;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.sessiondata.*;
import com.paytm.pgplus.theia.viewmodel.EntityPaymentOptionsTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import static com.paytm.pgplus.payloadvault.paymentservice.utils.ConstantsUtil.PARAMETERS.REQUEST.ACCESS_TOKEN;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.OAUTH_LOGOUT_PROPERTY;

/**
 * @createdOn 21-Mar-2016
 * @author kesari
 */
@Controller
public class LogoutController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutController.class);

    @Autowired
    @Qualifier(value = "theiaViewResolverService")
    private ITheiaViewResolverService theiaViewResolverService;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    public static final String AUTHORIZATION_TOKEN = "Authorization";

    @RequestMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model,
            Locale locale) {
        LOGGER.debug("model :{}, locale :{}", model, locale);
        // calling oauth2/logout api which clear the cookies for the user
        try {
            LOGGER.info("Calling Logout Api for clearing cookies ");
            queryHelper(request);
        } catch (Exception e) {
            LOGGER.info("Failed to clear cookies during logout");
        }
        removeLoginSessionAttribute(session, request);
        return theiaViewResolverService.returnOauthLoginRedirectPage();
    }

    private void queryHelper(HttpServletRequest request) {
        LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(request);
        String clientId = configurationDataService.getPaytmProperty(TheiaConstant.ExtraConstants.OAUTH_CLIENT_ID)
                .getValue();
        String clientSecret = configurationDataService.getPaytmProperty(
                TheiaConstant.ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue();
        String paytmToken = null;
        if (loginInfo != null && loginInfo.getUser() != null) {
            paytmToken = loginInfo.getUser().getPaytmToken();
        }
        HttpRequestPayload<String> payload = generatePayloadForLogOut2(clientId, clientSecret, paytmToken);
        Response response = null;
        try {
            response = JerseyHttpClient.sendHttpGetRequest(payload);
        } catch (Exception e) {
            LOGGER.debug("Failed to clear cookies during logout");
        }
    }

    public static HttpRequestPayload<String> generatePayloadForLogOut2(String clientId, String clientSecret,
            String token) {
        HttpRequestPayload<String> payload = new HttpRequestPayload();
        MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap();
        headerMap.add(ACCESS_TOKEN, token);
        headerMap.add(AUTHORIZATION_TOKEN, "Basic " + getParam(clientId, clientSecret));
        String authUrl = ConfigurationUtil.getProperty(OAUTH_LOGOUT_PROPERTY);
        payload.setTarget(authUrl);
        payload.setHeaders(headerMap);
        payload.setHttpMethod(HttpMethod.GET);
        return payload;
    }

    private static String getParam(String clientId, String secretKey) {
        return new String(Base64.getEncoder().encode((clientId + ":" + secretKey).getBytes()));
    }

    private void removeLoginSessionAttribute(HttpSession session, HttpServletRequest request) {
        session.removeAttribute(SessionDataAttributes.walletInfo.name());
        LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(request);
        removeCardsSavedOnUserId(request);
        loginInfo.setUser(new OAuthUserInfo());
        loginInfo.setLoginFlag(false);
        loginInfo.setLoginRetryCount(0);
        loginInfo.setShowLoginSuccess(false);

        TransactionConfig transactionConfig = theiaSessionDataService.getTxnConfigFromSession(request);
        transactionConfig.setAddAndPayAllowed(false);
        transactionConfig.setAddMoneyFlag(false);
        transactionConfig.setHybridAllowed(false);
        transactionConfig.setAddAndPayAllowed(false);

        removeEntityOptions(request);

        TransactionInfo txnInfo = theiaSessionDataService.getTxnInfoFromSession(request);
        txnInfo.setPaymentOtpSendCount(0);
        txnInfo.setSaveStateAgainstPaymentOTP(null);
        txnInfo.setMaxInvalidAttempts(0);

        theiaSessionDataService.removeAttributeFromSession(request, SessionDataAttributes.walletInfo);
        removePPBLDetails(request);

        LOGGER.debug("Removed login Attributes from session");
    }

    private void removeEntityOptions(HttpServletRequest request) {
        EntityPaymentOptionsTO enOptionsTO = theiaSessionDataService.getEntityPaymentOptions(request);
        enOptionsTO.setAddAtmEnabled(false);
        enOptionsTO.setAddCompleteATMList(Collections.emptyList());
        enOptionsTO.setAddCcEnabled(false);
        enOptionsTO.setAddCompleteCcList(Collections.emptyList());
        enOptionsTO.setAddCodEnabled(false);
        enOptionsTO.setAddDcEnabled(false);
        enOptionsTO.setAddCompleteDcList(Collections.emptyList());
        enOptionsTO.setAddEmiEnabled(false);
        enOptionsTO.setAddCompleteEMIInfoList(Collections.emptyList());
        enOptionsTO.setAddImpsEnabled(false);
        enOptionsTO.setAddCompleteIMPSList(Collections.emptyList());
        enOptionsTO.setAddNetBankingEnabled(false);
        enOptionsTO.setAddCompleteNbList(Collections.emptyList());
        enOptionsTO.setPaymentsBankEnabled(false);
        enOptionsTO.setAddPaymentsBankEnabled(false);
    }

    private void removePPBLDetails(HttpServletRequest request) {

        SavingsAccountInfo savingsAccountInfo = theiaSessionDataService
                .getSavingsAccountInfoFromSession(request, false);

        if (savingsAccountInfo == null)
            return;

        savingsAccountInfo.setSavingsAccountEnabled(false);
    }

    private void removeCardsSavedOnUserId(HttpServletRequest request) {
        CardInfo cardInfo = theiaSessionDataService.getCardInfoFromSession(request);
        LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(request);
        OAuthUserInfo user = loginInfo.getUser();
        if (cardInfo != null) {
            Iterator<SavedCardInfo> itr = cardInfo.getMerchantViewSavedCardsList().iterator();
            while (itr.hasNext()) {
                SavedCardInfo card = itr.next();
                if (StringUtils.isNotBlank(card.getUserId()) && card.getUserId().equalsIgnoreCase(user.getUserID())) {
                    LOGGER.info("Removing card : {} saved on userId: ", card.getCardId());
                    cardInfo.getSavedCardMap().remove(card.getCardId().toString());
                    itr.remove();
                }
            }
            itr = cardInfo.getAddAndPayViewCardsList().iterator();
            while (itr.hasNext()) {
                SavedCardInfo card = itr.next();
                if (StringUtils.isNotBlank(card.getUserId()) && card.getUserId().equalsIgnoreCase(user.getUserID())) {
                    LOGGER.info("Removing card : {} saved on userId :", card.getCardId());
                    cardInfo.getAddAnPaySavedCardMap().remove(card.getCardId().toString());
                    itr.remove();
                }
            }
            if ((cardInfo.getMerchantViewSavedCardsList() == null || cardInfo.getMerchantViewSavedCardsList().isEmpty())
                    && (cardInfo.getAddAndPayViewCardsList() == null || cardInfo.getAddAndPayViewCardsList().isEmpty())) {
                cardInfo.setSaveCardEnabled(false);
            }
        }
    }
}
