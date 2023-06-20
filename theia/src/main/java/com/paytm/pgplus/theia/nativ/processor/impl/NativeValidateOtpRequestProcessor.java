package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.enums.BankTransferCheckoutFlow;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.BasicAndTokenInfo;
import com.paytm.pgplus.facade.user.models.BasicAndTokenInfoResponse;
import com.paytm.pgplus.facade.user.models.request.FetchUserDetailsRequest;
import com.paytm.pgplus.facade.user.models.request.ValidateAuthCodeRequest;
import com.paytm.pgplus.facade.user.models.request.ValidateLoginOtpRequest;
import com.paytm.pgplus.facade.user.models.response.*;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.helper.PGPreferenceHelper;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpRequest;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpResponse;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpResponseBody;
import com.paytm.pgplus.theia.nativ.model.auth.ValidateOtpServiceResponse;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.subscription.INativeSubscriptionHelper;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.DYNAMIC_QR_REQUIRED;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.CHECKOUT;

@Service("nativeValidateOtpRequestProcessor")
public class NativeValidateOtpRequestProcessor
        extends
        AbstractRequestProcessor<ValidateOtpRequest, ValidateOtpResponse, ValidateLoginOtpRequest, ValidateOtpServiceResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeValidateOtpRequestProcessor.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(NativeValidateOtpRequestProcessor.class);
    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private NativePayviewConsultRequestProcessor nativePayviewConsultRequestProcessor;

    @Autowired
    private NativePayviewConsultRequestProcessorV5 nativePayviewConsultRequestProcessorV5;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Autowired
    @Qualifier("nativeSubscriptionHelper")
    private INativeSubscriptionHelper nativeSubscriptionHelper;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private PGPreferenceHelper pgPreferenceHelper;

    @Override
    protected ValidateLoginOtpRequest preProcess(ValidateOtpRequest request) {
        if (StringUtils.isBlank(request.getBody().getOtp()) && StringUtils.isBlank(request.getBody().getAuthCode())) {
            throw com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException
                    .getException(com.paytm.pgplus.theia.accesstoken.enums.ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        InitiateTransactionRequestBody orderDetail = validate(request);

        request.getBody().setOriginalVersion(request.getHead().getVersion());
        if (TheiaConstant.RequestHeaders.Version_V3.equals(request.getHead().getVersion())) {
            request.getHead().setVersion(TheiaConstant.RequestHeaders.Version_V2);
        }
        ValidateLoginOtpRequest serviceRequest = null;

        if (StringUtils.isNotBlank(request.getBody().getOtp()) && orderDetail != null) {
            serviceRequest = requestTransform(request, orderDetail.getOrderId());
        } else {
            serviceRequest = requestTransform(request, null);
        }

        setParamsForNativeSubsRequest(orderDetail, serviceRequest);

        return serviceRequest;
    }

    @Override
    protected ValidateOtpServiceResponse onProcess(ValidateOtpRequest request, ValidateLoginOtpRequest serviceRequest)
            throws Exception {
        long startTime = System.currentTimeMillis();

        ValidateLoginOtpResponse validateLoginOtpResponse = null;
        ValidateOtpServiceResponse validateOtpServiceResponse = new ValidateOtpServiceResponse();

        try {
            if (StringUtils.isNotBlank(request.getBody().getOtp())) {
                validateLoginOtpResponse = authService.validateLoginOtp(serviceRequest);
            } else {
                validateLoginOtpResponse = validateLoginViaAuthCode(request);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occured while validating login OTP/AuthCode", e);
            validateLoginOtpResponse = new ValidateLoginOtpResponse();
            if (StringUtils.isNotBlank(request.getBody().getOtp())) {
                validateLoginOtpResponse.setMessage(TheiaConstant.ExtraConstants.OTP_VALIDATION_EXCEPTION_MESSAGE);
            } else {
                validateLoginOtpResponse.setMessage(TheiaConstant.ExtraConstants.AUTHCODE_VALIDATION_EXCEPTION_MESSAGE);
            }
            validateLoginOtpResponse.setStatus(TheiaConstant.ResponseConstants.ResponseCodes.SC_INTERNAL_SERVER_ERROR);
            validateOtpServiceResponse.setValidateLoginOtpResponse(validateLoginOtpResponse);
            return validateOtpServiceResponse;
        }

        if (validateLoginOtpResponse.getResponseData() != null
                && !CollectionUtils.isEmpty(validateLoginOtpResponse.getResponseData().getAccess_token())) {

            updateSsoTokenInCache(request, validateLoginOtpResponse);

            if (StringUtils.equals(CHECKOUT, request.getHead().getWorkFlow())) {
                HttpServletRequest httpServletRequest = OfflinePaymentUtils.gethttpServletRequest();
                httpServletRequest.setAttribute(DYNAMIC_QR_REQUIRED, false);
            }

            NativeCashierInfoResponse cashierData;
            NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = getNativePayviewConsultRequest(request);
            if (TheiaConstant.RequestHeaders.Version_V5.equals(request.getHead().getVersion()))
                cashierData = nativePayviewConsultRequestProcessorV5.process(nativeCashierInfoContainerRequest);
            else
                cashierData = nativePayviewConsultRequestProcessor.process(nativeCashierInfoContainerRequest);

            setCookieAfterValidatingOtp(request, validateLoginOtpResponse);

            markRequestValidatedForSubscription(request, serviceRequest);

            validateOtpServiceResponse.setCashierInfoResponse(cashierData);

        }
        LOGGER.info("Total time taken for ValidateOtpRequest auth call is {} ms", System.currentTimeMillis()
                - startTime);

        validateOtpServiceResponse.setValidateLoginOtpResponse(validateLoginOtpResponse);

        return validateOtpServiceResponse;
    }

    private ValidateLoginOtpResponse validateLoginViaAuthCode(ValidateOtpRequest request) throws FacadeCheckedException {
        ValidateAuthCodeResponse validateAuthCodeResponse = getValidateAuthCodeResponse(request);
        BasicAndTokenInfoResponse basicAndTokenInfoResponse = getBasicAndTokenInfo(validateAuthCodeResponse);
        ValidateLoginOtpResponse validateLoginOtpResponse = mapToValidOtpResponse(validateAuthCodeResponse,
                basicAndTokenInfoResponse);
        return validateLoginOtpResponse;
    }

    private ValidateAuthCodeResponse getValidateAuthCodeResponse(ValidateOtpRequest request)
            throws FacadeCheckedException {
        String clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        ValidateAuthCodeRequest validateAuthCodeRequest = new ValidateAuthCodeRequest(request.getBody().getAuthCode(),
                clientId, secretKey);
        ValidateAuthCodeResponse validateAuthCodeResponse = authService.validateAuthCode(validateAuthCodeRequest);
        if (!validateAuthCodeResponse.isSuccessfullyProcessed()) {
            throw new FacadeCheckedException(validateAuthCodeResponse.getResponseMessage());
        }
        EXT_LOGGER.customInfo("Successfully validated authCode");
        return validateAuthCodeResponse;
    }

    private BasicAndTokenInfoResponse getBasicAndTokenInfo(ValidateAuthCodeResponse validateAuthCodeResponse)
            throws FacadeCheckedException {
        String clientId = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_ID);
        String secretKey = configurationDataService.getPaytmPropertyValue(ExtraConstants.OAUTH_CLIENT_SECRET_KEY);
        FetchUserDetailsRequest fetchUserDetailsRequest = new FetchUserDetailsRequest(validateAuthCodeResponse
                .getAccessToken().getToken(), clientId, secretKey);
        BasicAndTokenInfoResponse basicAndTokenInfoResponse = authService
                .fetchBasicAndTokenInfoByToken(fetchUserDetailsRequest);
        if (!basicAndTokenInfoResponse.isSuccessfullyProcessed()) {
            throw new FacadeCheckedException(basicAndTokenInfoResponse.getResponseMessage());
        }
        EXT_LOGGER.customInfo("Successfully fetched basicAndToken info");
        return basicAndTokenInfoResponse;
    }

    private ValidateLoginOtpResponse mapToValidOtpResponse(ValidateAuthCodeResponse validateAuthCodeResponse,
            BasicAndTokenInfoResponse basicAndTokenInfoResponse) {
        ValidateLoginOtpResponse validateLoginOtpResponse = new ValidateLoginOtpResponse();
        BasicAndTokenInfo basicAndTokenInfo = basicAndTokenInfoResponse.getBasicAndTokenInfo();
        AccessToken accessToken = new AccessToken();
        ResponseData responseData = new ResponseData();
        responseData.setBasicInfo(basicAndTokenInfo.getBasicInfo());
        if (basicAndTokenInfo.getAccessToken() != null) {
            responseData.setUserId(basicAndTokenInfo.getAccessToken().getUserId());
            accessToken.setExpires(basicAndTokenInfo.getAccessToken().getExpiryTime());
        }
        if (validateAuthCodeResponse.getAccessToken() != null) {
            accessToken.setValue(validateAuthCodeResponse.getAccessToken().getToken());
        }
        List<AccessToken> accessTokens = new ArrayList<>();
        accessTokens.add(accessToken);
        responseData.setAccess_token(accessTokens);
        validateLoginOtpResponse.setResponseData(responseData);
        validateLoginOtpResponse.setStatus(ExtraConstants.SUCCESS);
        validateLoginOtpResponse.setResponseCode(Integer.toString(HttpStatus.OK.value()));
        validateLoginOtpResponse.setMessage(validateAuthCodeResponse.getResponseMessage());
        return validateLoginOtpResponse;

    }

    private void updateSsoTokenInCache(ValidateOtpRequest request, ValidateLoginOtpResponse validateLoginOtpResponse) {

        String ssoToken = validateLoginOtpResponse.getResponseData().getAccess_token().get(0).getValue();

        if (TokenType.GUEST == request.getHead().getTokenType() || TokenType.ACCESS == request.getHead().getTokenType()) {
            nativeSessionUtil.setSsoToken(request.getHead().getToken(), ssoToken);
            return;
        }

        InitiateTransactionRequestBody orderDetail = nativeSessionUtil.getOrderDetail(request.getHead().getTxnToken());
        orderDetail.setPaytmSsoToken(ssoToken);
        orderDetail.setSetSSOViaOptLogin(true);
        /**
         * Setting IdentificationNo as MobileNo in PAYTM_CONTROLLED Cases where
         * user was initially not loggedin
         */
        if (orderDetail.getVanInfo() != null) {
            if (BankTransferCheckoutFlow.PAYTM_CONTROLLED.getValue().equals(
                    merchantPreferenceService.getBankTransferCheckoutFlow(orderDetail.getMid()))) {
                orderDetail.getVanInfo().setCheckoutFlow(BankTransferCheckoutFlow.PAYTM_CONTROLLED.getValue());
                orderDetail.getVanInfo().setIdentificationNo(
                        validateLoginOtpResponse.getResponseData().getBasicInfo().getPhone());
            }
        }

        nativeSessionUtil.setOrderDetail(request.getHead().getTxnToken(), orderDetail);

        nativeSessionUtil.updateAuthenticatedFlagInCache(request.getHead().getTxnToken(), true);

        if (ERequestType.NATIVE_SUBSCRIPTION.getType().equals(orderDetail.getRequestType())
                && merchantPreferenceService.isSubscriptionLimitOnWalletEnabled(orderDetail.getMid())) {
            nativeSubscriptionHelper.markSubscriptionAuthorized(request.getHead().getTxnToken(), orderDetail,
                    validateLoginOtpResponse);
        }
    }

    @Override
    protected ValidateOtpResponse postProcess(ValidateOtpRequest request, ValidateLoginOtpRequest serviceRequest,
            ValidateOtpServiceResponse serviceResponse) {
        ValidateOtpResponse response = responseTransformer(request, serviceResponse);
        return response;
    }

    private InitiateTransactionRequestBody validate(ValidateOtpRequest request) {

        if (TokenType.GUEST == request.getHead().getTokenType()) {
            nativeSessionUtil.validateGuestTokenForCheckoutFlow(request.getHead().getToken());
            String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
            nativeValidationService.validateMid(mid);
            return null;
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            validateAccessToken(request);
            return null;
        }

        InitiateTransactionRequestBody orderDetail = null;
        orderDetail = nativeValidationService.validateTxnToken(request.getHead().getTxnToken());
        nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        return orderDetail;
    }

    private ValidateLoginOtpRequest requestTransform(ValidateOtpRequest request, String uniqueId) {
        String clientId = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_SECRET_KEY);
        ValidateLoginOtpRequest serviceRequest = null;
        if (StringUtils.isNotBlank(request.getBody().getOtp())) {
            GenerateLoginOtpResponse generateLoginOtpResponse = getSendOtpResponseFromCache(request);
            String state = generateLoginOtpResponse.getState();
            serviceRequest = new ValidateLoginOtpRequest(request.getBody().getOtp(), state, clientId, secretKey,
                    uniqueId);
        } else {
            serviceRequest = new ValidateLoginOtpRequest(null, request.getBody().getAuthCode(), null, clientId,
                    secretKey, null, false);
        }
        return serviceRequest;
    }

    private GenerateLoginOtpResponse getSendOtpResponseFromCache(ValidateOtpRequest request) {
        String token = request.getHead().getTxnToken();
        if (TokenType.GUEST == request.getHead().getTokenType() || TokenType.ACCESS == request.getHead().getTokenType()) {
            token = request.getHead().getToken();
        }
        return (GenerateLoginOtpResponse) nativeSessionUtil.getSendOtpResponse(token);
    }

    private ValidateOtpResponse responseTransformer(ValidateOtpRequest request,
            ValidateOtpServiceResponse serviceResponse) {

        ValidateLoginOtpResponse ValidateLoginOtpResponse = serviceResponse.getValidateLoginOtpResponse();

        ValidateOtpResponse response = new ValidateOtpResponse();
        ValidateOtpResponseBody responseBody = new ValidateOtpResponseBody();

        if ("SUCCESS".equalsIgnoreCase(ValidateLoginOtpResponse.getStatus())) {
            responseBody.setAuthenticated(true);

            if (request.getBody().isFetchCashierData()) {
                if (serviceResponse.getCashierInfoResponse() != null) {
                    serviceResponse.getCashierInfoResponse().getBody().setResultInfo(null);
                    responseBody.setCashierData(serviceResponse.getCashierInfoResponse().getBody());
                }
            }

            responseBody.setResultInfo(new ResultInfo(ValidateLoginOtpResponse.getStatus(), ValidateLoginOtpResponse
                    .getResponseCode(), ValidateLoginOtpResponse.getMessage()));

            if (TokenType.GUEST == request.getHead().getTokenType()) {
                responseBody.getResultInfo().setResultStatus("S");
            }

        } else {
            responseBody.setAuthenticated(false);
            responseBody.setResultInfo(new ResultInfo(ValidateLoginOtpResponse.getStatus(), ValidateLoginOtpResponse
                    .getResponseCode(), ValidateLoginOtpResponse.getMessage()));

            if (TokenType.GUEST == request.getHead().getTokenType()) {
                responseBody.getResultInfo().setResultStatus("F");
            }
        }
        response.setBody(responseBody);
        response.setHead(new ResponseHeader());
        if (TheiaConstant.RequestHeaders.Version_V2.equals(request.getBody().getOriginalVersion())
                || TheiaConstant.RequestHeaders.Version_V3.equals(request.getBody().getOriginalVersion())) {
            response.getHead().setVersion(request.getBody().getOriginalVersion());
        }
        return response;
    }

    private NativeCashierInfoContainerRequest getNativePayviewConsultRequest(ValidateOtpRequest request) {
        NativeCashierInfoRequest nativeCashierInfoRequest = new NativeCashierInfoRequest();
        NativeCashierInfoRequestBody nativeCashierInfoRequestBody = new NativeCashierInfoRequestBody();
        nativeCashierInfoRequestBody.setMid(getMid(request));
        nativeCashierInfoRequestBody.setReferenceId(request.getBody().getReferenceId());
        if (TheiaConstant.RequestHeaders.Version_V3.equals(request.getBody().getOriginalVersion())) {
            nativeCashierInfoRequestBody.setReturnDisabledChannels(true);
        }
        nativeCashierInfoRequest.setBody(nativeCashierInfoRequestBody);
        nativeCashierInfoRequest.setHead(request.getHead());

        NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                nativeCashierInfoRequest);
        return nativeCashierInfoContainerRequest;
    }

    private void setCookieAfterValidatingOtp(ValidateOtpRequest request,
            ValidateLoginOtpResponse validateLoginOtpResponse) {
        String mid = getMid(request);
        String ssoToken = validateLoginOtpResponse.getResponseData().getAccess_token().get(0).getValue();
        boolean isPgAutoLoginEnabled = pgPreferenceHelper.checkPgAutologinEnabledFlag(mid);
        if (request.getBody().isRememberMe() && isPgAutoLoginEnabled) {
            nativeSessionUtil.setLoginCookie(ssoToken);
        } else {
            LOGGER.info("Skipping cookie setting in response. isRememberMe:{}, isPgAutoLoginEnabled:{}", request
                    .getBody().isRememberMe(), isPgAutoLoginEnabled);
        }
    }

    private String getMid(ValidateOtpRequest request) {
        if (TokenType.TXN_TOKEN == request.getHead().getTokenType()) {
            return nativeSessionUtil.getOrderDetail(request.getHead().getToken()).getMid();
        }

        if (TokenType.GUEST == request.getHead().getTokenType() || TokenType.ACCESS == request.getHead().getTokenType()) {
            return (String) nativeSessionUtil.getField(request.getHead().getToken(),
                    TheiaConstant.RequestParams.Native.MID);
        }
        return null;
    }

    private void validateAccessToken(ValidateOtpRequest request) {
        String mid = (String) nativeSessionUtil.getField(request.getHead().getToken(),
                TheiaConstant.RequestParams.Native.MID);
        String referenceId = request.getBody().getReferenceId();
        String token = request.getHead().getToken();
        nativeValidationService.validateMid(mid);
        accessTokenUtils.validateAccessToken(mid, referenceId, token);

    }

    private void markRequestValidatedForSubscription(ValidateOtpRequest request, ValidateLoginOtpRequest serviceRequest) {
        if (serviceRequest.isNativeSubsRequest()) {
            nativeSessionUtil.setSubsOtpAuthorisedFlag(request.getHead().getTxnToken(), true);
        }
    }

    private void setParamsForNativeSubsRequest(InitiateTransactionRequestBody orderDetail,
            ValidateLoginOtpRequest serviceRequest) {

        if (orderDetail != null && serviceRequest != null
                && ERequestType.NATIVE_SUBSCRIPTION.getType().equals(orderDetail.getRequestType())) {
            serviceRequest.setNativeSubsRequest(true);
        }
    }
}