package com.paytm.pgplus.theia.accesstoken.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.enums.EPreAuthType;
import com.paytm.pgplus.enums.TokenType;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import com.paytm.pgplus.theia.accesstoken.exception.BaseException;
import com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException;
import com.paytm.pgplus.theia.accesstoken.helper.AccessTokenValidationHelper;
import com.paytm.pgplus.theia.accesstoken.model.AccessTokenBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenRequest;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.accesstoken.model.response.CreateAccessTokenResponse;
import com.paytm.pgplus.theia.accesstoken.model.response.CreateAccessTokenResponseBody;
import com.paytm.pgplus.theia.accesstoken.model.response.CreateAccessTokenServiceResponse;
import com.paytm.pgplus.theia.accesstoken.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativePersistData;
import com.paytm.pgplus.theia.offline.exceptions.PaymentRequestProcessingException;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service("accessTokenRequestProcessor")
public class AccessTokenRequestProcessor
        extends
        AbstractRequestProcessor<CreateAccessTokenRequest, CreateAccessTokenResponse, CreateAccessTokenServiceRequest, CreateAccessTokenServiceResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenRequestProcessor.class);

    @Autowired
    private AccessTokenValidationHelper accessTokenValidationHelper;

    @Autowired
    private INativeValidationService nativeValidationService;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    FF4JUtil ff4JUtil;

    @Override
    protected CreateAccessTokenServiceRequest preProcess(CreateAccessTokenRequest request) {

        if (StringUtils.isNotBlank(request.getBody().getCardPreAuthType())
                && !EnumUtils.isValidEnum(EPreAuthType.class, request.getBody().getCardPreAuthType())) {
            throw RequestValidationException.getException(ResultCode.INVALID_CARD_PREAUTH_TYPE);
        }
        if (StringUtils.isNotBlank(request.getBody().getPreAuthBlockSeconds())
                && !StringUtils.isNumeric(request.getBody().getPreAuthBlockSeconds())) {
            throw RequestValidationException.getException(ResultCode.INVALID_PREAUTH_BLOCK_SECONDS);
        }
        CreateAccessTokenServiceRequest serviceRequest = getServiceRequest(request);
        validateRequest(request, serviceRequest);
        return serviceRequest;
    }

    @Override
    protected CreateAccessTokenServiceResponse onProcess(CreateAccessTokenRequest request,
            CreateAccessTokenServiceRequest serviceRequest) throws Exception {

        // Get Access token
        AccessTokenBody accessTokenBody = accessTokenUtils.createAccessToken(serviceRequest);

        // Validate Idempotent request
        if (accessTokenBody.isIdempotent() && !accessTokenUtils.isValidAccessToken(serviceRequest, accessTokenBody)) {
            throw RequestValidationException.getException(ResultCode.INCONSISTENT_REPEAT_REQUEST);
        }

        // Send Response(Access token)
        CreateAccessTokenServiceResponse response = new CreateAccessTokenServiceResponse();
        response.setToken(accessTokenBody.getToken());
        response.setIdempotent(accessTokenBody.isIdempotent());
        return response;
    }

    @Override
    protected CreateAccessTokenResponse postProcess(CreateAccessTokenRequest request,
            CreateAccessTokenServiceRequest serviceRequest, CreateAccessTokenServiceResponse serviceResponse)
            throws Exception {

        CreateAccessTokenResponse response = new CreateAccessTokenResponse();
        response.setHead(AccessTokenUtils.createResponseHeader());
        response.setBody(getResponseBody(serviceResponse));

        return response;
    }

    private CreateAccessTokenResponseBody getResponseBody(CreateAccessTokenServiceResponse serviceResponse) {

        if (null == serviceResponse) {
            throw new BaseException();
        }
        CreateAccessTokenResponseBody responseBody = new CreateAccessTokenResponseBody();
        responseBody.setAccessToken(serviceResponse.getToken());
        return responseBody;
    }

    private CreateAccessTokenServiceRequest getServiceRequest(CreateAccessTokenRequest request) {

        CreateAccessTokenServiceRequest serviceRequest = new CreateAccessTokenServiceRequest();
        serviceRequest.setMid(request.getBody().getMid());
        serviceRequest.setReferenceId(request.getBody().getReferenceId());
        serviceRequest.setPaytmSsoToken(request.getBody().getPaytmSsoToken());
        serviceRequest.setCustId(request.getBody().getCustId());
        serviceRequest.setUserInfo(request.getBody().getUserInfo());
        serviceRequest.setProductType(request.getBody().getProductType());
        if (StringUtils.isNotBlank(request.getBody().getCardPreAuthType()))
            serviceRequest.setPreAuthType(EPreAuthType.valueOf(request.getBody().getCardPreAuthType()));
        if (StringUtils.isNotBlank(request.getBody().getPreAuthBlockSeconds()))
            serviceRequest.setPreAuthBlockSeconds(Long.valueOf(request.getBody().getPreAuthBlockSeconds()));
        return serviceRequest;
    }

    private void validateRequest(CreateAccessTokenRequest request, CreateAccessTokenServiceRequest serviceRequest) {

        int isValidRequest = 0; // Zero for valid case & nonZero for various
                                // invalid case which are handled later.
        StringBuilder invalidRequestCause = new StringBuilder("Invalid request: ");

        String referenceIdLengthValidationMessage = " ReferenceId should fulfill: Minimum length of 10 and maximum length of 20.";

        if (null == request || null == request.getHead() || null == request.getBody()) {
            isValidRequest = 1;
            invalidRequestCause.append("Request is empty. ");
        } else {
            String mid = request.getBody().getMid();
            if (StringUtils.isBlank(mid)) {
                isValidRequest = 1;
                invalidRequestCause.append("Mid is blank. ");
            }
            String referenceId = request.getBody().getReferenceId();
            if (StringUtils.isBlank(referenceId)) {
                isValidRequest = 1;
                invalidRequestCause.append("ReferenceId is blank.");
            } else if (referenceId.length() < 10 || referenceId.length() > 20) {
                isValidRequest = 2;
                invalidRequestCause.append(referenceIdLengthValidationMessage);
            }
            String paytmSsoToken = request.getBody().getPaytmSsoToken();
            if (StringUtils.isNotBlank(paytmSsoToken)) {
                try {
                    UserDetailsBiz userDetailsBiz = nativeValidationService.validateSSOToken(paytmSsoToken, mid);
                    serviceRequest.setNativePersistData(new NativePersistData(userDetailsBiz));
                } catch (PaymentRequestProcessingException e) {
                    boolean fkMid = ff4JUtil.isFeatureEnabled(
                            TheiaConstant.ExtraConstants.ACCESS_TOKEN_INVALID_SSO_RESPONSE_CODE, mid);
                    ResultCode rc = ResultCode.INVALID_SSO_TOKEN;
                    if (fkMid) {
                        rc = ResultCode.INVALID_FK_SSO_TOKEN;
                    }
                    throw RequestValidationException.getException(rc);
                }

            }

        }
        if (isValidRequest != 0) {
            LOGGER.info(invalidRequestCause.toString());
        }
        if (isValidRequest == 1) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        } else if (isValidRequest == 2) {
            throw RequestValidationException.getExceptionWithAffixedMessage(ResultCode.REQUEST_PARAMS_VALIDATION,
                    referenceIdLengthValidationMessage);
        }

        // validating Checksum
        validateToken(request);
    }

    private void validateToken(CreateAccessTokenRequest request) {
        if (TokenType.CHECKSUM.equals(request.getHead().getTokenType())) {
            HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            String requestBody = "";
            try {
                requestBody = IOUtils.toString(httpServletRequest.getInputStream(), Charsets.UTF_8.name());
            } catch (IOException e) {
                LOGGER.error("Exception in validating checksum for createToken {}", e);
            }
            accessTokenValidationHelper.validateChecksum(request.getHead().getToken(), request.getBody(), requestBody,
                    request.getBody().getMid());
        } else {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE);
        }
    }
}
