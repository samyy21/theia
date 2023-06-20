package com.paytm.pgplus.biz.utils;

import com.paytm.pgplus.biz.model.aoaorderlookup.AOAOrderLookUpRequest;
import com.paytm.pgplus.biz.model.aoaorderlookup.AOAOrderLookUpResponse;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.dynamicwrapper.utils.JSONUtils.toJsonString;
import static com.paytm.pgplus.facade.enums.AOAServiceUrl.AOA_ORDER_LOOKUP;

@Service
public class UPIPSPUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UPIPSPUtil.class);
    @Autowired
    Environment environment;

    public boolean orderPresentOnAOA(String mid, String orderId) throws FacadeCheckedException {

        AOAOrderLookUpRequest request = new AOAOrderLookUpRequest(mid, orderId, "PG");
        AOAOrderLookUpResponse response = fetchAOAOrderLookUpResponse(request);

        return response != null && response.isOrderExists();
    }

    private String createJWTTokenForAOAOrderLookUp(String mid, String orderId) {
        Map<String, String> claims = new HashMap<>();

        claims.put("mid", mid);
        claims.put("inquirer", "PG");
        claims.put("orderId", orderId);

        String key = environment.getProperty("aoa-order-lookup.secret-key.pg-client");

        return JWTWithHmacSHA256.createUpiVpaJsonWebToken(claims, key, "pg-theia");
    }

    private AOAOrderLookUpResponse fetchAOAOrderLookUpResponse(AOAOrderLookUpRequest request)
            throws FacadeCheckedException {
        String token = createJWTTokenForAOAOrderLookUp(request.getMid(), request.getOrderId());
        String url = AOA_ORDER_LOOKUP.getUrl();
        final HttpRequestPayload<String> payload = generateAOAOrderLookUpPayload(request, url, token);
        try {
            LogUtil.logPayload(url, Type.REQUEST, payload.toString());
            final Response response = JerseyHttpClient.sendHttpPostRequest(payload);
            final String responseEntity = response.readEntity(String.class);
            final AOAOrderLookUpResponse responseObject = JsonMapper.mapJsonToObject(responseEntity,
                    AOAOrderLookUpResponse.class);
            String responseString = toJsonString(responseObject);
            LogUtil.logPayload(url, Type.RESPONSE, responseString);
            return responseObject;
        } catch (final Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new FacadeCheckedException(e);
        }
    }

    private HttpRequestPayload<String> generateAOAOrderLookUpPayload(final AOAOrderLookUpRequest request, String url,
            String token) throws FacadeCheckedException {
        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        headerMap.add("Content-Type", MediaType.APPLICATION_JSON);
        headerMap.add("Accept", MediaType.APPLICATION_JSON);
        headerMap.add("client-token", token);
        headerMap.add("client-token-type", "JWT");
        headerMap.add("client-id", "pg-theia");
        payload.setTarget(url);
        payload.setHeaders(headerMap);
        String requestBody = request == null ? null : JsonMapper.mapObjectToJson(request);
        payload.setEntity(requestBody);
        return payload;
    }
}
