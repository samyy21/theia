package com.paytm.pgplus.biz.workflow.aoatimeoutcenter.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.enums.AoaTimeoutCenterUrl;
import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderRequest;
import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderResponse;
import com.paytm.pgplus.common.config.ConfigurationUtil;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.common.statistics.StatisticsLogger;
import com.paytm.pgplus.facade.enums.ExternalEntity;
import com.paytm.pgplus.facade.enums.PaymentPromoServiceUrl;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.paymentpromotion.models.request.ApplyPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.BulkApplyPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.CheckoutPromoServiceRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.PromoReinstateAPIRequest;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.ApplyPromoServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.BulkApplyPromoServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.CheckoutPromoServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.request.v2.SearchPaymentOffersServiceRequestV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.*;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.ApplyPromoServiceResponseV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.BulkApplyPromoServiceResponseV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.CheckoutPromoServiceResponseV2;
import com.paytm.pgplus.facade.paymentpromotion.models.response.v2.SearchPaymentOffersServiceResponseV2;
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
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary Service to handle circuit breaker rollout
 */

@Service
public class AoaTimeoutCenterCircuitBreaker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AoaTimeoutCenterCircuitBreaker.class);

    @SentinelResource(value = "persistOrderInfoAtAoaTimeoutCenter", blockHandler = "persistOrderInfoAtAoaTimeoutCenterFallbackOnCircuitBreak", exceptionsToTrace = FacadeCheckedException.class)
    public AoaTimeoutCenterOrderResponse persistOrderInfoAtAoaTimeoutCenterCircuitBreak(
            AoaTimeoutCenterOrderRequest request, Map<String, String> queryParams) throws FacadeCheckedException {
        return executePost(request, AoaTimeoutCenterUrl.AOA_TIMEOUT_CENTER_ORDER_URL.getUrl()
                + toQueryParamString(queryParams), AoaTimeoutCenterOrderResponse.class);
    }

    public AoaTimeoutCenterOrderResponse persistOrderInfoAtAoaTimeoutCenterFallbackOnCircuitBreak(
            AoaTimeoutCenterOrderRequest request, Map<String, String> queryParams, BlockException ex)
            throws FacadeCheckedException {
        StatisticsLogger.logForXflush("PG", "AOATIMEOUTCENTER", null, "RESPONSE", "CIRCUIT_OPEN", null,
                AoaTimeoutCenterUrl.AOA_TIMEOUT_CENTER_ORDER_URL.getUrl());
        throw new FacadeCheckedException(ex);
    }

    private String toQueryParamString(Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.substring(0, sb.length() - 1);
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

    private <T> String generateBody(final T request) throws FacadeCheckedException {
        if (request == null)
            return null;
        return JsonMapper.mapObjectToJson(request);
    }

    private String toJsonString(Object payloadData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(payloadData);
        } catch (JsonProcessingException e) {
            return "Unable to convert  payloadData to json  :";
        }
    }
}
