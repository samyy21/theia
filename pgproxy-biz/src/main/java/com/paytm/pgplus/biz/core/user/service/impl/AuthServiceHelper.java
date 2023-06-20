package com.paytm.pgplus.biz.core.user.service.impl;

import com.paytm.pgplus.facade.enums.OAuthServiceUrl;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.enums.HttpMethod;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.Base64;

import static com.paytm.pgplus.facade.constants.FacadeConstants.AUTHORIZATION;
import static com.paytm.pgplus.facade.constants.FacadeConstants.HTTP_SUCCESS_CODE;
import static com.paytm.pgplus.facade.utils.LogUtil.logResponse;

public final class AuthServiceHelper {

    private AuthServiceHelper() {
    }

    public static HttpRequestPayload<String> generateRequestPayload(String userId, String clientId,
            String clientSecretKey) {

        HttpRequestPayload<String> payload = new HttpRequestPayload();
        MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap();
        headerMap.add(AUTHORIZATION, "Basic " + getToken(clientId, clientSecretKey));
        headerMap.add("verification_type", "service_token");
        String authUrl = getBasicUserInfoByIdQueryParam(userId);
        payload.setTarget(authUrl);
        payload.setHeaders(headerMap);
        payload.setHttpMethod(HttpMethod.GET);
        return payload;

    }

    private static String getBasicUserInfoByIdQueryParam(String userId) {
        StringBuilder queryParam = new StringBuilder("?");
        queryParam.append("fetch_strategy=basic").append('&');
        queryParam.append("user_id=").append(userId);
        return getBaseUrl().append(queryParam).toString();
    }

    private static StringBuilder getBaseUrl() {
        String url = OAuthServiceUrl.FETCH_BASIC_USER_WITH_ID.getUrl();
        StringBuilder targetUrl = new StringBuilder(url);
        return targetUrl;
    }

    private static String getToken(final String clientId, final String secretKey) {
        return new String(Base64.getEncoder().encode((clientId + ":" + secretKey).getBytes()));

    }

    public static boolean isValidResponse(Response response) {
        logResponse(response.getStatus(), response.getHeaders(), response.getEntity());
        return HTTP_SUCCESS_CODE.equals(response.getStatus());
    }
}
