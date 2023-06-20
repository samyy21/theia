package com.paytm.pgplus.theia.nativ.supergw.processor.impl;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.request.ValidateLoginOtpRequest;
import com.paytm.pgplus.facade.user.models.response.ValidateLoginOtpResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.helper.PGPreferenceHelper;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theiacommon.exception.InvalidRequestParameterException;
import com.paytm.pgplus.theiacommon.exception.RequestValidationException;
import com.paytm.pgplus.theiacommon.response.ResultInfo;
import com.paytm.pgplus.theiacommon.supergw.constants.Constants;
import com.paytm.pgplus.theiacommon.supergw.models.AccessToken;
import com.paytm.pgplus.theiacommon.supergw.models.BasicInfo;
import com.paytm.pgplus.theiacommon.supergw.models.ResponseData;
import com.paytm.pgplus.theiacommon.supergw.request.ValidateOtpRequest;
import com.paytm.pgplus.theiacommon.supergw.request.ValidateOtpServiceRequest;
import com.paytm.pgplus.theiacommon.supergw.response.ValidateOtpResponse;
import com.paytm.pgplus.theiacommon.supergw.response.ValidateOtpResponseBody;
import com.paytm.pgplus.theiacommon.supergw.response.ValidateOtpServiceResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.PaytmPropertyConstants.IS_ORDER_ID_ENABLE_FOR_OTPS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REFERENCE_ID;

