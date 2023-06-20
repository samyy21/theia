package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.one.click.request.DeEnrollOneClickRequest;
import com.paytm.pgplus.theia.nativ.model.one.click.request.DeEnrollOneClickServiceRequest;
import com.paytm.pgplus.theia.nativ.model.one.click.response.DeEnrollOneClickResponse;
import com.paytm.pgplus.theia.nativ.model.one.click.response.InstaProxyDeEnrollOneClickResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.oneclick.Constants.Constants;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.services.IConsumableInstaProxyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

@Service("deEnrollOneClickRequestProcessor")
public class DeEnrollOneClickRequestProcessor
        extends
        AbstractRequestProcessor<DeEnrollOneClickRequest, DeEnrollOneClickResponse, DeEnrollOneClickServiceRequest, DeEnrollOneClickResponse>
        implements
        IConsumableInstaProxyService<DeEnrollOneClickRequest, DeEnrollOneClickServiceRequest, InstaProxyDeEnrollOneClickResponse> {

    public static final Logger LOGGER = LoggerFactory.getLogger(DeEnrollOneClickRequestProcessor.class);

    @Autowired
    NativeValidationService nativeValidationService;

    @Autowired
    TokenValidationHelper tokenValidationHelper;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Override
    protected DeEnrollOneClickServiceRequest preProcess(DeEnrollOneClickRequest request) {
        DeEnrollOneClickServiceRequest serviceRequest = null;
        if (request.getHead() != null && request.getHead().getTokenType() == null) {
            request.getHead().setTokenType(TokenType.SSO);
        }
        validateRequest(request);
        validateToken(request);
        serviceRequest = getServiceRequest(request);
        return serviceRequest;
    }

    @Override
    protected DeEnrollOneClickResponse onProcess(DeEnrollOneClickRequest request,
            DeEnrollOneClickServiceRequest serviceRequest) throws Exception {

        DeEnrollOneClickResponse finalResponse = null;
        try {
            /** Calling InstaProxy API */
            InstaProxyDeEnrollOneClickResponse instaProxyAPIResponse = callInstaProxyService(request, serviceRequest);
            if (instaProxyAPIResponse != null) {
                validateResponse(instaProxyAPIResponse);
                return new DeEnrollOneClickResponse(instaProxyAPIResponse);
            }
        } catch (Exception e) {
            LOGGER.error("Exception in calling InstaProxy enroll status check service {}",
                    ExceptionUtils.getStackTrace(e));
        }
        return finalResponse;
    }

    @Override
    protected DeEnrollOneClickResponse postProcess(DeEnrollOneClickRequest request,
            DeEnrollOneClickServiceRequest deEnrollOneClickRequest, DeEnrollOneClickResponse response) throws Exception {
        if (null == response) {
            response = new DeEnrollOneClickResponse(ResultCode.FAILED);
        }
        return response;
    }

    @Override
    public InstaProxyDeEnrollOneClickResponse callInstaProxyService(DeEnrollOneClickRequest request,
            DeEnrollOneClickServiceRequest serviceRequest) throws Exception {
        String targetUrl = getUrl(TheiaConstant.TheiaInstaConstants.INSTAPROXY_BASE_URL)
                + getUrl(TheiaConstant.TheiaInstaConstants.INSTAPROXY_ONECLICK_DENROLL_URL);
        HttpRequestPayload<MultivaluedMap<String, String>> payload = createRequest(serviceRequest.getDeEnrollContent(),
                targetUrl);
        LOGGER.info("CardDeEnrollRequestProcessor.callInstaProxyService | serviceRequest log {}", payload.toString());
        Response response = sendRequestToInstaProxy(payload);
        if (response != null) {
            InstaProxyDeEnrollOneClickResponse instaProxyDeEnrollOneClickResponse = convertResponseToInstaProxyResponse(
                    response, InstaProxyDeEnrollOneClickResponse.class);
            LOGGER.info("CardDeEnrollRequestProcessor.callInstaProxyService | instaProxyDeEnrollOneClickResponse :{}",
                    instaProxyDeEnrollOneClickResponse.toString());
            return instaProxyDeEnrollOneClickResponse;
        }
        return null;
    }

    private DeEnrollOneClickServiceRequest getServiceRequest(DeEnrollOneClickRequest request) {
        if (null == request || null == request.getBody()) {
            return null;
        }
        DeEnrollOneClickServiceRequest serviceRequest = new DeEnrollOneClickServiceRequest();
        Map<String, String> requestBodyContent = request.getBody().getDeEnrollContent();
        serviceRequest.setDeEnrollContent(requestBodyContent);
        return serviceRequest;
    }

    private void validateResponse(InstaProxyDeEnrollOneClickResponse directAPIResponse) throws Exception {
        if (directAPIResponse == null) {
            LOGGER.error("Response got from InstaProxy for card De-enrollment is null");
        }
    }

    private void validateRequest(DeEnrollOneClickRequest request) {

        if (null == request || null == request.getHead() || null == request.getBody()
                || null == request.getBody().getDeEnrollContent()) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.INVALID_REQUEST));
        }
        if (StringUtils.isBlank(request.getHead().getToken())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.EMPTY_TOKEN));
        }
        if (request.getHead().getTokenType() == null) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.EMPTY_TOKEN_TYPE));
        }

        if (!StringUtils.isBlank(request.getHead().getRequestId())) {
            MDC.put(TheiaConstant.RequestParams.REQUEST_ID, request.getHead().getRequestId());
        }

        if ((TokenType.ACCESS.name().equals(request.getHead().getTokenType()) || TokenType.CHECKSUM.name().equals(
                request.getHead().getTokenType()))
                && StringUtils.isBlank(request.getBody().getMid())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.INVALID_MID));
        }
    }

    private void validateToken(DeEnrollOneClickRequest request) {
        switch (request.getHead().getTokenType()) {
        case SSO:
            try {
                UserDetailsBiz userDetails = nativeValidationService.validateSSOToken(request.getHead().getToken(),
                        request.getBody().getMid());
                String custIdInRequest = request.getBody().getDeEnrollContent().get("custId");
                if (StringUtils.isNotBlank(custIdInRequest) && !userDetails.getUserId().equals(custIdInRequest)) {
                    LOGGER.error(
                            "CardEnrollmentStatusRequestProcessor.validate | sso token:{} is not of this custID:{}",
                            request.getHead().getToken(), custIdInRequest);
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new RequestValidationException(new ResultInfo(ResultCode.INVALID_SSO_TOKEN.getResultStatus(),
                        ResultCode.INVALID_SSO_TOKEN.getResultCodeId(), ResultCode.INVALID_SSO_TOKEN.getResultCodeId(),
                        ResultCode.INVALID_SSO_TOKEN.getResultMsg()));
            }
            break;
        case CHECKSUM:
            try {
                tokenValidationHelper.validateChecksum(request.getHead().getToken(), request.getBody(), request
                        .getBody().getMid());
            } catch (Exception e) {
                throw new RequestValidationException(new ResultInfo(ResultCode.INVALID_CHECKSUM.getResultStatus(),
                        ResultCode.INVALID_CHECKSUM.getResultCodeId(), ResultCode.INVALID_CHECKSUM.getResultCodeId(),
                        ResultCode.INVALID_CHECKSUM.getResultMsg()));
            }

            break;
        case ACCESS:
            try {
                accessTokenUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(),
                        request.getHead().getToken());
            } catch (Exception e) {
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultStatus(),
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultMsg()));
            }
            break;
        case TXN_TOKEN:
            try {
                nativeSessionUtil.validate(request.getHead().getToken());
            } catch (Exception e) {
                throw new RequestValidationException(new ResultInfo(
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultStatus(),
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultCodeId(),
                        ResultCode.TOKEN_VALIDATION_EXCEPTION.getResultMsg()));
            }
            break;
        default:
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.INVALID_TOKEN_TYPE_EXCEPTION.getResultStatus(),
                    ResultCode.INVALID_TOKEN_TYPE_EXCEPTION.getResultCodeId(),
                    ResultCode.INVALID_TOKEN_TYPE_EXCEPTION.getResultCodeId(),
                    ResultCode.INVALID_TOKEN_TYPE_EXCEPTION.getResultMsg()));
        }
    }

    public HttpRequestPayload<MultivaluedMap<String, String>> createRequest(Map<String, String> content,
            String targetUrlProperty) throws Exception {

        final HttpRequestPayload<MultivaluedMap<String, String>> payload = new HttpRequestPayload<>();
        payload.setHttpMethod(HttpMethod.POST);
        payload.setMediaTypeProduced(MediaType.APPLICATION_FORM_URLENCODED);
        payload.setMediaTypeConsumed(MediaType.TEXT_PLAIN);
        payload.setTarget(targetUrlProperty);

        payload.setEntity(getMultivaluedMapRequest(content));

        return payload;
    }
}
