/**
 * Alipay.com Inc. * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.user.models.UserPreferenceInfo;
import com.paytm.pgplus.facade.user.models.response.PutUserPreferenceResponse;
import com.paytm.pgplus.facade.user.services.IUserPreferenceService;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.theia.nativ.enums.UpsTheiaResponseMapping;
import com.paytm.pgplus.theia.nativ.model.user.SetUserPreferenceRequest;
import com.paytm.pgplus.theia.nativ.model.user.SetUserPreferenceResponse;
import com.paytm.pgplus.theia.nativ.model.user.SetUserPreferenceResponseBody;
import com.paytm.pgplus.theia.nativ.model.user.SetUserPreferenceServiceRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.*;
import static com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.UPSConstants.ResponseConstants.*;

@Service("setUserPreferenceRequestProcessor")
public class SetUserPreferenceRequestProcessor
        extends
        AbstractRequestProcessor<SetUserPreferenceRequest, SetUserPreferenceResponse, SetUserPreferenceServiceRequest, PutUserPreferenceResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetUserPreferenceRequestProcessor.class);

    @Autowired
    private IUserPreferenceService userPreferenceService;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Autowired
    Environment env;

    private final Map<String, Object> allowedUserPrefMap = JsonMapper.getMapFromJson(ConfigurationUtil
            .getProperty(ALLOWED_UPS_USERPREFERENCE_KEYVALUES));

    public SetUserPreferenceRequestProcessor() throws FacadeCheckedException {
    }

    @Override
    protected SetUserPreferenceServiceRequest preProcess(SetUserPreferenceRequest request) throws Exception {

        validateRequest(request);

        if (TokenType.SSO.equals(request.getHead().getTokenType())) {
            return preProcessWithSSOToken(request);
        }
        SetUserPreferenceServiceRequest serviceRequest = generateServiceRequestData(request);
        enrich(request, serviceRequest);
        return serviceRequest;
    }

    @Override
    protected PutUserPreferenceResponse onProcess(SetUserPreferenceRequest request,
            SetUserPreferenceServiceRequest serviceRequest) throws Exception {
        PutUserPreferenceResponse serviceResponse;
        try {
            serviceResponse = userPreferenceService.putUserPreference(serviceRequest.getUserId(), request.getBody()
                    .getUserPreferenceDetails(), ConfigurationUtil.getProperty(UPS_JWT_CLIENT_ID), env
                    .getProperty(UPS_JWT_SECRET_KEY));
        } catch (Exception e) {
            LOGGER.error("Exception occurred while setting User Preference {}", e);
            serviceResponse = null;
        }
        return serviceResponse;
    }

    @Override
    protected SetUserPreferenceResponse postProcess(SetUserPreferenceRequest request,
            SetUserPreferenceServiceRequest serviceRequest, PutUserPreferenceResponse serviceResponse) {
        return responseTransformer(serviceRequest, serviceResponse);
    }

    private void validateRequest(SetUserPreferenceRequest request) throws Exception {
        validateMandatoryParams(request);
        if (StringUtils.equalsIgnoreCase(request.getHead().getTokenType().getType(), TokenType.TXN_TOKEN.getType()))
            nativeSessionUtil.validate(request.getHead().getToken());

        validateUserPreferencesDetails(request);
    }

    private void validateUserPreferencesDetails(SetUserPreferenceRequest request) throws Exception {

        if (allowedUserPrefMap != null && ObjectUtils.notEqual(request.getBody(), null)
                && CollectionUtils.isNotEmpty(request.getBody().getUserPreferenceDetails())) {

            for (UserPreferenceInfo userPref : request.getBody().getUserPreferenceDetails()) {
                if (StringUtils.isNotBlank(userPref.getPreferenceKey())
                        && StringUtils.isNotBlank(userPref.getPreferenceValue())
                        && allowedUserPrefMap.containsKey(userPref.getPreferenceKey())
                        && ObjectUtils.notEqual(allowedUserPrefMap.get(userPref.getPreferenceKey()), null)
                        && isPrefValueAllowed(allowedUserPrefMap.get(userPref.getPreferenceKey()),
                                userPref.getPreferenceValue())) {
                } else {
                    LOGGER.error("preferenceKey or preferenceValue is Invaild");
                    throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
                }
            }
        } else {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private boolean isPrefValueAllowed(Object o, String preferenceValue) {
        List<String> allowedValues = (List<String>) o;
        return allowedValues.contains(preferenceValue);
    }

    private void validateMandatoryParams(SetUserPreferenceRequest request) {
        if (ObjectUtils.equals(request, null) || ObjectUtils.equals(request.getHead(), null)
                || StringUtils.isBlank(request.getHead().getToken())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        if (ObjectUtils.equals(request.getHead().getTokenType(), null)
                || !(StringUtils.equalsIgnoreCase(request.getHead().getTokenType().getType(),
                        TokenType.TXN_TOKEN.getType()) || StringUtils.equalsIgnoreCase(request.getHead().getTokenType()
                        .getType(), TokenType.SSO.getType()))) {
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE_EXCEPTION);
        }
    }

    private SetUserPreferenceServiceRequest generateServiceRequestData(SetUserPreferenceRequest request) {
        SetUserPreferenceServiceRequest serviceRequest = new SetUserPreferenceServiceRequest();
        UserDetailsBiz userDetails = nativeSessionUtil.getUserDetails(request.getHead().getToken());

        if (ObjectUtils.notEqual(userDetails, null) && StringUtils.isNotEmpty(userDetails.getUserId())) {
            serviceRequest.setUserId(userDetails.getUserId());
        } else {
            LOGGER.error("User details not found in Redis");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        return serviceRequest;
    }

    private SetUserPreferenceServiceRequest preProcessWithSSOToken(SetUserPreferenceRequest request) {
        String ssoToken = request.getHead().getToken();
        String mid = request.getBody().getMid();
        UserDetailsBiz userDetails = null;
        SetUserPreferenceServiceRequest serviceRequest = new SetUserPreferenceServiceRequest();

        userDetails = nativeValidationService.validateSSOToken(ssoToken, mid);

        if (ObjectUtils.notEqual(userDetails, null) && StringUtils.isNotEmpty(userDetails.getUserId())) {
            serviceRequest.setUserId(userDetails.getUserId());
        } else {
            LOGGER.error("Invalid SSO Token");
            throw RequestValidationException.getException(ResultCode.INVALID_SSO_TOKEN);
        }
        enrich(request, serviceRequest);
        return serviceRequest;
    }

    private void enrich(SetUserPreferenceRequest request, SetUserPreferenceServiceRequest serviceRequest) {
        serviceRequest.setUserPreferences(request.getBody().getUserPreferenceDetails());
        serviceRequest.setMid(request.getBody().getMid());
        serviceRequest.setOrderId(request.getBody().getOrderId());
    }

    private SetUserPreferenceResponse responseTransformer(SetUserPreferenceServiceRequest serviceRequest,
            PutUserPreferenceResponse serviceResponse) {

        if (ObjectUtils.equals(serviceResponse, null)) {
            return generateErrorResponse();
        }
        SetUserPreferenceResponse response = new SetUserPreferenceResponse(new ResponseHeader(),
                new SetUserPreferenceResponseBody());
        ResultInfo responseMapping = UpsTheiaResponseMapping.fetchResultInfoByCode(serviceResponse.getStatusInfo()
                .getStatusCode());
        response.getBody().setResultInfo(responseMapping);

        if (serviceResponse.getStatusInfo().getStatus().equalsIgnoreCase(SUCCESS)) {
            response.getBody().setMessage(SET_USER_PREFERENCE_SUCCESS_MESSAGE);
        } else {
            response.getBody().setMessage(SET_USER_PREFERENCE_FAILURE_MESSAGE);
        }
        return response;
    }

    private SetUserPreferenceResponse generateErrorResponse() {
        SetUserPreferenceResponse response = new SetUserPreferenceResponse(new ResponseHeader(),
                new SetUserPreferenceResponseBody());
        response.getBody().setMessage(SET_USER_PREFERENCE_FAILURE_MESSAGE);
        response.getBody().setResultInfo(new ResultInfo("00000900", "F", "SYSTEM_ERROR"));
        return response;
    }

}