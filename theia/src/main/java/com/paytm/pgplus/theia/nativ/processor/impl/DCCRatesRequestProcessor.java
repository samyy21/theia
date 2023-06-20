package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.model.DccPaymentDetail;
import com.paytm.pgplus.common.model.DccPaymentDetailResponse;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.enums.HttpMethod;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.model.common.DccPaymentDetailRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.services.IConsumableInstaProxyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static com.paytm.pgplus.facade.constants.FacadeConstants.ConsentAPIConstant.CONTENT_TYPE;
import static com.paytm.pgplus.theia.constants.TheiaConstant.DccConstants.*;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.SUCCESS;
import static com.paytm.pgplus.theia.constants.TheiaConstant.TheiaInstaConstants.INSTAPROXY_FETCH_DCC_RATES_URL;

@Service("fetchDccRatesRequestProcessor")
public class DCCRatesRequestProcessor
        extends
        AbstractRequestProcessor<DccPaymentDetailRequest, DccPaymentDetail, DccPaymentDetailRequest, DccPaymentDetailResponse>
        implements
        IConsumableInstaProxyService<DccPaymentDetailRequest, DccPaymentDetailRequest, DccPaymentDetailResponse> {

    public static final Logger LOGGER = LoggerFactory.getLogger(DCCRatesRequestProcessor.class);

    @Override
    protected DccPaymentDetailRequest preProcess(DccPaymentDetailRequest request) {
        validateRequest(request);
        return request;
    }

    @Override
    protected DccPaymentDetailResponse onProcess(DccPaymentDetailRequest request, DccPaymentDetailRequest serviceRequest) {
        LOGGER.info("Instaproxy Request : " + serviceRequest);
        DccPaymentDetailResponse instaProxyAPIResponse = null;
        try {
            /** Calling InstaProxy API */
            instaProxyAPIResponse = callInstaProxyService(request, serviceRequest);
            return instaProxyAPIResponse;
        } catch (Exception e) {
            LOGGER.error("Exception in calling InstaProxy dcc Fetch Rates service {}", e.getMessage());

        }
        return instaProxyAPIResponse;
    }

    @Override
    protected DccPaymentDetail postProcess(DccPaymentDetailRequest request,
            DccPaymentDetailRequest dccPaymentDetailServiceRequest, DccPaymentDetailResponse dccPaymentDetailResponse) {
        DccPaymentDetail dccPaymentDetail = null;

        if (dccPaymentDetailResponse != null) {

            if (SUCCESS.equalsIgnoreCase(dccPaymentDetailResponse.getApiStatus())
                    && dccPaymentDetailResponse.getDccOffered()) {
                LOGGER.info("Insta Dcc Rates API Response {}", dccPaymentDetailResponse.toString());
                dccPaymentDetail = convertToDccPaymentDetail(dccPaymentDetailResponse);
                dccPaymentDetail.setConvenienceFeeInInr(request.getPcfAmount());
                dccPaymentDetail.setOrderAmount(request.getAmount());
            } else {
                LOGGER.info("Insta dcc Rates API Resposne {}", dccPaymentDetailResponse.toString());
            }
        } else {
            LOGGER.error("Something Went Wrong in Fetching Dcc Rates from Insta");
        }
        return dccPaymentDetail;
    }

    private DccPaymentDetail convertToDccPaymentDetail(DccPaymentDetailResponse dccPaymentDetailResponse) {
        DccPaymentDetail dccPaymentDetail = null;
        final String responseString;
        try {
            responseString = JsonMapper.mapObjectToJson(dccPaymentDetailResponse);
            LOGGER.debug("Response received is :: {}", responseString);

            if (StringUtils.isNotBlank(responseString)) {

                dccPaymentDetail = JsonMapper.mapJsonToObject(responseString, DccPaymentDetail.class);
                LOGGER.debug("dccPaymentDetailObject {}  ", dccPaymentDetail);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("Something went wrong {}", e.getMessage());
        }
        return dccPaymentDetail;

    }

    @Override
    public DccPaymentDetailResponse callInstaProxyService(DccPaymentDetailRequest dccPaymentDetailRequest,
            DccPaymentDetailRequest dccPaymentDetailServiceRequest) {
        try {
            String targetUrl = getUrl(TheiaConstant.TheiaInstaConstants.INSTAPROXY_BASE_URL)
                    + INSTAPROXY_FETCH_DCC_RATES_URL;
            HttpRequestPayload<String> payload = fetchDccRatesInstaRequest(dccPaymentDetailServiceRequest, targetUrl);
            LOGGER.info("DccRatesRequestProcessor.callInstaProxyService | serviceRequest log {}", payload.toString());
            Response response = initiateInstaPostServiceCall(payload);
            if (response != null) {
                DccPaymentDetailResponse dccPaymentDetailResponse = convertResponseToInstaProxyResponse(response,
                        DccPaymentDetailResponse.class);
                LOGGER.info("DCCRatesRequestProcessor.callInstaProxyService response :{}",
                        dccPaymentDetailResponse.toString());
                return dccPaymentDetailResponse;
            }
        } catch (Exception e) {
            LOGGER.error("Something went wrong {} ", e.getMessage());
        }
        return null;
    }

    private HttpRequestPayload<String> fetchDccRatesInstaRequest(DccPaymentDetailRequest serviceRequest,
            String targetUrl) throws Exception {
        final HttpRequestPayload<String> payload = new HttpRequestPayload<>();
        final MultivaluedMap<String, Object> headerMap = new MultivaluedHashMap<>();
        payload.setHttpMethod(HttpMethod.POST);
        payload.setMediaTypeProduced(MediaType.APPLICATION_JSON);
        payload.setMediaTypeConsumed(MediaType.APPLICATION_JSON);
        payload.setTarget(targetUrl);
        final String body = JsonMapper.mapObjectToJson(serviceRequest);
        payload.setEntity(body);
        headerMap.add(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        payload.setHeaders(headerMap);
        return payload;
    }

    private void validateRequest(DccPaymentDetailRequest request) {

        if (null == request) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), INVALID_REQUEST));
        }
        if (StringUtils.isBlank(request.getVersion())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), EMPTY_VERSION));
        }
        if (StringUtils.isBlank(request.getRequestTimeStamp())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), EMPTY_REQUEST_TIMESTAMP));
        }

        if (StringUtils.isBlank(request.getMid())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), INVALID_MID));
        }
        if (StringUtils.isBlank(request.getClient())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), EMPTY_CLIENT));
        }
        if (StringUtils.isBlank(request.getOrderId())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), EMPTY_ORDER_ID));
        }
        if (StringUtils.isBlank(request.getBankCode())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), EMPTY_BANK_CODE));
        }
        if (StringUtils.isBlank(request.getBin()) && StringUtils.isNumeric(request.getBin())
                && request.getBin().length() == SIZE_OF_BIN) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), INVALID_BIN));
        }
        if (StringUtils.isBlank(request.getPayMode())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), INVALID_PAY_MODE));
        }
        if (StringUtils.isBlank(request.getAmount())) {
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(), INVALID_AMOUNT));
        }

    }

}
