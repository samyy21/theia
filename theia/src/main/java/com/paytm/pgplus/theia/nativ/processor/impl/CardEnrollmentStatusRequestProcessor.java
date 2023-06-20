package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.one.click.request.CheckEnrollStatusRequest;
import com.paytm.pgplus.theia.nativ.model.one.click.request.CheckEnrollStatusServiceRequest;
import com.paytm.pgplus.theia.nativ.model.one.click.request.EnrolledCardData;
import com.paytm.pgplus.theia.nativ.model.one.click.response.CheckEnrollStatusResponse;
import com.paytm.pgplus.common.model.InstaProxyEnrollStatusResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.oneclick.Constants.Constants;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.services.IConsumableInstaProxyService;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.paytm.pgplus.facade.constants.FacadeConstants.ConsentAPIConstant.CONTENT_TYPE;

@Service("cardEnrollmentStatusRequestProcessor")
public class CardEnrollmentStatusRequestProcessor
        extends
        AbstractRequestProcessor<CheckEnrollStatusRequest, CheckEnrollStatusResponse, CheckEnrollStatusServiceRequest, CheckEnrollStatusResponse>
        implements
        IConsumableInstaProxyService<CheckEnrollStatusRequest, CheckEnrollStatusServiceRequest, InstaProxyEnrollStatusResponse> {

    public static final Logger LOGGER = LoggerFactory.getLogger(CardEnrollmentStatusRequestProcessor.class);

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    TokenValidationHelper tokenValidationHelper;

    @Autowired
    NativeValidationService nativeValidationService;

    @Autowired
    NativeSessionUtil nativeSessionUtil;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Override
    protected CheckEnrollStatusServiceRequest preProcess(CheckEnrollStatusRequest request) {
        CheckEnrollStatusServiceRequest serviceRequest = null;
        validateRequest(request);
        validateToken(request);
        serviceRequest = getServiceRequest(request);
        return serviceRequest;
    }

    @Override
    protected CheckEnrollStatusResponse onProcess(CheckEnrollStatusRequest request,
            CheckEnrollStatusServiceRequest serviceRequest) throws Exception {
        CheckEnrollStatusResponse finalResponse = null;
        try {
            /** Calling InstaProxy API */
            InstaProxyEnrollStatusResponse instaProxyAPIResponse = callInstaProxyService(request, serviceRequest);
            if (instaProxyAPIResponse != null) {
                validateResponse(instaProxyAPIResponse);
                return new CheckEnrollStatusResponse(instaProxyAPIResponse);
            }
        } catch (Exception e) {
            LOGGER.error("Exception in calling InstaProxy enroll status check service {}",
                    ExceptionUtils.getStackTrace(e));
        }
        return finalResponse;
    }

    @Override
    protected CheckEnrollStatusResponse postProcess(CheckEnrollStatusRequest request,
            CheckEnrollStatusServiceRequest checkEnrollStatusServiceRequest,
            CheckEnrollStatusResponse checkEnrollStatusResponse) throws Exception {
        if (null == checkEnrollStatusResponse) {
            checkEnrollStatusResponse = new CheckEnrollStatusResponse(ResultCode.UNKNOWN_ERROR);
        }
        return checkEnrollStatusResponse;
    }

    @Override
    public InstaProxyEnrollStatusResponse callInstaProxyService(CheckEnrollStatusRequest checkEnrollStatusRequest,
            CheckEnrollStatusServiceRequest checkEnrollStatusServiceRequest) throws Exception {
        String targetUrl = getUrl(TheiaConstant.TheiaInstaConstants.INSTAPROXY_BASE_URL)
                + getUrl(TheiaConstant.TheiaInstaConstants.INSTAPROXY_ENROLL_STATUS_CHECK_URL);
        HttpRequestPayload<String> payload = createEnrollStatusCheckRequest(checkEnrollStatusServiceRequest, targetUrl);
        LOGGER.info("CardEnrollmentRequestProcessor.callInstaProxyService | serviceRequest log {}", payload.toString());
        Response response = initiatePostServiceCall(payload);
        if (response != null) {
            InstaProxyEnrollStatusResponse instaProxyEnrollStatusResponse = convertResponseToInstaProxyResponse(
                    response, InstaProxyEnrollStatusResponse.class);
            LOGGER.info("CardEnrollmentRequestProcessor.callInstaProxyService | instaProxyEnrollStatusResponse :{}",
                    instaProxyEnrollStatusResponse.toString());
            return instaProxyEnrollStatusResponse;
        }
        return null;
    }

    private CheckEnrollStatusServiceRequest getServiceRequest(CheckEnrollStatusRequest request) {
        CheckEnrollStatusServiceRequest serviceRequest = new CheckEnrollStatusServiceRequest();
        serviceRequest.setAppId(request.getBody().getAppId());
        serviceRequest.setCustId(request.getBody().getCustId());
        serviceRequest.setEnrolledCardDataList(request.getBody().getEnrolledCardDataList());
        return serviceRequest;
    }

    private void validateResponse(InstaProxyEnrollStatusResponse response) throws Exception {
        if (response == null) {
            LOGGER.error("CardEnrollmentRequestProcessor.validateResponse Response got from InstaProxy for enrollment status check is null");
        }
    }

    private HttpRequestPayload<String> createEnrollStatusCheckRequest(CheckEnrollStatusServiceRequest serviceRequest,
            String targetUrl) throws Exception {
        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        payload.setHttpMethod(HttpMethod.POST);
        payload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        payload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);
        payload.setTarget(targetUrl);
        final String body = JsonMapper.mapObjectToJson(serviceRequest);
        payload.setEntity(body);
        headerMap.add(CONTENT_TYPE, "application/json");
        payload.setHeaders(headerMap);
        return payload;
    }

    private void validateRequest(CheckEnrollStatusRequest request) {

        if (null == request || null == request.getHead() || null == request.getBody()) {
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

        if ((TokenType.ACCESS.name().equals(request.getHead().getTokenType()) || TokenType.CHECKSUM.name().equals(
                request.getHead().getTokenType()))
                && StringUtils.isBlank(request.getBody().getMid())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.INVALID_MID));
        }
        if (TokenType.ACCESS.name().equals(request.getHead().getTokenType())
                && StringUtils.isBlank(request.getBody().getReferenceId())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.INVALID_REFERENCE_ID));
        }
        if (StringUtils.isBlank(request.getHead().getRequestId())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.EMPTY_REQUEST_ID));
        }
        MDC.put(TheiaConstant.RequestParams.REQUEST_ID, request.getHead().getRequestId());
        if (StringUtils.isBlank(request.getBody().getAppId())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.EMPTY_APP_ID));
        }
        if (StringUtils.isBlank(request.getBody().getCustId())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), Constants.EMPTY_CUST_ID));
        }
        List<EnrolledCardData> enrolledCardDataList = request.getBody().getEnrolledCardDataList();
        if (null == enrolledCardDataList) {
            throw new RequestValidationException(
                    new ResultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                            ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                            Constants.EMPTY_ACCOUNT_DATA_LIST));
        }
    }

    private void validateToken(CheckEnrollStatusRequest request) {
        switch (request.getHead().getTokenType()) {
        case SSO:
            try {
                UserDetailsBiz userDetails = nativeValidationService.validateSSOToken(request.getHead().getToken(),
                        request.getBody().getMid());
                if (!userDetails.getUserId().equals(request.getBody().getCustId())) {
                    LOGGER.error(
                            "CardEnrollmentStatusRequestProcessor.validate | sso token:{} is not of this custID:{}",
                            request.getHead().getToken(), request.getBody().getCustId());
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
}
