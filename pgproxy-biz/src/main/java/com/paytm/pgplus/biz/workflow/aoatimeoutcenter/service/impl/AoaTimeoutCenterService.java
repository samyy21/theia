package com.paytm.pgplus.biz.workflow.aoatimeoutcenter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.enums.AoaTimeoutCenterUrl;
import com.paytm.pgplus.biz.workflow.aoatimeoutcenter.service.IAoaTimeoutCenterService;
import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderRequest;
import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderResponse;
import com.paytm.pgplus.facade.enums.ExternalEntity;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentpromotion.services.impl.PaymentPromoService;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.pgpff4jclient.IPgpFf4jClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

@Service("aoaTimeoutCenterService")
public class AoaTimeoutCenterService implements IAoaTimeoutCenterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AoaTimeoutCenterService.class);

    @Autowired
    private AoaTimeoutCenterCircuitBreaker aoaTimeoutCenterCircuitBreaker;

    @Autowired
    private IPgpFf4jClient iPgpFf4jClient;

    @Override
    public AoaTimeoutCenterOrderResponse persistOrderInfoAtAoaTimeoutCenter(AoaTimeoutCenterOrderRequest request,
            Map<String, String> queryParams) throws FacadeCheckedException {
        if (isCircuitBreakFeatureEnabled()) {
            aoaTimeoutCenterCircuitBreaker.persistOrderInfoAtAoaTimeoutCenterCircuitBreak(request, queryParams);
        }
        return executePost(request, AoaTimeoutCenterUrl.AOA_TIMEOUT_CENTER_ORDER_URL.getUrl()
                + toQueryParamString(queryParams), AoaTimeoutCenterOrderResponse.class);
    }

    private <Req, Resp> Resp executePost(Req request, String url, Class<Resp> respClass) throws FacadeCheckedException {
        final HttpRequestPayload<String> payload = generatePayload(request, url);
        try {
            LogUtil.logPayload(url, Type.REQUEST, payload.toString());
            final Response response = JerseyHttpClient.sendHttpPostRequest(payload);
            final String responseEntity = response.readEntity(String.class);
            final Resp responseObject = JsonMapper.mapJsonToObject(responseEntity, respClass);
            String responseString = toJsonString(responseObject);
            LogUtil.logPayload(url, Type.RESPONSE, responseString);
            return responseObject;
        } catch (final Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw new FacadeCheckedException(e);
        }
    }

    private <T> HttpRequestPayload<String> generatePayload(final T request, String url) throws FacadeCheckedException {
        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        headerMap.add("content-type", MediaType.APPLICATION_JSON);
        headerMap.add("X-REQUEST-ID", UUID.randomUUID().toString());
        payload.setTarget(url);
        payload.setHeaders(headerMap);
        String requestBody = generateBody(request);
        payload.setEntity(requestBody);
        return payload;
    }

    private String toJsonString(Object payloadData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(payloadData);
        } catch (JsonProcessingException e) {
            return "Unable to convert  payloadData tojson  :";
        }
    }

    private String toQueryParamString(Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private <T> String generateBody(final T request) throws FacadeCheckedException {
        if (request == null)
            return null;
        return JsonMapper.mapObjectToJson(request);
    }

    private boolean isCircuitBreakFeatureEnabled() {
        try {
            return iPgpFf4jClient.checkWithdefault("theia.circuitBreaker", null, false);

        } catch (Exception e) {
            LOGGER.error("Exception getting feature {} from ff4j", "theia.circuitBreaker");
            return false;
        }
    }
}
