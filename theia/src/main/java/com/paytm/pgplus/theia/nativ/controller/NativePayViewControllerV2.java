package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.FetchQRPaymentDetailsRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.FetchQRPaymentDetailsResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.utils.CustomObjectMapperUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIV_FETCH_PAYMENTOPTIONS_URL_V2;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType.FETCH_QR_PAYMENT_DETAILS;
import static com.paytm.pgplus.theia.utils.EnvInfoUtil.httpServletRequest;

@NativeControllerAdvice
@RestController
@RequestMapping("api")
public class NativePayViewControllerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePayViewControllerV2.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = NATIV_FETCH_PAYMENTOPTIONS_URL_V2, responseClass = NativeCashierInfoResponse.class, isResponseObjectType = false)
    @ApiOperation(value = "fetchPaymentOptionsV2", notes = "To fetch payment instruments for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/v2/fetchPaymentOptions", method = { RequestMethod.POST })
    public Map<Object, Object> fetchPaymentOptionsV2(
            @ApiParam(required = true) @RequestBody NativeCashierInfoRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            if (httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null) {
                LOGGER.info("Insecure Request for Native : UA {}",
                        httpServletRequest().getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
            }
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            if (request.getBody() == null) {
                request.setBody(new NativeCashierInfoRequestBody());
            }
            if (request.getHead() != null && StringUtils.equals("v3", request.getHead().getVersion())) {
                request.getBody().setReturnDisabledChannels(true);
            }
            // For logging and filtering purposes
            MDC.put(VERSION, "v2");
            request.getHead().setVersion("v2");
            request.getBody().setExternalFetchPaymentOptions(true);
            nativePaymentUtil.setReferenceIdInBody(request); // Adding query
                                                             // param to body
            nativePaymentUtil.setReferenceIdInBody(request); // Adding query
                                                             // param to body
            LOGGER.info("Native request received for API: /fetchPaymentOptionsV2 is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<NativeCashierInfoContainerRequest, NativeCashierInfoResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_PAY_VIEW_CONSULT);
            NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                    request);
            NativeCashierInfoResponse response = requestProcessor.process(nativeCashierInfoContainerRequest);
            localeFieldAspect.addLocaleFieldsInObject(response, NATIV_FETCH_PAYMENTOPTIONS_URL_V2);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v2/fetchPaymentOptions", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v2/fetchPaymentOptions" + "to grafana",
                        exception);
            }
            LOGGER.debug("Native response returned for API: /fetchPaymentOptionsV2 is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));

        } finally {
            LOGGER.info("Total time taken for NativeFetchPaymentOptionsV2 is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "fetchQRPaymentDetailsV2", notes = "To fetch payment instruments for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/v2/fetchQRPaymentDetails", method = { RequestMethod.POST })
    public Map<Object, Object> fetchQRPaymentOptions(
            @ApiParam(required = true) @RequestBody FetchQRPaymentDetailsRequest request,
            @RequestParam LinkedHashMap<String, String> queryParams,
            @RequestParam(value = "appVersion") String appVersion) throws Exception {
        long startTime = System.currentTimeMillis();
        String apiUrl = TheiaConstant.ExtraConstants.FETCH_QR_PAYMENT_DETAILS_V2;
        try {
            if (httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null) {
                LOGGER.info("Insecure Request for Native : UA {}",
                        httpServletRequest().getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
            }
            // For logging and filtering purposes
            MDC.put(VERSION, TheiaConstant.RequestHeaders.Version_V2);
            if (request.getHead() != null) {
                request.getHead().setVersion(TheiaConstant.RequestHeaders.Version_V2);
            }

            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            LOGGER.info("Native request received for API: {} is: {}", apiUrl, request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<FetchQRPaymentDetailsRequest, FetchQRPaymentDetailsResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(FETCH_QR_PAYMENT_DETAILS);
            if (request.getBody() != null) {
                request.getBody().setAppVersion(appVersion);
                // for logging device details in event and passing details in
                // UPI request
                request.getBody().setQueryParams(queryParams);
            }
            FetchQRPaymentDetailsResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("fetchQRPaymentDetailsV2", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "fetchQRPaymentDetailsV2" + "to grafana", exception);
            }

            LOGGER.info("Native response returned for API: {} is: {}", apiUrl, response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            nativePaymentUtil.logNativeResponse(queryParams, EventNameEnum.FETCH_QR_PAYMENT_DETAILS_V2);
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));
        } finally {
            LOGGER.info("Total time taken for {} is {} ms", apiUrl, System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime, apiUrl);
        }
    }

}
