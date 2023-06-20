/**
 *
 */
package com.paytm.pgplus.theia.session.utils;

import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.workflow.model.WorkFlowResponseBean;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.OAuthUserInfo;
import com.paytm.pgplus.theia.utils.BeanParamValidator;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author amit.dubey
 *
 */
@Component("loginInfoSessionUtil")
public class LoginInfoSessionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginInfoSessionUtil.class);

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService theiaSessionDataService;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    private static final String LOGIN_STRIP_PAYTM_PAYMODE_KEY = "login.strip.text.paytm.paymodes";
    private static final String LOGIN_STRIP_PAYTM_PAYMODE_TEXT = "Login to use Paytm balance";

    private static final String LOGIN_STRIP_ONLY_PG_PAYMODE_KEY = "login.strip.text.only.pg.paymodes";
    private static final String LOGIN_STRIP_ONLY_PG_PAYMODE_TEXT = "Login to use your Paytm saved cards";

    private static final String LOGIN_STRIP_DEFAULT_KEY = "login.strip.text.default";
    private static final String LOGIN_STRIP_DEFAULT_TEXT = "Login to use Paytm balance or your saved cards";

    public void setLoginInfoIntoSession(final PaymentRequestBean requestData, final WorkFlowResponseBean responseData) {
        LOGGER.debug("WorkFlowResponseBean :{}", responseData);

        final LoginInfo loginInfo = theiaSessionDataService.getLoginInfoFromSession(requestData.getRequest(), true);
        final OAuthUserInfo userInfo = loginInfo.getUser() != null ? loginInfo.getUser() : new OAuthUserInfo();
        if (BeanParamValidator.validateInputObjectParam(responseData.getUserDetails())) {
            loginInfo.setLoginFlag(true);
            LOGGER.debug("Found data in session, loginInfo :{}", loginInfo);
            userInfo.setUserName(responseData.getUserDetails().getUserName());
            userInfo.setMobileNumber(responseData.getUserDetails().getMobileNo());
            userInfo.setEmailId(responseData.getUserDetails().getEmail());
            userInfo.setUserID(responseData.getUserDetails().getUserId());// Paytm
            // UserID
            userInfo.setPayerUserID(responseData.getUserDetails().getInternalUserId());// AlipayUserID
            userInfo.setPayerAccountNumber(responseData.getUserDetails().getPayerAccountNumber());
            userInfo.setKYC(responseData.getUserDetails().isKYC());
            /*
             * When User is logged in , get paytm token from original payment
             * request , else paytm token will be obtained from userDetailsBiz
             * for post login flow
             */
            userInfo.setPaytmToken(StringUtils.isNotBlank(requestData.getPaytmToken()) ? requestData.getPaytmToken()
                    : responseData.getUserDetails().getUserToken());

            /*
             * adding this for riskExtendedInfo during a+ /pay api
             */
            userInfo.setUserTypes(responseData.getUserDetails().getUserTypes());

            LOGGER.debug("Updated session login data :{}", loginInfo);
        } else {
            // If user not logged in
            loginInfo.setLoginFlag(false);
        }

        // checked for litepayview change not needed
        if (responseData.getMerchnatViewResponse() != null) {
            loginInfo.setLoginMandatory(responseData.getMerchnatViewResponse().isLoginMandatory());
        }

        // From Merchant Preferences
        if (isAutoLoginEnable(requestData)) {
            loginInfo.setAutoLoginCreate(true);
        } else {
            loginInfo.setAutoLoginCreate(false);
        }

        loginInfo.setLoginWithOtp(merchantExtendInfoUtils.isLoginViaOtpEnabled(requestData.getMid()));

        setLoginStripPreferences(requestData.getMid(), responseData, loginInfo);
    }

    private boolean isAutoLoginEnable(PaymentRequestBean requestData) {
        return merchantPreferenceService.isAutoLoginEnable(requestData.getMid());
    }

    private void setLoginStripPreferences(String mid, final WorkFlowResponseBean responseData, final LoginInfo loginInfo) {

        if (StringUtils.isBlank(mid) || null == loginInfo)
            return;

        loginInfo.setLoginDisabled(merchantPreferenceService.isLoginDisabled(mid));

        if (!loginInfo.isLoginDisabled())
            setLoginStripText(responseData, loginInfo);

    }

    void setLoginStripText(final WorkFlowResponseBean responseData, final LoginInfo loginInfo) {

        List<PayMethodViewsBiz> payMethodViewsBiz = responseData.getMerchnatViewResponse().getPayMethodViews();

        boolean pgPayModeAvailable = false;
        boolean paytmPayModeAvailable = false;

        for (PayMethodViewsBiz payMethodView : payMethodViewsBiz) {

            PayMethod payMethod = PayMethod.getPayMethodByMethod(payMethodView.getPayMethod());

            if ("Paytm".equals(payMethod.getType())) {
                paytmPayModeAvailable = true;
            }

            if ("PG".equals(payMethod.getType())) {
                pgPayModeAvailable = true;
            }
        }

        String loginStripText;

        if (paytmPayModeAvailable && !pgPayModeAvailable) {
            loginStripText = ConfigurationUtil.getMessageProperty(LOGIN_STRIP_PAYTM_PAYMODE_KEY,
                    LOGIN_STRIP_PAYTM_PAYMODE_TEXT);
        } else if (pgPayModeAvailable && !paytmPayModeAvailable) {
            loginStripText = ConfigurationUtil.getMessageProperty(LOGIN_STRIP_ONLY_PG_PAYMODE_KEY,
                    LOGIN_STRIP_ONLY_PG_PAYMODE_TEXT);
        } else {
            loginStripText = ConfigurationUtil.getMessageProperty(LOGIN_STRIP_DEFAULT_KEY, LOGIN_STRIP_DEFAULT_TEXT);
        }

        loginInfo.setLoginStripText(loginStripText);
    }
}
