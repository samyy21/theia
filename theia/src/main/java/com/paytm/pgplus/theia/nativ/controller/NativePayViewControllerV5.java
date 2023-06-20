package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.aspects.LocaleFieldAspect;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.common.NativeCashierInfoContainerRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequest;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoRequestBody;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.NATIV_FETCH_PAYMENTOPTIONS_URL_V5;
import static com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants.VERSION;
import static com.paytm.pgplus.theia.utils.EnvInfoUtil.httpServletRequest;

@NativeControllerAdvice
@RestController
@RequestMapping("api")
public class NativePayViewControllerV5 {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativePayViewControllerV5.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private LocaleFieldAspect localeFieldAspect;

    @LocaleAPI(apiName = NATIV_FETCH_PAYMENTOPTIONS_URL_V5, responseClass = NativeCashierInfoResponse.class, isResponseObjectType = false)
    @ApiOperation(value = "fetchPaymentOptionsV5", notes = "To fetch payment instruments for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/v5/fetchPaymentOptions", method = { RequestMethod.POST })
    public Map<Object, Object> fetchPaymentOptionsV5(
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

            // For logging and filtering purposes
            MDC.put(VERSION, "v5");
            request.getHead().setVersion("v5");
            request.getBody().setExternalFetchPaymentOptions(true);
            nativePaymentUtil.setReferenceIdInBody(request); // Adding query
            LOGGER.info("Native request received for API: /fetchPaymentOptionsV5 is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<NativeCashierInfoContainerRequest, NativeCashierInfoResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_PAY_VIEW_CONSULT_V5);
            NativeCashierInfoContainerRequest nativeCashierInfoContainerRequest = new NativeCashierInfoContainerRequest(
                    request);
            NativeCashierInfoResponse response = requestProcessor.process(nativeCashierInfoContainerRequest);
            localeFieldAspect.addLocaleFieldsInObject(response, NATIV_FETCH_PAYMENTOPTIONS_URL_V5);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v5/fetchPaymentOptions", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v5/fetchPaymentOptions" + "to grafana",
                        exception);
            }
            LOGGER.debug("Native response returned for API: /fetchPaymentOptionsV5 is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));

        } finally {
            LOGGER.info("Total time taken for NativeFetchPaymentOptionsV5 is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }
}
