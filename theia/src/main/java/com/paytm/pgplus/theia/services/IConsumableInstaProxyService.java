package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.httpclient.exception.HttpCommunicationException;
import com.paytm.pgplus.httpclient.exception.IllegalPayloadException;
import com.paytm.pgplus.theia.nativ.model.one.click.response.InstaProxyDeEnrollOneClickResponse;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

public interface IConsumableInstaProxyService<InstaProxyRequest, InstaProxyServiceRequest, InstaProxyResponse> {

    Logger LOGGER = LoggerFactory.getLogger(IConsumableInstaProxyService.class);

    InstaProxyResponse callInstaProxyService(InstaProxyRequest request, InstaProxyServiceRequest serviceRequest)
            throws Exception;

    default MultivaluedMap<String, String> getMultivaluedMapRequest(Map<String, String> requestMap) {

        MultivaluedMap<String, String> bodyParams = new MultivaluedHashMap<String, String>();
        if (null != requestMap && requestMap.size() > 0) {
            for (Map.Entry<String, String> param : requestMap.entrySet()) {
                bodyParams.add(param.getKey(), param.getValue());
            }
        }
        return bodyParams;
    }

    default Response initiatePostServiceCall(HttpRequestPayload<String> obj) throws HttpCommunicationException,
            IllegalPayloadException {

        Response clientResponse = null;
        try {
            clientResponse = JerseyHttpClient.sendHttpPostRequest(obj);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        if (clientResponse != null && clientResponse.getStatus() != 200) {
            LOGGER.error("Status for initiatePostServiceCall call is : " + clientResponse.getStatus());
        }
        return clientResponse;
    }

    default Response sendRequestToInstaProxy(HttpRequestPayload<MultivaluedMap<String, String>> obj) {

        Response clientResponse = null;
        try {
            clientResponse = JerseyHttpClient.sendHttpPostRequest(obj);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        if (clientResponse.getStatus() != 200) {
            LOGGER.error("Status for InstaProxy call is : " + clientResponse.getStatus());
        }
        return clientResponse;
    }

    default InstaProxyResponse convertResponseToInstaProxyResponse(Response response, Class<InstaProxyResponse> clasz)
            throws FacadeCheckedException {
        InstaProxyResponse responseObject = null;

        final String responseString = response.readEntity(String.class);
        LOGGER.debug("Response received is :: {}", response);

        if (StringUtils.isNotBlank(responseString)) {

            responseObject = JsonMapper.mapJsonToObject(responseString, clasz);
            LOGGER.debug("responseObject ", responseObject);
        }
        return responseObject;
    }

    default Response initiateInstaPostServiceCall(HttpRequestPayload<String> obj) {

        Response clientResponse = null;
        try {
            clientResponse = JerseyHttpClient.sendHttpPostRequest(obj);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        if (clientResponse != null && clientResponse.getStatus() != 200) {
            LOGGER.error("Status for initiatePostServiceCall call is : " + clientResponse.getStatus());
        }
        return clientResponse;
    }

    default String getUrl(String targetUrlProperty) throws Exception {

        String targetUrl = ConfigurationUtil.getProperty(targetUrlProperty, "");
        if (StringUtils.isBlank(targetUrl)) {
            throw new Exception("Exception occurred: Unable to retrieve InstaProxy URL from property. ResultCode: "
                    + ResultCode.FAILED);
        }

        return targetUrl;
    }
}
