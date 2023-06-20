package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.log.EventLogger;
import com.paytm.pgplus.common.util.MaskingUtil;
import com.paytm.pgplus.localisationProcessor.annotation.LocaleAPI;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.BalanceInfoResponse;
import com.paytm.pgplus.theia.nativ.model.balanceinfo.FetchBalanceInfoRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory.RequestType;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.nativ.utils.NativeSessionUtil;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.Native.MID;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeBalanceInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeBalanceInfoController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private AWSStatsDUtils statsDUtils;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private NativeSessionUtil nativeSessionUtil;

    @SuppressWarnings("unchecked")
    @LocaleAPI(apiName = "/theia/api/v1/fetchBalanceInfo", responseClass = BalanceInfoResponse.class)
    @RequestMapping(value = "/fetchBalanceInfo", method = { RequestMethod.POST })
    public BalanceInfoResponse fetchBalanceInfo(
            @ApiParam(required = true) @RequestBody FetchBalanceInfoRequest request,
            @RequestParam Map<String, String> queryParams) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /fetchBalanceInfo is: {}", request);
            setMid(request, queryParams);
            nativePaymentUtil.setReferenceIdInBody(request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
            IRequestProcessor<FetchBalanceInfoRequest, BalanceInfoResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestType.NATIVE_FETCH_BALANCE_INFO);
            BalanceInfoResponse response = requestProcessor.process(request);
            String fetchBalanceResponse = MaskingUtil.maskingString(response.toString(), "value", 0, 0);
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("api/v1/fetchBalanceInfo", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "api/v1/fetchBalanceInfo" + "to grafana", exception);
            }

            // event logging with additional info
            logAdditionalInfo(request, queryParams, response);

            LOGGER.info("Native response returned for API: /fetchBalanceInfo is: {}", fetchBalanceResponse);
            nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                    .getBody()));
            return response;
        } finally {
            LOGGER.info("Total time taken for NativeFetchBalanceInfo is {} ms", System.currentTimeMillis() - startTime);
            nativePaymentUtil.logNativeResponse(startTime);
        }
    }

    private void setMid(FetchBalanceInfoRequest request, Map<String, String> queryParams) {
        if (request.getBody().getMid() == null) {
            request.getBody().setMid(queryParams.get(MID));
        }
    }

    private void logAdditionalInfo(FetchBalanceInfoRequest request, Map<String, String> queryParams,
            BalanceInfoResponse response) {
        try {
            UserDetailsBiz userDetailsBiz = nativeSessionUtil.getUserDetails(request.getHead().getTxnToken());
            queryParams.put("userId", userDetailsBiz != null ? userDetailsBiz.getUserId() : null);
            queryParams.put("request", request.toString());
            queryParams.put("response", response.toString());
        } catch (Exception e) {
            LOGGER.info("Exception in fetching details for logging {}", e);
        }
        String mid = MDC.get(TheiaConstant.RequestParams.MID);
        String orderId = MDC.get(TheiaConstant.RequestParams.ORDER_ID);
        EventLogger.pushEventLog(mid, orderId, EventNameEnum.NATIVE, queryParams);
    }
}