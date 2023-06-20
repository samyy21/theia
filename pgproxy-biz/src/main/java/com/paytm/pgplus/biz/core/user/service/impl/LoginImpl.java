/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.user.service.impl;

import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginRequestBizBean;
import com.paytm.pgplus.biz.core.model.oauth.VerifyLoginResponseBizBean;
import com.paytm.pgplus.biz.core.user.service.ILogin;
import com.paytm.pgplus.biz.core.user.service.ISavedCards;
import com.paytm.pgplus.biz.core.user.service.utils.OAuthHelperServiceImpl;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.biz.exception.InvalidFacadeResponseException;
import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.enums.TxnState;
import com.paytm.pgplus.common.util.CommonConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.FetchAllTokensRequest;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.request.FetchUserTypeAttributeDetailsRequest;
import com.paytm.pgplus.facade.user.models.request.ValidateAuthCodeRequest;
import com.paytm.pgplus.facade.user.models.response.FetchAllTokensResponse;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.models.response.ValidateAuthCodeResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.pgproxycommon.models.UserProfile;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.nativ.UPSHelper;
import com.paytm.pgplus.theia.redis.ITheiaTransactionalRedisUtil;
import com.paytm.pgplus.transactionlogger.annotation.Loggable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.facade.constants.FacadeConstants.AUTH_USER_TYPE_POSTPAID_USER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.*;

/**
 * @author namanjain
 *
 */
@Service("loginService")
public class LoginImpl implements ILogin {

    @Autowired
    private IAuthentication authFacade;

    @Autowired
    OAuthHelperServiceImpl oAuthHelperServiceImpl;

    @Autowired
    @Qualifier("savedCards")
    private ISavedCards savedCardsService;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    private ITheiaTransactionalRedisUtil theiaTransactionalRedisUtil;