@Service("superGwValidateOtpRequestProcessor")
public class SuperGwValidateOtpRequestProcessor
        extends
        AbstractRequestProcessor<ValidateOtpRequest, ValidateOtpResponse, ValidateOtpServiceRequest, ValidateOtpServiceResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwValidateOtpRequestProcessor.class);

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    /*
     * @Autowired
     * 
     * @Qualifier("sgwValidateOtpRequestProcessor") private
     * SgwValidateOtpRequestProcessor sgwValidateOtpRequestProcessor;
     */

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authService;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    private Environment environment;

    @Autowired
    private PGPreferenceHelper pgPreferenceHelper;

    private static final String SUCCESS = "SUCCESS";

    @Override
    protected ValidateOtpServiceRequest preProcess(ValidateOtpRequest request) {
        validateRequest(request);
        ValidateOtpServiceRequest serviceRequest = getServiceRequest(request);
        return serviceRequest;
    }

    @Override
    protected ValidateOtpServiceResponse onProcess(ValidateOtpRequest request, ValidateOtpServiceRequest serviceRequest)
            throws Exception {
        long startTime = System.currentTimeMillis();

        // code to be moved in common jar - start
        validateRequest(serviceRequest);
        ValidateOtpServiceResponse serviceResponse = null;
        try {
            ValidateLoginOtpRequest validateLoginOtpRequest = getValidateLoginOtpRequest(serviceRequest);
            ValidateLoginOtpResponse validateLoginOtpResponse = authService.validateLoginOtp(validateLoginOtpRequest);
            serviceResponse = getValidateOtpServiceResponse(validateLoginOtpResponse);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occurred while validating login OTP {}", e);
            serviceResponse = new ValidateOtpServiceResponse();
            serviceResponse.setMessage("Something went wrong while validating OTP");
            serviceResponse.setStatus("SC_INTERNAL_SERVER_ERROR");
            return serviceResponse;
        }
        // code to be moved in common jar - end

        if (serviceResponse.getResponseData() != null
                && !CollectionUtils.isEmpty(serviceResponse.getResponseData().getAccessTokens())) {
            updateSsoTokenInCache(request, serviceResponse);
            setCookie(request, serviceResponse);
        }
        LOGGER.info("Total time taken for v4/ValidateOtpRequest auth call is {} ms", System.currentTimeMillis()
                - startTime);
        return serviceResponse;
    }

    @Override
    protected ValidateOtpResponse postProcess(ValidateOtpRequest request, ValidateOtpServiceRequest serviceRequest,
            ValidateOtpServiceResponse serviceResponse) {
        if (serviceResponse.getResponseData() != null
                && !CollectionUtils.isEmpty(serviceResponse.getResponseData().getAccessTokens())) {
            updateSsoTokenInCache(request, serviceResponse);
        }
        ValidateOtpResponse response = getValidateOtpResponse(request, serviceResponse);
        return response;
    }

    private void updateSsoTokenInCache(ValidateOtpRequest request, ValidateOtpServiceResponse validateLoginOtpResponse) {
        String ssoToken = validateLoginOtpResponse.getResponseData().getAccessTokens().get(0).getValue();
        String referenceId = MDC.get(REFERENCE_ID);
        String mid = request.getBody().getMid();
        String redisKey = nativeSessionUtil.getCacheKeyForSuperGw(mid, referenceId, ERequestType.NATIVE.getType());
        nativeSessionUtil.setSsoToken(redisKey, ssoToken);
    }

    private void validateRequest(ValidateOtpRequest request) {
        validateMandatoryParams(request);
        nativeValidationService.validateMid(request.getBody().getMid());
        validateJwt(request);
    }

    private void validateMandatoryParams(ValidateOtpRequest request) {
        if (StringUtils.isBlank(request.getBody().getMid()) || StringUtils.isBlank(request.getBody().getOtp())) {
            com.paytm.pgplus.common.model.ResultInfo resultInfo = new com.paytm.pgplus.common.model.ResultInfo();
            ResultCode resultCode = ResultCode.MISSING_MANDATORY_ELEMENT;
            resultInfo.setResultCode(resultCode.getResultCodeId());
            resultInfo.setResultMsg(resultCode.getResultMsg());
            resultInfo.setResultStatus(ResultCode.FAILED.getResultStatus());
            throw new RequestValidationException(resultInfo);
        }
    }

    private ValidateOtpServiceRequest getServiceRequest(ValidateOtpRequest request) {
        String clientId = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_ID);
        String secretKey = configurationDataService
                .getPaytmPropertyValue(TheiaConstant.ExtraConstants.OAUTH_PG_PLUS_EXPRESS_CLIENT_SECRET_KEY);
        String state = getStateFromCache(request);
        boolean isOrderIdEnableForOTPs = Boolean.parseBoolean(ConfigurationUtil.getTheiaProperty(
                IS_ORDER_ID_ENABLE_FOR_OTPS, "false"));
        String uniqueId = isOrderIdEnableForOTPs ? request.getHead().getRequestId() : null;
        ValidateOtpServiceRequest serviceRequest = new ValidateOtpServiceRequest(request.getBody().getOtp(), state,
                clientId, secretKey, uniqueId);
        return serviceRequest;
    }

    private String getStateFromCache(ValidateOtpRequest request) {
        String referenceId = MDC.get(REFERENCE_ID);
        String mid = request.getBody().getMid();
        String redisKey = nativeSessionUtil.getCacheKeyForSuperGw(mid, referenceId, ERequestType.NATIVE.getType());
        String state = (String) nativeSessionUtil.getField(redisKey, "otpState");
        if (StringUtils.isBlank(state)) {
            throw new SessionExpiredException("Token has expired");
        }
        return state;
    }

    private ValidateOtpResponse getValidateOtpResponse(ValidateOtpRequest request,
            ValidateOtpServiceResponse serviceResponse) {
        ResponseHeader responseHeader = new ResponseHeader(TheiaConstant.RequestHeaders.SUPERGW_VERSION);

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setResultMsg(serviceResponse.getMessage());
        resultInfo.setResultCode(serviceResponse.getResponseCode());

        ValidateOtpResponseBody validateOtpResponseBody = new ValidateOtpResponseBody();
        if (SUCCESS.equals(serviceResponse.getStatus())) {
            validateOtpResponseBody.setAuthenticated(true);
            resultInfo.setResultStatus(Constants.Alphabets.S);
        } else {
            validateOtpResponseBody.setAuthenticated(false);
            resultInfo.setResultStatus(Constants.Alphabets.F);
        }
        if (serviceResponse.getResponseData() != null
                && !CollectionUtils.isEmpty(serviceResponse.getResponseData().getAccessTokens())) {
            String ssoToken = serviceResponse.getResponseData().getAccessTokens().get(0).getValue();
            validateOtpResponseBody.setSsoToken(ssoToken);
        }
        validateOtpResponseBody.setResultInfo(resultInfo);

        ValidateOtpResponse response = new ValidateOtpResponse();
        response.setBody(validateOtpResponseBody);
        response.setHead(responseHeader);

        return response;
    }

    // removed because the api in being called internally via super gateway
    private void setCookie(ValidateOtpRequest request, ValidateOtpServiceResponse serviceResponse) {
        String mid = request.getBody().getMid();
        String ssoToken = serviceResponse.getResponseData().getAccessTokens().get(0).getValue();
        boolean isPgAutoLoginEnabled = pgPreferenceHelper.checkPgAutologinEnabledFlag(mid);
        if (request.getBody().isRememberMe() && isPgAutoLoginEnabled) {
            nativeSessionUtil.setLoginCookie(ssoToken);
        } else {
            LOGGER.info("Skipping cookie setting in response. isRememberMe:{}, isPgAutoLoginEnabled:{}", request
                    .getBody().isRememberMe(), isPgAutoLoginEnabled);
        }
    }

    private void validateJwt(ValidateOtpRequest request) {
        String clientId = request.getHead().getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new InvalidRequestParameterException("clientId can't be null");
        }
        String clientSecret = environment.getProperty(clientId);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(FacadeConstants.MID, request.getBody().getMid());
        jwtClaims.put(TheiaConstant.RequestParams.OTP, request.getBody().getOtp());
        SuperGwValidationUtil.validateJwtToken(jwtClaims, clientId, clientSecret);
    }

    private void validateRequest(ValidateOtpServiceRequest inRequest) throws InvalidRequestParameterException {
        if (StringUtils.isBlank(inRequest.getOtp())) {
            throw new InvalidRequestParameterException("OTP can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getClientId())) {
            throw new InvalidRequestParameterException("ClientId can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getSecretKey())) {
            throw new InvalidRequestParameterException("SecretKey can't be null or blank");
        }
        if (StringUtils.isBlank(inRequest.getState())) {
            throw new InvalidRequestParameterException("State can't be null or blank");
        }
    }

    private ValidateLoginOtpRequest getValidateLoginOtpRequest(ValidateOtpServiceRequest serviceRequest) {
        String otp = serviceRequest.getOtp();
        String state = serviceRequest.getState();
        String clientId = serviceRequest.getClientId();
        String secretKey = serviceRequest.getSecretKey();
        return new ValidateLoginOtpRequest(otp, state, clientId, secretKey);
    }

    private ValidateOtpServiceResponse getValidateOtpServiceResponse(ValidateLoginOtpResponse validateLoginOtpResponse) {
        ValidateOtpServiceResponse serviceResponse = new ValidateOtpServiceResponse();
        ResponseData responseData = new ResponseData();
        BasicInfo basicInfo = new BasicInfo();
        if (validateLoginOtpResponse != null) {
            serviceResponse.setMessage(validateLoginOtpResponse.getMessage());
            serviceResponse.setStatus(validateLoginOtpResponse.getStatus());
            serviceResponse.setResponseCode(validateLoginOtpResponse.getResponseCode());

            com.paytm.pgplus.facade.user.models.response.ResponseData serviceResponseData = validateLoginOtpResponse
                    .getResponseData();
            if (serviceResponseData != null) {
                responseData.setUserId(serviceResponseData.getUserId());
                List<AccessToken> accessTokens = new ArrayList<>();
                if (!CollectionUtils.isEmpty(serviceResponseData.getAccess_token())) {
                    for (com.paytm.pgplus.facade.user.models.response.AccessToken serviceRespAccessToken : serviceResponseData
                            .getAccess_token()) {
                        accessTokens.add(new AccessToken(serviceRespAccessToken.getValue(), serviceRespAccessToken
                                .getExpires(), serviceRespAccessToken.getScope()));
                    }
                }
                responseData.setAccessTokens(accessTokens);
                com.paytm.pgplus.facade.user.models.BasicInfo serviceRespBasicInfo = serviceResponseData.getBasicInfo();
                if (serviceRespBasicInfo != null) {
                    basicInfo.setCountryCode(serviceRespBasicInfo.getCountryCode());
                    basicInfo.setEmail(serviceRespBasicInfo.getEmail());
                    basicInfo.setLastName(serviceRespBasicInfo.getLastName());
                    basicInfo.setFirstName(serviceRespBasicInfo.getFirstName());
                    basicInfo.setPhone(serviceRespBasicInfo.getPhone());
                }
            }
        }
        responseData.setBasicInfo(basicInfo);
        serviceResponse.setResponseData(responseData);
        return serviceResponse;
    }

}