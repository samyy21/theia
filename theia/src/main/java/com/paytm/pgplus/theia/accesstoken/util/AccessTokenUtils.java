package com.paytm.pgplus.theia.accesstoken.util;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.model.nativ.ResponseHeader;
import com.paytm.pgplus.common.util.ThreadLocalUtil;
import com.paytm.pgplus.request.TokenRequestHeader;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import com.paytm.pgplus.theia.accesstoken.exception.BaseException;
import com.paytm.pgplus.theia.accesstoken.model.AccessTokenBody;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenServiceRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.redis.ITheiaSessionRedisUtil;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.DEFAULT_TXN_TOKEN_EXPIRY_FOR_ACCESS_TOKEN_IN_SECONDS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.TRUE;

@Service
public class AccessTokenUtils {

    @Autowired
    @Qualifier("theiaSessionRedisUtil")
    ITheiaSessionRedisUtil theiaSessionRedisUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenUtils.class);
    private static final String REQUEST_HEADER_KEY = "REQUEST_HEADER_ACCESS_TOKEN";

    public static ResultInfo resultInfo(ResultCode resultCode) {
        if (resultCode == null)
            resultCode = ResultCode.UNKNOWN_ERROR;
        return new ResultInfo(resultCode.getStatus(), resultCode.getId(), resultCode.getCode(), resultCode.getMessage());
    }

    public static HttpServletRequest gethttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static void setRequestHeader(TokenRequestHeader requestHeader) {
        gethttpServletRequest().setAttribute(REQUEST_HEADER_KEY, requestHeader);
    }

    public static TokenRequestHeader getRequestHeader() {
        return (TokenRequestHeader) gethttpServletRequest().getAttribute(REQUEST_HEADER_KEY);
    }

    public static ResponseHeader createResponseHeader() {
        ResponseHeader responseHeader = new ResponseHeader();
        TokenRequestHeader requestHeader = getRequestHeader();
        if (requestHeader != null) {
            if (StringUtils.isNotBlank(requestHeader.getVersion())) {
                responseHeader.setVersion(requestHeader.getVersion());
            }
            responseHeader.setRequestId(requestHeader.getRequestId());
        }
        return responseHeader;
    }

    /**
     * Excerpt from NativeSessionUtil
     */

    public static int getTokenExpiryTime() {
        int tokenExpiryInSeconds = 1800;

        String tokenExpiryStringInSeconds = ConfigurationUtil.getProperty("access.token.expiry.seconds",
                DEFAULT_TXN_TOKEN_EXPIRY_FOR_ACCESS_TOKEN_IN_SECONDS);

        if (StringUtils.isNotBlank(tokenExpiryStringInSeconds)) {
            tokenExpiryInSeconds = Integer.parseInt(tokenExpiryStringInSeconds);
        }
        return tokenExpiryInSeconds;
    }

    private Object fetchField(String key, String field) {
        return theiaSessionRedisUtil.hget(key, field);
    }

    // For Access token creation using Mid & RefId
    public AccessTokenBody createAccessToken(CreateAccessTokenServiceRequest request) {
        int txnTokenExpiryInSeconds = getTokenExpiryTime();
        AccessTokenBody accessTokenBody = new AccessTokenBody();
        String accessToken = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();

        if (theiaSessionRedisUtil.setnx(getMidReferenceIdKeyForRedis(request), accessToken, txnTokenExpiryInSeconds)) {
            theiaSessionRedisUtil.hset(accessToken, "tokenDetail", request, txnTokenExpiryInSeconds);
            LOGGER.info("AccessToken {}, created with expiryinSec {}", accessToken, txnTokenExpiryInSeconds);
            if (ThreadLocalUtil.getForMockRequest()) {
                theiaSessionRedisUtil.hset(accessToken, "isMockRequest", TRUE, txnTokenExpiryInSeconds);
            }

            accessTokenBody.setToken(accessToken);
        } else {
            accessTokenBody.setToken((String) theiaSessionRedisUtil.get(getMidReferenceIdKeyForRedis(request)));
            accessTokenBody.setIdempotent(true);
        }
        return accessTokenBody;
    }

    // For Access token validation
    public CreateAccessTokenServiceRequest validateAccessToken(String mid, String referenceId, String token) {
        CreateAccessTokenServiceRequest cachedRequest = getAccessTokenDetail(token);

        if (null == cachedRequest) {
            // MISSING_MANDATORY_ELEMENT
            throw com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException
                    .getException(com.paytm.pgplus.theia.accesstoken.enums.ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(cachedRequest.getMid())
                || org.apache.commons.lang3.StringUtils.isBlank(cachedRequest.getReferenceId())) {
            // UNKNOWN_ERROR
            throw new BaseException();
        }

        if (!org.apache.commons.lang3.StringUtils.equals(cachedRequest.getMid(), mid)
                || !org.apache.commons.lang3.StringUtils.equals(cachedRequest.getReferenceId(), referenceId)) {
            // TOKEN_VALIDATION_EXCEPTION or TOKEN_VALIDATION_FAILED
            throw new BaseException(com.paytm.pgplus.theia.accesstoken.enums.ResultCode.TOKEN_VALIDATION_FAILED);
        }

        return cachedRequest;
    }

    public boolean isValidAccessToken(CreateAccessTokenServiceRequest serviceRequest, AccessTokenBody accessTokenBody) {
        String token = accessTokenBody.getToken();
        CreateAccessTokenServiceRequest cachedRequest = getAccessTokenDetail(token);

        if (null == cachedRequest) {
            return false;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(cachedRequest.getMid())
                || !org.apache.commons.lang3.StringUtils.equals(cachedRequest.getMid(), serviceRequest.getMid())) {
            return false;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(cachedRequest.getReferenceId())
                || !org.apache.commons.lang3.StringUtils.equals(cachedRequest.getReferenceId(),
                        serviceRequest.getReferenceId())) {
            return false;
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(cachedRequest.getPaytmSsoToken())
                && !org.apache.commons.lang3.StringUtils.equals(cachedRequest.getPaytmSsoToken(),
                        serviceRequest.getPaytmSsoToken())) {
            return false;
        }
        return true;
    }

    public CreateAccessTokenServiceRequest getAccessTokenDetail(String token) {
        if (org.apache.commons.lang3.StringUtils.isBlank(token)) {
            throw com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException
                    .getException(com.paytm.pgplus.theia.accesstoken.enums.ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        CreateAccessTokenServiceRequest request = (CreateAccessTokenServiceRequest) fetchField(token, "tokenDetail");

        if (null == request) {
            throw com.paytm.pgplus.theia.accesstoken.exception.SessionExpiredException.getException();
        } else {
            return request;
        }
    }

    private static String getMidReferenceIdKeyForRedis(CreateAccessTokenServiceRequest request) {
        StringBuilder sb = new StringBuilder("AccessTokenRequest_");
        sb.append(request.getMid()).append("_").append(request.getReferenceId());
        return sb.toString();
    }

    public AccessTokenBody createAccessToken(NativeCashierInfoRequest request) {
        CreateAccessTokenServiceRequest createAccessTokenServiceRequest = generateAccessTokenRequest(request);
        return createAccessToken(createAccessTokenServiceRequest);

    }

    private static CreateAccessTokenServiceRequest generateAccessTokenRequest(NativeCashierInfoRequest request) {
        CreateAccessTokenServiceRequest serviceRequest = new CreateAccessTokenServiceRequest();
        serviceRequest.setMid(request.getBody().getMid());
        serviceRequest.setReferenceId(request.getBody().getReferenceId());
        serviceRequest.setPaytmSsoToken(request.getBody().getPaytmSsoToken());

        return serviceRequest;
    }
}