    @Autowired
    @Qualifier("upsHelper")
    private UPSHelper upsHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginImpl.class);

    @Loggable(logLevel = Loggable.INFO, state = TxnState.AUTH_FETCH_USER_DETAIL)
    public GenericCoreResponseBean<UserDetailsBiz> fetchUserDetails(final String token, boolean fetchSavedCards,
            final WorkFlowRequestBean workFlowBean) {

        String clientId = workFlowBean.getOauthClientId();
        String clientSecret = workFlowBean.getOauthSecretKey();

        if (StringUtils.isBlank(token)) {
            LOGGER.error("Token is blank");
            return new GenericCoreResponseBean<UserDetailsBiz>("InvalidTokenProvided", ResponseConstants.SYSTEM_ERROR);
        }
        final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(token, clientId,
                clientSecret, workFlowBean.getPaytmMID());
        try {
            final FetchUserDetailsResponse fetchUserDetailsResponse;
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                LOGGER.debug("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.info("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);
            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (fetchUserDetailsResponse.getUserDetails() == null)) {
                LOGGER.error("User details fetching failed or UserDetails is null");
                return new GenericCoreResponseBean<>(fetchUserDetailsResponse.getResponseMessage(),
                        ResponseConstants.INVALID_SSO_TOKEN);
            }

            upsHelper.updateUserPostpaidAccStatusFromUPS(fetchUserDetailsRequest.getMid(),
                    fetchUserDetailsResponse.getUserDetails());

            /**
             * Used In Payment Request For Corporate Advance Deposit So Not
             * Setting UserType null and childCustId
             */
            if (StringUtils.isNotBlank(workFlowBean.getCorporateCustId())) {
                LOGGER.debug("Corporate Advance Deposit Request. Setting corporateCustId as CustId");
                fetchUserDetailsResponse.getUserDetails().setUserId(workFlowBean.getCorporateCustId());
            }

            setUserDetailsToCache(fetchUserDetailsResponse);

            UserDetailsBiz userDetails = mappingUtil.mapUserDetails(fetchUserDetailsResponse.getUserDetails());

            updatePostpaidStatusAndCCEnabledFlag(token, workFlowBean.isPostpaidOnboardingSupported(), clientId,
                    clientSecret, userDetails);

            getAndSetUserProfile(userDetails);

            if (fetchSavedCards) {
                if (workFlowBean.isStoreCardPrefEnabled() && StringUtils.isNotBlank(workFlowBean.getCustID())) {
                    fetchSavedCardDetails(userDetails, workFlowBean.getPaytmMID(), workFlowBean.getCustID());
                } else {
                    fetchSavedCardDetails(userDetails);
                }
            }

            return new GenericCoreResponseBean<>(userDetails);
        } catch (final Exception e) {
            // LOGGER.error("Exception : ", e);
            LOGGER.error("Exception : ", ExceptionLogUtils.limitLengthOfStackTrace(e));
            return new GenericCoreResponseBean<UserDetailsBiz>("Could not fetch user details",
                    ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Override
    public void updatePostpaidStatusAndCCEnabledFlag(String token, boolean isPostpaidOnboardingSupported,
            String clientId, String clientSecret, UserDetailsBiz userDetails) throws FacadeCheckedException {
        if (userDetails.isPaytmCCEnabled()) {
            LOGGER.debug("Getting CC Enabled flag true hence user is already live so no need to hit user type Attribute Api");
        } else {
            List<String> userTypes = userDetails.getUserTypes();

            if (CollectionUtils.isNotEmpty(userTypes) && userTypes.contains(AUTH_USER_TYPE_POSTPAID_USER)) {

                FetchUserTypeAttributeDetailsRequest fetchUserPostpaidDetailsRequest = new FetchUserTypeAttributeDetailsRequest(
                        token, clientId, clientSecret, Arrays.asList(AUTH_USER_TYPE_POSTPAID_USER));

                Map<String, Map<String, String>> userTypesMap = authFacade
                        .fetchUserTypeAttributesDetails(fetchUserPostpaidDetailsRequest);

                String postpaidStatus = getPostpaidStatus(userTypesMap);

                if (StringUtils.isNotBlank(postpaidStatus)) {
                    userDetails.setPostpaidStatus(postpaidStatus);
                    boolean isPaytmCCEnabled = isPaytmCCEnabled(isPostpaidOnboardingSupported, postpaidStatus);
                    if (isPaytmCCEnabled) {
                        userDetails.setPaytmCCEnabled(true);
                    }
                }

            }
        }
    }

    @Override
    public Map<String, Map<String, String>> fetchUserTypeAttributesDetails(String token, String userType,
            String clientId, String clientSecret) throws FacadeCheckedException {

        FetchUserTypeAttributeDetailsRequest fetchUserPostpaidDetailsRequest = new FetchUserTypeAttributeDetailsRequest(
                token, clientId, clientSecret, Arrays.asList(AUTH_USER_TYPE_POSTPAID_USER));

        return authFacade.fetchUserTypeAttributesDetails(fetchUserPostpaidDetailsRequest);

    }

    private String getPostpaidStatus(Map<String, Map<String, String>> userTypesMap) {
        String postpaidStatus = null;
        if (!org.springframework.util.CollectionUtils.isEmpty(userTypesMap)
                && !org.springframework.util.CollectionUtils.isEmpty(userTypesMap.get(AUTH_USER_TYPE_POSTPAID_USER))) {
            postpaidStatus = userTypesMap.get(AUTH_USER_TYPE_POSTPAID_USER).get(POSTPAID_STATUS);
        } else {
            LOGGER.error("Unable to fetch data form fetchUserTypeAttributesDetails map");

        }
        return postpaidStatus;
    }

    private boolean isPaytmCCEnabled(boolean isPostpaidOnboardingSupported, String postpaidStatus) {
        boolean isPostPaidOnboardingFeatureEnabled = isPostPaidOnboardingFeatureEnabled();
        boolean paytmCCEnabled = POSTPAID_STATUS_LIVE.equals(postpaidStatus)
                || (isPostPaidOnboardingFeatureEnabled && isPostpaidOnboardingSupported && POSTPAID_STATUS_WHITELISTED
                        .equals(postpaidStatus));
        LOGGER.info(
                "Postpaid account status from auth for user is : {} and for current workflow isPostpaidOnboardingSupported: {}, isPostPaidOnboardingFeatureEnabled :{}, paytmCCEnabled :{}",
                postpaidStatus, isPostpaidOnboardingSupported, isPostPaidOnboardingFeatureEnabled, paytmCCEnabled);
        return paytmCCEnabled;
    }

    private boolean isPostPaidOnboardingFeatureEnabled() {
        String postPaidOnboardingFeatureEnabled = ConfigurationUtil.getProperty(IS_POSTPAID_ONBOARDING_FEATURE_ENABLED,
                "true");
        return Boolean.valueOf(postPaidOnboardingFeatureEnabled);
    }

    public void fetchSavedCards(WorkFlowRequestBean workFlowBean, UserDetailsBiz userDetails, boolean fetchSavedCards) {
        setUserDetailsToCache(userDetails);
        getAndSetUserProfile(userDetails);

        if (fetchSavedCards) {
            long startTime = System.currentTimeMillis();
            if (workFlowBean.isStoreCardPrefEnabled() && StringUtils.isNotBlank(workFlowBean.getCustID())) {
                fetchSavedCardDetails(userDetails, workFlowBean.getPaytmMID(), workFlowBean.getCustID());
            } else {
                fetchSavedCardDetails(userDetails);
            }
            LOGGER.info("Total time taken for fetching savedCards : {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private void getAndSetUserProfile(UserDetailsBiz userDetails) {
        if (StringUtils.isNotBlank(userDetails.getInternalUserId())) {
            String userProfileKey = new StringBuilder(CommonConstants.LAST_PAYMENT_MODE_KEY).append(
                    userDetails.getInternalUserId()).toString();
            Object userProfile = theiaTransactionalRedisUtil.get(userProfileKey);
            LOGGER.debug("User profile Fetched as :: {} for Key :: {} ", userProfile, userProfileKey);
            if (userProfile != null) {
                userDetails.setUserProfile((UserProfile) userProfile);
            }
        }
    }

    private void setUserDetailsToCache(final FetchUserDetailsResponse fetchUserDetailsResponse) {
        // Store userDetails in Cache
        StringBuilder userDetailsCacheKey = new StringBuilder(CommonConstants.USER_INFO_KEY);
        userDetailsCacheKey.append(fetchUserDetailsResponse.getUserDetails().getUserId());
        theiaTransactionalRedisUtil.set(userDetailsCacheKey.toString(), fetchUserDetailsResponse.getUserDetails(),
                2 * 60 * 60);
    }

    private void setUserDetailsToCache(UserDetailsBiz userDetailsBiz) {
        StringBuilder userDetailsCacheKey = new StringBuilder(CommonConstants.USER_INFO_KEY);
        userDetailsCacheKey.append(userDetailsBiz.getUserId());
        theiaTransactionalRedisUtil.set(userDetailsCacheKey.toString(), userDetailsBiz, 2 * 60 * 60);
    }

    /**
     * @param userDetails
     *            Fetch Saved Cards
     */
    private void fetchSavedCardDetails(UserDetailsBiz userDetails) {
        try {

            GenericCoreResponseBean<List<CardBeanBiz>> savedCards = savedCardsService
                    .fetchSavedCardsByUserId(userDetails.getUserId());
            if (savedCards.isSuccessfullyProcessed()
                    && BizParamValidator.validateInputListParam(savedCards.getResponse())) {
                userDetails.setMerchantViewSavedCardsList(savedCards.getResponse());
            }
        } catch (Exception e) {
            LOGGER.error("Exception ", e);
        }
    }

    public void fetchSavedCardDetails(UserDetailsBiz userDetails, String mId, String custId) {
        try {

            GenericCoreResponseBean<List<CardBeanBiz>> savedCards = savedCardsService.fetchSavedCardsByMidCustIdUserId(
                    mId, custId, userDetails.getUserId());
            if (savedCards.isSuccessfullyProcessed()
                    && BizParamValidator.validateInputListParam(savedCards.getResponse())) {
                userDetails.setMerchantViewSavedCardsList(savedCards.getResponse());
            }
        } catch (Exception e) {
            LOGGER.error("Exception ", e);
        }
    }

    @Override
    @Loggable(logLevel = Loggable.INFO, state = TxnState.AUTH_FETCH_USER_DETAIL_NO_SAVED_CARD)
    public GenericCoreResponseBean<UserDetailsBiz> fetchUserDetailsNoSavedCards(WorkFlowRequestBean requestBean,
            String token) {
        String clientId = requestBean.getOauthClientId();
        String clientSecret = requestBean.getOauthSecretKey();

        if (StringUtils.isBlank(token)) {
            LOGGER.error("Token is blank");
            return new GenericCoreResponseBean<>("InvalidTokenProvided", ResponseConstants.SYSTEM_ERROR);
        }
        final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(token, clientId,
                clientSecret, requestBean.getPaytmMID());
        try {

            final FetchUserDetailsResponse fetchUserDetailsResponse;
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                LOGGER.debug("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.info("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);
            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (fetchUserDetailsResponse.getUserDetails() == null)) {
                LOGGER.error("User details fetching failed or UserDetails is null");
                return new GenericCoreResponseBean<>(fetchUserDetailsResponse.getResponseMessage(),
                        ResponseConstants.INVALID_SSO_TOKEN);
            }

            upsHelper.updateUserPostpaidAccStatusFromUPS(fetchUserDetailsRequest.getMid(),
                    fetchUserDetailsResponse.getUserDetails());

            /**
             * Might be used if user details not fetched in Initiate Transaction
             * Request
             */
            if (StringUtils.isNotBlank(requestBean.getCorporateCustId())) {
                LOGGER.debug("Corporate Advance Deposit Request. Setting corporate CustId as CustId");
                String userId = fetchUserDetailsResponse.getUserDetails().getUserId();
                fetchUserDetailsResponse.getUserDetails().setUserId(requestBean.getCorporateCustId());
                fetchUserDetailsResponse.getUserDetails().setUserTypes(Collections.emptyList());
                fetchUserDetailsResponse.getUserDetails().setChildUserId(userId);
                fetchUserDetailsResponse.getUserDetails().setPaytmCCEnabled(false);
            }
            setUserDetailsToCache(fetchUserDetailsResponse);

            UserDetailsBiz userDetails = mappingUtil.mapUserDetails(fetchUserDetailsResponse.getUserDetails());
            getAndSetUserProfile(userDetails);

            return new GenericCoreResponseBean<>(userDetails);
        } catch (final Exception e) {
            LOGGER.error("Exception : ", e);
            return new GenericCoreResponseBean<>("Could not fetch user details", ResponseConstants.SYSTEM_ERROR);
        }
    }

    @Loggable(logLevel = Loggable.INFO, state = TxnState.AUTH_VERIFY_USER_LOGIN)
    @Override
    public GenericCoreResponseBean<VerifyLoginResponseBizBean> verfifyLogin(
            final VerifyLoginRequestBizBean verifyLoginReqBean) {
        // Create OAuthRequest model from Biz model..
        final ValidateAuthCodeRequest validateAuthReqbean = OAuthHelperServiceImpl.createOAuthRequestBean(
                verifyLoginReqBean.getoAuthCode(), verifyLoginReqBean.getClientID(), verifyLoginReqBean.getSecretKey());
        GenericCoreResponseBean<VerifyLoginResponseBizBean> response = null;
        GenericCoreResponseBean<List<CardBeanBiz>> savedCards = null;
        try {
            final ValidateAuthCodeResponse validateAuthRespBean = authFacade.validateAuthCode(validateAuthReqbean);
            LOGGER.debug("Validate AuthCodeResponse Bean is::{}", validateAuthRespBean);
            if (!validateAuthRespBean.isSuccessfullyProcessed()) {
                throw new InvalidFacadeResponseException("InvalidValidateAuthCodeResponse");
            }

            final FetchAllTokensRequest fetchAllTokenReq = OAuthHelperServiceImpl.createfetchAllTokensRequest(
                    validateAuthRespBean.getAccessToken().getToken(), verifyLoginReqBean.getClientID(),
                    verifyLoginReqBean.getSecretKey());
            final FetchAllTokensResponse fetchAllTokensResponse = authFacade.fetchAllTokens(fetchAllTokenReq);
            LOGGER.debug("FetchAllToken Response Bean is::{}", fetchAllTokensResponse);
            if (!fetchAllTokensResponse.isSuccessfullyProcessed()) {
                throw new InvalidFacadeResponseException("InvalidFetchAllTokensResponse");
            }
            final FetchUserDetailsRequest fetchUserDetailsRequest = OAuthHelperServiceImpl
                    .createFetchUserDetailsRequest(fetchAllTokensResponse.getToken(BizConstant.WALLET_TOKEN_SCOPE)
                            .getToken(), verifyLoginReqBean.getClientID(), verifyLoginReqBean.getSecretKey(),
                            verifyLoginReqBean.getmId());
            final FetchUserDetailsResponse fetchUserDetailsResponse;
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                LOGGER.debug("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.debug("FetchUserDetails Response Bean is::{}", fetchAllTokensResponse);
            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()) {
                throw new InvalidFacadeResponseException("InvalidFetchUserDetailsResponse");
            }

            final VerifyLoginResponseBizBean verifyLoginResponseBean = oAuthHelperServiceImpl
                    .createVerifyLoginResponseBean(validateAuthRespBean.getAccessToken(), fetchAllTokensResponse,
                            fetchUserDetailsResponse.getUserDetails());
            LOGGER.debug("Returning VerifyLoginResponseBizBean as ::{}", verifyLoginResponseBean);

            UserDetailsBiz userDetails = verifyLoginResponseBean.getUserDetails();
            userDetails.setUserToken(fetchAllTokensResponse.getToken(BizConstant.PAYTM_TOKEN_SCOPE).getToken());

            upsHelper.updateUserPostpaidAccStatusFromUPS(fetchUserDetailsRequest.getMid(),
                    fetchUserDetailsResponse.getUserDetails());

            setUserDetailsToCache(fetchUserDetailsResponse);

            // Fetch Saved Cards
            if (verifyLoginReqBean.isStoreCardPrefEnabled() && StringUtils.isNotBlank(verifyLoginReqBean.getCustId())
                    && StringUtils.isNotBlank(verifyLoginReqBean.getmId())) {
                savedCards = savedCardsService.fetchSavedCardsByMidCustIdUserId(verifyLoginReqBean.getmId(),
                        verifyLoginReqBean.getCustId(), userDetails.getUserId());
            } else {
                savedCards = savedCardsService.fetchSavedCardsByUserId(userDetails.getUserId());
            }
            if (savedCards.isSuccessfullyProcessed()
                    && BizParamValidator.validateInputListParam(savedCards.getResponse())) {
                userDetails.setMerchantViewSavedCardsList(savedCards.getResponse());
            }
            verifyLoginResponseBean.setUserDetails(userDetails);

            response = new GenericCoreResponseBean<>(verifyLoginResponseBean);
        } catch (final Exception e) {
            LOGGER.error("Error occured while processing verifyLoginFlow : ", e);
            response = new GenericCoreResponseBean<>(e.getMessage(), ResponseConstants.SYSTEM_ERROR);
        }

        return response;
    }

}
