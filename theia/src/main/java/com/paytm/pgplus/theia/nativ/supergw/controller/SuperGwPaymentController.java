package com.paytm.pgplus.theia.nativ.supergw.controller;

import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailResponse;
import com.paytm.pgplus.theia.nativ.model.bin.NativeBinDetailV4Request;
import com.paytm.pgplus.theia.nativ.model.payview.nb.NativeFetchNBPayChannelResponse;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoV4Request;
import com.paytm.pgplus.theia.nativ.model.payview.request.NativeCashierInfoV4RequestBody;
import com.paytm.pgplus.theia.nativ.model.payview.response.NativeCashierInfoResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.supergw.util.PaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.utils.CustomObjectMapperUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import com.paytm.pgplus.theiacommon.supergw.payview.nb.NativeFetchNBPayChannelRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.utils.EnvInfoUtil.httpServletRequest;

@RestController
@RequestMapping("api/v4")
@NativeControllerAdvice
public class SuperGwPaymentController {

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private PaymentUtil paymentUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwPaymentController.class);

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchBinDetail", method = { RequestMethod.POST })
    public NativeBinDetailResponse fetchBinDetail(
            @ApiParam(required = true) @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestBody NativeBinDetailV4Request request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /v4/fetchBinDetail is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<NativeBinDetailV4Request, NativeBinDetailResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SUPERGW_BIN_DETAIL_REQUEST);
            NativeBinDetailResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /v4/fetchBinDetail is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for SuperGwFetchBinDetail is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }

    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fetchPaymentOptions", method = { RequestMethod.POST })
    public Map<Object, Object> fetchPaymentOptions(
            @ApiParam(required = true) @RequestBody NativeCashierInfoV4Request request,
            @ApiParam(required = true) @RequestParam(value = "referenceId", required = false) String referenceId)
            throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            if (httpServletRequest().getHeader(TheiaConstant.RequestHeaders.TRACK_TLS) != null) {
                LOGGER.info("Insecure Request for Native : UA {}",
                        httpServletRequest().getHeader(TheiaConstant.RequestHeaders.USER_AGENT));
            }
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            request.getHead().setVersion("v4");
            if (request.getBody() == null) {
                request.setBody(new NativeCashierInfoV4RequestBody());
            }
            request.getBody().setExternalFetchPaymentOptions(true);
            request.getBody().setReferenceId(referenceId);
            paymentUtil.setReferenceIdInBody(request); // Adding query param to
                                                       // body
            LOGGER.info("Native request received for API: /v4/fetchPaymentOptions is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<NativeCashierInfoV4Request, NativeCashierInfoResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SUPERGW_PAY_VIEW_CONSULT);
            NativeCashierInfoResponse response = requestProcessor.process(request);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("fetchPaymentOptions", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "fetchPaymentOptions" + "to grafana", exception);
            }
            LOGGER.info("Native response returned for API: /fetchPaymentOptions is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return CustomObjectMapperUtil.jsonToMap(CustomObjectMapperUtil.convertToString(response));
        } finally {
            LOGGER.info("Total time taken for SuperGwFetchPaymentOptions is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "fetchNBPaymentChannels", notes = "To fetch net banking for native payments")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 500, message = "Internal Server error") })
    @RequestMapping(value = "/fetchNBPaymentChannels", method = { RequestMethod.POST })
    public NativeFetchNBPayChannelResponse fetchNBPaymentChannels(
            @ApiParam(required = true) @RequestBody NativeFetchNBPayChannelRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /v4/fetchNBPaymentChannels is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<NativeFetchNBPayChannelRequest, NativeFetchNBPayChannelResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SUPERGW_FETCH_NB_PAY_CHANNEL_REQUEST);
            NativeFetchNBPayChannelResponse response = requestProcessor.process(request);
            LOGGER.info("Native response returned for API: /v4/fetchNBPaymentChannels is: {}", response);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for SuperGwFetchNBPaymentChannels is {} ms", System.currentTimeMillis()
                    - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

}
