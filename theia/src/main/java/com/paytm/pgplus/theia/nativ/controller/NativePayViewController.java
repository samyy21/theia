package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.response.ResultInfo;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.emi.*;
import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelRequest;
import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelResponse;
import com.paytm.pgplus.theia.nativ.model.payview.request.FetchQRPaymentDetailsRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.FetchQRPaymentDetailsResponse;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailRequest;
import com.paytm.pgplus.theia.nativ.model.pcfDetails.FetchPcfDetailResponse;
import com.paytm.pgplus.theia.nativ.model.vpa.details.FetchVpaDetailsRequest;
import com.paytm.pgplus.theia.nativ.model.vpa.details.VpaDetailsResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.utils.CustomObjectMapperUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType.FETCH_QR_PAYMENT_DETAILS;

@NativeControllerAdvice
@RestController
@RequestMapping("api/v1")
public class NativePayViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePayViewController.class);
    private static final String FETCH_PAYMENT_V1_API_NAME = "/theia/api/v1/fetchPaymentOptions";

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = FETCH_PAYMENT_V1_API_NAME, responseClass = NativeCashierInfoResponse.class, isResponseObjectType = false)
    @ApiOperation(value = "fetchPaymentOptions", notes = "To fetch payment instruments for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/fetchPaymentOptions", method = { RequestMethod.POST })
    public Map<Object, Object> fetchPaymentOptions(
            @ApiParam(required = true) @RequestBody NativeCashierInfoRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            if (httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null) {
                LOGGER.info("Insecure Request for Native : UA {}",
                        httpServletRequest().getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
            }
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            request.getHead().setVersion("v1");
            if (request.getBody() == null) {
                request.setBody(new NativeCashierInfoRequestBody());
            }
            request.getBody().setExternalFetchPaymentOptions(true);
            nativePaymentUtil.setReferenceIdInBody(request); // Adding query
                                                             // param to body
            LOGGER.info("Native request received for API: /fetchPaymentOptions is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<NativeCashierInfoContainerRequest, NativeCashierInfoResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_PAY_VIEW_CONSULT);
            NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                    request);
            NativeCashierInfoResponse response = requestProcessor.process(nativeCashierInfoContainerRequest);
            localeFieldAspect.addLocaleFieldsInObject(response, FETCH_PAYMENT_V1_API_NAME);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v1/fetchPaymentOptions", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v1/fetchPaymentOptions" + "to grafana",
                        exception);
            }

            LOGGER.debug("Native response returned for API: /fetchPaymentOptions is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));
        } finally {
            LOGGER.info("Total time taken for NativeFetchPaymentOptions is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = "/theia/api/v1/fetchNBPaymentChannels", responseClass = NativeFetchNBPayChannelResponse.class)
    @ApiOperation(value = "fetchNBPaymentChannels", notes = "To fetch net banking for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/fetchNBPaymentChannels", method = { RequestMethod.POST })
    public NativeFetchNBPayChannelResponse fetchNBPaymentChannels(
            @ApiParam(required = true) @RequestBody NativeFetchNBPayChannelRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            nativePaymentUtil.setReferenceIdInBody(request);
            LOGGER.info("Native request received for API: /fetchNBPaymentChannels is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<NativeFetchNBPayChannelRequest, NativeFetchNBPayChannelResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_FETCH_NB_PAY_CHANNEL_REQUEST);
            // OfflinePaymentUtils.gethttpServletResponse().setHeader("Access-Control-Allow-Origin",
            // "*");
            NativeFetchNBPayChannelResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("fetchNBPaymentChannels", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "fetchNBPaymentChannels" + "to grafana", exception);
            }
            LOGGER.info("Native response returned for API: /fetchNBPaymentChannels is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchNBPaymentChannels is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "fetchEMIPaymentChannels", notes = "To fetch net banking for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/fetchEMIPaymentChannels", method = { RequestMethod.POST })
    public FetchEmiPayChannelResponse fetchEMIPaymentChannels(
            @ApiParam(required = true) @RequestBody FetchEmiPayChannelRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchEMIPaymentChannels is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<FetchEmiPayChannelRequest, FetchEmiPayChannelResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_FETCH_EMI_PAY_CHANNEL_REQUEST);
            FetchEmiPayChannelResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /fetchEMIPaymentChannels is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchEMIPaymentChannels is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @LocaleAPI(apiName = "/theia/api/v1/fetchEMIDetail", responseClass = FetchEmiDetailResponse.class)
    @SuppressWarnings("unchecked")
    @ApiOperation(value = "fetchEMIDetail", notes = "To fetch net banking for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/fetchEMIDetail", method = { RequestMethod.POST })
    public FetchEmiDetailResponse fetchEMIDetail(@ApiParam(required = true) @RequestBody FetchEmiDetailRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchEMIDetail is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<FetchEmiDetailRequest, FetchEmiDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_FETCH_EMI_DETAIL_REQUEST);
            if (StringUtils.isNotBlank(request.getBody().getEmiType())) {
                EPayMethod ePayMethod = EPayMethod.getPayMethodByOldName(request.getBody().getEmiType());
                if (ePayMethod != null) {
                    request.getBody().setEmiType(ePayMethod.getMethod());
                }
            }
            FetchEmiDetailResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /fetchEMIDetail is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchEMIDetail is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchVpaDetails", method = { RequestMethod.POST })
    public VpaDetailsResponse fetchVpaDetails(@ApiParam(required = true) @RequestBody FetchVpaDetailsRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchVpaDetails is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<FetchVpaDetailsRequest, VpaDetailsResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_FETCH_VPA_DETAILS);
            VpaDetailsResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /fetchVpaDetails is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchVpaDetails is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/checkEMIEligibility", method = { RequestMethod.POST })
    public EMIEligibilityResponse checkEmiEligibility(
            @ApiParam(required = true) @RequestBody EMIEligibilityRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /checkEMIEligibility is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<EMIEligibilityRequest, EMIEligibilityResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.CHECK_EMI_ELIGIBILITY);
            EMIEligibilityResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /checkEMIEligibility is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for checkEMIEligibility is {} ms", System.currentTimeMillis() - startTime);
        }

    }

    private HttpServletRequest httpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = "/theia/api/v1/fetchPcfDetails", responseClass = FetchPcfDetailResponse.class)
    @RequestMapping(value = "/fetchPcfDetails", method = { RequestMethod.POST })
    public FetchPcfDetailResponse fetchPcfDetails(@ApiParam(required = true) @RequestBody FetchPcfDetailRequest request)
            throws Exception {
        long startTime = System.currentTimeMillis();
        FetchPcfDetailResponse response = null;
        try {
            LOGGER.info("Native request received for API: /fetchPcfDetails is: {}", request);
            nativePaymentUtil.setReferenceIdInBody(request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<FetchPcfDetailRequest, FetchPcfDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_FETCH_PCF_DETAILS);
            response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /fetchPcfDetails is: {}", response);
            if (response != null && response.getBody() != null && response.getBody().getResultInfo() != null) {
                logToStatsdMetric(response.getBody().getResultInfo().getResultStatus(), response.getBody()
                        .getResultInfo().getResultMsg());
            }
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } catch (BaseException ex) {
            if (ex.getResultInfo() != null)
                logToStatsdMetric(ex.getResultInfo().getResultStatus(), ex.getResultInfo().getResultMsg());
            throw ex;
        } finally {
            LOGGER.info("Total time taken for NativeFetchPcfDetails is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "getEmiDetails", notes = "Purpose of this API is to fetch Emi Details for a specific merchant.")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/getEmiDetails", method = { RequestMethod.POST })
    public MerchantEmiDetailResponse getEmiDetails(
            @ApiParam(required = true) @RequestBody MerchantEmiDetailRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /getEmiDetails is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<MerchantEmiDetailRequest, MerchantEmiDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_GET_MERCHANT_EMI_DETAILS);
            MerchantEmiDetailResponse response = requestProcessor.process(request);
            ResponseHeader head = new ResponseHeader();
            head.setVersion(request.getHead().getVersion());
            head.setResponseTimestamp(String.valueOf(System.currentTimeMillis() - startTime));
            response.setHead(head);
            LOGGER.info("Native response returned for API: /getEmiDetails is: {}", response);
            nativePaymentUtil.logNativeResponse(getResultInfo(response));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeGetEMIDetails is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "fetchQRPaymentDetails", notes = "To fetch payment instruments for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/fetchQRPaymentDetails", method = { RequestMethod.POST })
    public Map<Object, Object> fetchQRPaymentOptions(
            @ApiParam(required = true) @RequestBody FetchQRPaymentDetailsRequest request,
            @RequestParam LinkedHashMap<String, String> queryParams,
            @RequestParam(value = "appVersion") String appVersion) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            if (httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null) {
                LOGGER.info("Insecure Request for Native : UA {}",
                        httpServletRequest().getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
            }
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            LOGGER.info("Native request received for API: /fetchQRPaymentDetails is: {}", request);
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

            LOGGER.info("Native response returned for API: /fetchQRPaymentDetails is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            nativePaymentUtil.logNativeResponse(queryParams, EventNameEnum.FETCH_QR_PAYMENT_DETAILS);
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));
        } finally {
            LOGGER.info("Total time taken for FetchQRPaymentDetails is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime, TheiaConstant.ExtraConstants.FETCH_QR_PAYMENT_DETAILS);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/guest/fetchPaymentOptions", method = { RequestMethod.POST })
    public Map<Object, Object> fetchPaymentOptionsForGuest(
            @ApiParam(required = true) @RequestBody NativeCashierInfoRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            request.getHead().setVersion("v5");
            LOGGER.info("Native request received for API: /guest/fetchPaymentOptions is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<NativeCashierInfoContainerRequest, NativeCashierInfoResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_PAY_VIEW_CONSULT_V5);
            NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                    request);
            NativeCashierInfoResponse response = requestProcessor.process(nativeCashierInfoContainerRequest);
            response.getHead().setVersion("v1");
            LOGGER.debug("Native response returned for API: /guest/fetchPaymentOptions is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));
        } finally {
            LOGGER.info("Total time taken for NativeFetchPaymentOptions is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    private ResultInfo getResultInfo(MerchantEmiDetailResponse response) {
        if (null == response || null == response.getBody()) {
            return null;
        }
        return response.getBody().getResultInfo();
    }

    private void logToStatsdMetric(String resultStatus, String resultMsg) {
        try {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("RESPONSE_STATUS", resultStatus);
            responseMap.put("RESPONSE_MESSAGE", resultMsg);
            statsDUtils.pushResponse("fetchPcfDetails", responseMap);
        } catch (Exception exception) {
            LOGGER.error("Error in pushing response message " + "fetchPcfDetails" + "to grafana", exception);
        }
    }

}
