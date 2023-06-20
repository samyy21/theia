package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.Ff4jUtils;
import com.paytm.pgplus.biz.workflow.service.helper.WorkFlowHelper;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditRequest;
import com.paytm.pgplus.facade.postpaid.model.PaytmDigitalCreditResponse;
import com.paytm.pgplus.facade.user.models.UserDetailsV2;
import com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.logging.ExceptionLogUtils;
import com.paytm.pgplus.theia.nativ.OAuthHelper;
import com.paytm.pgplus.theia.nativ.UPSHelper;
import com.paytm.pgplus.theia.nativ.model.user.*;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.paytm.pgplus.biz.utils.BizConstant.Ff4jFeature.ENABLE_GET_USER_POSTPAID_STATUS_FROM_UPS;
import static com.paytm.pgplus.facade.constants.FacadeConstants.AUTH_USER_TYPE_POSTPAID_USER;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.PaytmDigitalCreditConstant.POSTPAID_STATUS_LIVE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.PreferenceValue.ACTIVE;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UserPayModeStatusConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.FF4J.BLOCK_USER_POSTPAID_STATUS_CHECK_ON_MID;

@Service("fetchUserPayModeStatusRequestProcessor")
public class FetchUserPaymodeStatusRequestProcessor
        extends
        AbstractRequestProcessor<UserPayModeStatusRequest, UserPayModeStatusResponse, UserPayModeStatusRequest, UserPayModeStatusResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchUserPaymodeStatusRequestProcessor.class);

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private OAuthHelper oauthHelper;

    @Autowired
    @Qualifier("merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    private Ff4jUtils ff4jUtils;

    @Autowired
    @Qualifier("upsHelper")
    private UPSHelper upsHelper;

    @Autowired
    @Qualifier("workFlowHelper")
    private WorkFlowHelper workFlowHelper;

    private static final String MOBILE_NO_REGEX = "^[6-9][0-9]{9}$";
    private static final String CUST_ID_REGEX = "^[a-zA-Z0-9-|_@.-]*$";

    @Override
    protected UserPayModeStatusRequest preProcess(UserPayModeStatusRequest request) throws Exception {

        validateRequest(request);
        return request;
    }

    @Override
    protected UserPayModeStatusResponse onProcess(UserPayModeStatusRequest request,
            UserPayModeStatusRequest serviceRequest) throws Exception {
        UserPayModeStatusResponse serviceResponse = new UserPayModeStatusResponse(new UserPayModeStatusResponseBody());
        List<UserPayModeStatus> payModeStatus = new ArrayList<>();
        String mid = request.getBody().getMid();

        for (String payMode : request.getBody().getPaymentMode()) {

            if (StringUtils.equals(payMode, EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod())) {

                if (!merchantPreferenceService.isUserPostpaidStatusCheckEnabled(mid)
                        || ff4jUtils.isFeatureEnabledOnMid(mid, BLOCK_USER_POSTPAID_STATUS_CHECK_ON_MID, false)) {
                    LOGGER.info("User Postpaid Account Status check is disabled for the Merchant :{} ", mid);
                    break;
                }

                if (ff4jUtils.isFeatureEnabledOnMid(mid, ENABLE_GET_USER_POSTPAID_STATUS_FROM_UPS, false)) {
                    getUserPostpaidStatusFromUPS(request.getBody(), payModeStatus);
                } else {
                    getUserPostpaidStatusFromOAuth(request.getBody(), payModeStatus);
                }
                if (Objects.nonNull(payModeStatus) && Objects.nonNull(payModeStatus.get(0))) {
                    UserPayModeStatus userPayModeStatus = payModeStatus.get(0);
                    if (StringUtils.equals(USER_PAYMODE_ACTIVE_STATUS, userPayModeStatus.getStatus())
                            && StringUtils.isNotBlank(request.getBody().getUserId())) {
                        checkDigitalCreditLimit(request, payModeStatus);
                    }
                }

            } else {
                LOGGER.error("requested payMode :{} is invaild", payMode);
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }

        }

        serviceResponse.getBody().setPaymentModeStatus(payModeStatus);

        return serviceResponse;
    }

    @Override
    protected UserPayModeStatusResponse postProcess(UserPayModeStatusRequest request,
            UserPayModeStatusRequest serviceRequest, UserPayModeStatusResponse serviceResponse) throws Exception {

        serviceResponse.setHead(new ResponseHeader());
        if (ObjectUtils.equals(serviceResponse, null)
                || ObjectUtils.equals(serviceResponse.getBody().getPaymentModeStatus(), null)
                || CollectionUtils.isEmpty(serviceResponse.getBody().getPaymentModeStatus())) {
            throw RequestValidationException.getException("SYSTEM ERROR");
        }
        return serviceResponse;
    }

    private void validateRequest(UserPayModeStatusRequest request) {
        validateMandatoryParams(request);
        validateToken(request);
        validateMobileNumber(request.getBody().getMobileNo());
        validateUserId(request.getBody().getUserId());
    }

    private void validateMandatoryParams(UserPayModeStatusRequest request) {
        if (ObjectUtils.equals(request, null) || ObjectUtils.equals(request.getHead(), null)
                || ObjectUtils.equals(request.getBody(), null)) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        if (StringUtils.isBlank(request.getHead().getToken())
                || ObjectUtils.equals(request.getHead().getTokenType(), null))
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);

        if (ObjectUtils.equals(request.getBody().getMid(), null)
                || CollectionUtils.isEmpty(request.getBody().getPaymentMode())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
    }

    private void validateToken(UserPayModeStatusRequest request) {

        if (TokenType.CHECKSUM.equals(request.getHead().getTokenType())) {
            // Checksum validated at Interceptor

        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            if (StringUtils.isBlank(request.getBody().getReferenceId())) {
                LOGGER.error("referenceId is mandatory");
                throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
            }
            accessTokenUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(),
                    request.getHead().getToken());
        } else if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            String ssoToken = request.getHead().getToken();
            String mid = request.getBody().getMid();
            UserDetailsBiz userDetails = null;

            userDetails = nativeValidationService.validateSSOToken(ssoToken, mid);

            if (ObjectUtils.notEqual(userDetails, null) && StringUtils.isNotEmpty(userDetails.getUserId())) {
                request.getBody().setUserId(userDetails.getUserId());
            } else {
                LOGGER.error("Invalid SSO Token");
                throw RequestValidationException.getException(ResultCode.INVALID_SSO_TOKEN);
            }

        } else {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION);
        }

    }

    private boolean isUserPostpaidAccActive(UserDetailsV2 userDetails) {

        if (ObjectUtils.notEqual(userDetails.getUserAttributeInfo(), null)
                && ObjectUtils.notEqual(userDetails.getUserAttributeInfo().isCreditCard(), null)) {
            if (userDetails.getUserAttributeInfo().isCreditCard()) {
                return true;
            } else if (CollectionUtils.isNotEmpty(userDetails.getUserTypes())
                    && userDetails.getUserTypes().contains(AUTH_USER_TYPE_POSTPAID_USER)) {

                if (StringUtils.isNotBlank(userDetails.getUserAttributeInfo().getPostpaidStatus())
                        && StringUtils.equals(userDetails.getUserAttributeInfo().getPostpaidStatus(),
                                POSTPAID_STATUS_LIVE)) {
                    return true;
                }

            }
        }
        return false;
    }

    private void validateMobileNumber(final String mobileNumber) {
        if (StringUtils.isNotBlank(mobileNumber)) {
            if (mobileNumber.matches(MOBILE_NO_REGEX)) {
                return;
            }
            LOGGER.error("Mobile No : {} is not a valid value.", mobileNumber);
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private void validateUserId(String userId) {
        if (StringUtils.isNotBlank(userId)) {
            if (userId.matches(CUST_ID_REGEX)) {
                return;
            }
            LOGGER.error("USER ID : {} is not a valid value.", userId);
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private void getUserPostpaidStatusFromOAuth(UserPayModeStatusRequestBody requestBody,
            List<UserPayModeStatus> payModeStatus) {
        try {
            String mobileNo = requestBody.getMobileNo();
            String userId = requestBody.getUserId();
            UserDetailsV2 userDetails = oauthHelper.fetchUserDetailsViaPhoneOrUserId(mobileNo, userId);

            if (ObjectUtils.notEqual(userDetails, null)) {
                if (Objects.nonNull(userDetails.getAccessToken())
                        && StringUtils.isNotBlank(userDetails.getAccessToken().getUserId())) {
                    requestBody.setUserId(userDetails.getAccessToken().getUserId());
                }
                UserPayModeStatus userPostpaidAccStatus = new UserPayModeStatus();
                userPostpaidAccStatus.setPaymentMode(EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod());

                if (isUserPostpaidAccActive(userDetails)) {
                    userPostpaidAccStatus.setStatus(USER_PAYMODE_ACTIVE_STATUS);
                } else {
                    userPostpaidAccStatus.setStatus(USER_PAYMODE_INACTIVE_STATUS);
                }
                payModeStatus.add(userPostpaidAccStatus);
            }

        } catch (PaymentRequestProcessingException e) {
            LOGGER.error("Exception occurred while getting user postpaid account status {}", e);
            throw RequestValidationException.getException(e.getResultInfo());
        } catch (RequestValidationException e) {
            LOGGER.error("Exception occurred while getting user postpaid account status {}", e);
            throw RequestValidationException.getException(e.getResultInfo());
        } catch (Exception e) {
            LOGGER.error("Exception occurred while getting user postpaid account status {}", e);
            throw RequestValidationException.getException("SYSTEM ERROR");
        }
    }

    private void getUserPostpaidStatusFromUPS(UserPayModeStatusRequestBody requestBody,
            List<UserPayModeStatus> payModeStatus) throws FacadeCheckedException {
        String mobileNo = requestBody.getMobileNo();
        String userId = requestBody.getUserId();
        if (StringUtils.isBlank(mobileNo) && StringUtils.isBlank(userId)) {
            LOGGER.error("Either mobile number or userId should not be blank");
            throw PaymentRequestProcessingException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        if (StringUtils.isBlank(userId)) {
            LOGGER.info("UserId received as blank calling oauth service with mobile No :{}", mobileNo);
            userId = oauthHelper.fetchUserIdViaPhone(mobileNo);
            if (StringUtils.isNotBlank(userId)) {
                requestBody.setUserId(userId);
            }
        }

        String postpaidStatus = upsHelper.getUserPostpaidAccStatusFromUPS(userId);

        if (StringUtils.isBlank(postpaidStatus)) {
            LOGGER.error("Unable to fetch User Postpaid status from Ups for userId: {}", userId);
            throw RequestValidationException.getException("SYSTEM ERROR");
        }

        UserPayModeStatus userPostpaidAccStatus = new UserPayModeStatus();
        userPostpaidAccStatus.setPaymentMode(EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod());
        if (StringUtils.equals(postpaidStatus, ACTIVE)) {
            userPostpaidAccStatus.setStatus(USER_PAYMODE_ACTIVE_STATUS);
        } else {
            userPostpaidAccStatus.setStatus(USER_PAYMODE_INACTIVE_STATUS);
        }

        payModeStatus.add(userPostpaidAccStatus);
    }

    public void checkDigitalCreditLimit(UserPayModeStatusRequest request, List<UserPayModeStatus> payModeStatus) {
        PaytmDigitalCreditRequest paytmDigitalCreditRequest = getPaytmDigitalCreditCardRequest(request);
        try {
            PaytmDigitalCreditResponse digitalCreditResponse = workFlowHelper.getPaytmDigitalCreditBalanceResponse(
                    paytmDigitalCreditRequest, request.getBody().getUserId());
            if (Objects.nonNull(digitalCreditResponse)
                    && CollectionUtils.isNotEmpty(digitalCreditResponse.getResponse())
                    && Objects.nonNull(digitalCreditResponse.getResponse().get(0))
                    && Objects.nonNull(digitalCreditResponse.getResponse().get(0).getAccountStatus())) {
                if (!StringUtils.equals(digitalCreditResponse.getResponse().get(0).getAccountStatus(),
                        USER_PAYMODE_ACTIVE_STATUS)) {
                    payModeStatus.get(0).setStatus(USER_PAYMODE_INACTIVE_STATUS);
                } else if (merchantPreferenceService.isAllowPPLimitCheckInEligiblityApi(request.getBody().getMid(),
                        false)
                        && Objects.nonNull(digitalCreditResponse.getResponse().get(0).getAmount())
                        && StringUtils.isNotBlank(request.getBody().getTxnAmount())) {
                    if (digitalCreditResponse.getResponse().get(0).getAmount() < Double.parseDouble(request.getBody()
                            .getTxnAmount())) {
                        payModeStatus.get(0).setStatus(USER_PAYMODE_INSUFFICIENT_LIMIT);
                    }
                } else {
                    payModeStatus.get(0).setStatus(USER_PAYMODE_ACTIVE_STATUS);
                }
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception while fetching Postpaid Balance  ", ExceptionLogUtils.limitLengthOfStackTrace(e));
        }
    }

    public PaytmDigitalCreditRequest getPaytmDigitalCreditCardRequest(UserPayModeStatusRequest request) {
        PaytmDigitalCreditRequest paytmDigitalCreditRequest = new PaytmDigitalCreditRequest();
        paytmDigitalCreditRequest.setFlowType(TheiaConstant.PaytmDigitalCreditConstant.FLOW_TYPE_TRANSACTION);
        paytmDigitalCreditRequest.setPgmid(request.getBody().getMid());
        if (StringUtils.isNotBlank(request.getBody().getTxnAmount())) {
            paytmDigitalCreditRequest.setAmount(Double.parseDouble(request.getBody().getTxnAmount()));
        }
        return paytmDigitalCreditRequest;
    }

}
