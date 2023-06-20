package com.paytm.pgplus.theia.nativ;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.utils.MappingUtil;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequestViaEmailOrPhone;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequestViaPhoneOrUserId;
import com.paytm.pgplus.facade.user.models.response.FetchUserDetailsResponse;
import com.paytm.pgplus.facade.user.models.response.OauthCustIDContactResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.mappingserviceclient.service.IUserMapping;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

@Component
public class OAuthHelper implements IOAuthHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthHelper.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(OAuthHelper.class);

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authFacade;

    @Autowired
    @Qualifier("userMappingService")
    IUserMapping userMapping;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("mapUtilsBiz")
    private MappingUtil mappingUtil;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("upsHelper")
    private UPSHelper upsHelper;

    public UserDetailsBiz validateSSOToken(final String token, InitiateTransactionRequestBody body)
            throws PaymentRequestProcessingException {
        if (StringUtils.isBlank(token)) {
            LOGGER.error("Token provided is blank");
            throw PaymentRequestProcessingException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(token,
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_ID).getValue(),
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue(),
                body.getMid());
        try {
            final FetchUserDetailsResponse fetchUserDetailsResponse;
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                EXT_LOGGER.customInfo("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.info("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);

            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (Objects.isNull(fetchUserDetailsResponse.getUserDetails()))) {
                LOGGER.error("User details fetching failed or UserDetails is null");
                throw PaymentRequestProcessingException.getException(ResultCode.INVALID_SSO_TOKEN);
            }
            LOGGER.debug("User authentication successfully");

            upsHelper.updateUserPostpaidAccStatusFromUPS(body.getMid(), fetchUserDetailsResponse.getUserDetails());

            if (StringUtils.isNotBlank(body.getCorporateCustId())) {
                LOGGER.info("Corporate Advance Deposit Request. Setting corporate CustId as CustId and setting userType as Null");
                String userId = fetchUserDetailsResponse.getUserDetails().getUserId();
                fetchUserDetailsResponse.getUserDetails().setUserTypes(Collections.emptyList());
                fetchUserDetailsResponse.getUserDetails().setUserId(body.getCorporateCustId());
                fetchUserDetailsResponse.getUserDetails().setChildUserId(userId);
                fetchUserDetailsResponse.getUserDetails().setPaytmCCEnabled(false);
            }

            return mappingUtil.mapUserDetails(fetchUserDetailsResponse.getUserDetails());

        } catch (final PaymentRequestProcessingException e) {
            throw e;
        } catch (final Exception e) {
            throw new BaseException();
        }
    }

    @Override
    public UserDetailsBiz validateSSOToken(InitiateTransactionRequestBody body)
            throws PaymentRequestProcessingException {
        if (StringUtils.isBlank(body.getPaytmSsoToken())) {
            LOGGER.error("Token provided is blank");
            throw PaymentRequestProcessingException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(body.getPaytmSsoToken(),
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_ID).getValue(),
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue(),
                body.getMid());
        try {
            final FetchUserDetailsResponse fetchUserDetailsResponse;

            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                EXT_LOGGER.customInfo("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.debug("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);
            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (Objects.isNull(fetchUserDetailsResponse.getUserDetails()))) {
                LOGGER.error("User details fetching failed or UserDetails is null");
                throw PaymentRequestProcessingException.getException(ResultCode.INVALID_SSO_TOKEN);
            }
            LOGGER.debug("User authentication successfully");

            upsHelper.updateUserPostpaidAccStatusFromUPS(body.getMid(), fetchUserDetailsResponse.getUserDetails());

            if (StringUtils.isNotBlank(body.getCorporateCustId())) {
                LOGGER.info("Corporate Advance Deposit Request. Setting corporate CustId as CustId and setting userType as Null");
                String userId = fetchUserDetailsResponse.getUserDetails().getUserId();
                fetchUserDetailsResponse.getUserDetails().setUserTypes(Collections.emptyList());
                fetchUserDetailsResponse.getUserDetails().setUserId(body.getCorporateCustId());
                fetchUserDetailsResponse.getUserDetails().setChildUserId(userId);
                fetchUserDetailsResponse.getUserDetails().setPaytmCCEnabled(false);
            }

            return mappingUtil.mapUserDetails(fetchUserDetailsResponse.getUserDetails());

        } catch (final PaymentRequestProcessingException e) {
            throw e;
        } catch (final Exception e) {
            throw new BaseException();
        }
    }

    @Override
    @Deprecated
    public UserDetailsBiz validateSSOToken(String token) throws PaymentRequestProcessingException {
        if (StringUtils.isBlank(token)) {
            LOGGER.error("Token provided is blank");
            throw PaymentRequestProcessingException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(token,
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_ID).getValue(),
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue());
        try {
            FetchUserDetailsResponse fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.debug("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);
            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (Objects.isNull(fetchUserDetailsResponse.getUserDetails()))) {
                LOGGER.error("User details fetching failed or UserDetails is null");
                throw PaymentRequestProcessingException.getException(ResultCode.INVALID_SSO_TOKEN);
            }
            LOGGER.debug("User authentication successfully");

            return mappingUtil.mapUserDetails(fetchUserDetailsResponse.getUserDetails());

        } catch (final PaymentRequestProcessingException e) {
            throw e;
        } catch (final Exception e) {
            throw new BaseException();
        }
    }

    @Override
    public UserDetailsBiz validateSSOToken(String token, String mid) throws PaymentRequestProcessingException {
        if (StringUtils.isBlank(token)) {
            LOGGER.error("Token provided is blank");
            throw PaymentRequestProcessingException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        final FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(token,
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_ID).getValue(),
                configurationDataService.getPaytmProperty(ExtraConstants.OAUTH_CLIENT_SECRET_KEY).getValue(), mid);
        try {
            final FetchUserDetailsResponse fetchUserDetailsResponse;
            if (StringUtils.isBlank(fetchUserDetailsRequest.getMid())
                    || ff4jUtils.isFeatureEnabledOnMid(fetchUserDetailsRequest.getMid())) {
                EXT_LOGGER.customInfo("oAuth V2 enabled, Mid Available {}",
                        StringUtils.isNotBlank(fetchUserDetailsRequest.getMid()));
                fetchUserDetailsResponse = authFacade.fetchUserDetailsV2(fetchUserDetailsRequest);
            } else
                fetchUserDetailsResponse = authFacade.fetchUserDetails(fetchUserDetailsRequest);
            LOGGER.debug("Response for fetch UserDetails :: {} ", fetchUserDetailsResponse);
            if (!fetchUserDetailsResponse.isSuccessfullyProcessed()
                    || (Objects.isNull(fetchUserDetailsResponse.getUserDetails()))) {
                LOGGER.error("User details fetching failed or UserDetails is null");
                throw PaymentRequestProcessingException.getException(ResultCode.INVALID_SSO_TOKEN);
            }
            LOGGER.debug("User authentication successfully");

            upsHelper.updateUserPostpaidAccStatusFromUPS(mid, fetchUserDetailsResponse.getUserDetails());

            return mappingUtil.mapUserDetails(fetchUserDetailsResponse.getUserDetails());

        } catch (final PaymentRequestProcessingException e) {
            throw e;
        } catch (final Exception e) {
            if (ff4jUtils.isFeatureEnabled("ACCESS_TOKEN_DEBUG_TRACE", false))
                LOGGER.error("Exception while fetching userDetails :", e);
            throw new BaseException();
        }
    }

    @Override
    public UserDetailsV2 fetchUserDetailsViaPhoneOrUserId(String mobileNo, String userId) throws FacadeCheckedException {

        if (StringUtils.isBlank(mobileNo) && StringUtils.isBlank(userId)) {
            LOGGER.error("Either mobile number or userId should not be blank");
            throw PaymentRequestProcessingException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        String clientId = configurationDataService.getPaytmProperty(
                TheiaConstant.ExtraConstants.OAUTH_WHITELISTED_CLIENT_ID).getValue();
        String clientSecret = configurationDataService.getPaytmProperty(
                TheiaConstant.ExtraConstants.OAUTH_WHITELISTED_CLIENT_SECRET_KEY).getValue();

        FetchUserDetailsRequestViaPhoneOrUserId fetchUserDetailsRequest = new FetchUserDetailsRequestViaPhoneOrUserId(
                userId, mobileNo, clientId, clientSecret);

        UserDetailsV2 userDetails = authFacade.fetchUserDetailsViaPhoneOrUserId(fetchUserDetailsRequest);

        if (ObjectUtils.equals(userDetails, null)) {
            LOGGER.error("Unable to fetch user details from oauth");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        return userDetails;
    }

    @Override
    public String fetchUserIdViaPhone(String mobileNo) throws FacadeCheckedException {

        String clientId = configurationDataService.getPaytmProperty(
                TheiaConstant.ExtraConstants.OAUTH_WHITELISTED_CLIENT_ID).getValue();
        String clientSecret = configurationDataService.getPaytmProperty(
                TheiaConstant.ExtraConstants.OAUTH_WHITELISTED_CLIENT_SECRET_KEY).getValue();

        FetchUserDetailsRequestViaEmailOrPhone fetchUserDetailsRequest = new FetchUserDetailsRequestViaEmailOrPhone(
                null, mobileNo, clientId, clientSecret);

        OauthCustIDContactResponse userDetails = authFacade.fetchUserDetailsViaEmailOrPhone(fetchUserDetailsRequest);

        if (ObjectUtils.equals(userDetails, null) || StringUtils.isBlank(userDetails.getCustID())) {
            LOGGER.error("userId received Blank from Oauth for MobileNo: {}", mobileNo);
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        return userDetails.getCustID();
    }

}
