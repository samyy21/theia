package com.paytm.pgplus.theia.nativ.controller;

/**
 * Created by paraschawla on 11/4/18.
 */

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.FetchMerchantInfoRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantInfoResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigRequest;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.utils.EnvInfoUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders.Version_V2;

@RestController
@RequestMapping("api")
@NativeControllerAdvice
public class FetchMerchantInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchMerchantInfoController.class);

    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(FetchMerchantInfoController.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/v1/fetchMerchantInfo", method = { RequestMethod.POST })
    @ApiOperation(value = "/fetchMerchantInfo", notes = "Get Merchant Info and Map SSO_TOKEN and TXN_TOKEN")
    public MerchantInfoResponse fetchMerchantInfo(
            @ApiParam(required = true) @RequestBody FetchMerchantInfoRequest request) throws Exception {
        LOGGER.info("Fetch Merchant info request received for API: /fetchMerchantInfo is: {}", request);
        EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
        IRequestProcessor<FetchMerchantInfoRequest, MerchantInfoResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_MERCHANT_INFO);
        MerchantInfoResponse response = requestProcessor.process(request);
        LOGGER.info("Fetch Merchant info response returned for API: /v1/fetchMerchantInfo is: {}", response);
        return response;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/v2/fetchMerchantInfo", method = { RequestMethod.POST })
    @ApiOperation(value = "/fetchMerchantInfo", notes = "Get Merchant Info and Map SSO_TOKEN and TXN_TOKEN")
    public MerchantInfoResponse fetchMerchantInfoV2(
            @ApiParam(required = true) @RequestBody FetchMerchantInfoRequest request) throws Exception {
        LOGGER.info("Fetch Merchant info request received for API: /fetchMerchantInfo is: {}", request);
        request.getHead().setVersion(Version_V2);
        com.paytm.pgplus.biz.utils.EventUtils.pushTheiaEvents(EventNameEnum.FMIV2);
        EnvInfoUtil.setChannelDFromUserAgent(request.getHead());
        IRequestProcessor<FetchMerchantInfoRequest, MerchantInfoResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_MERCHANT_INFO);
        MerchantInfoResponse response = requestProcessor.process(request);
        try {
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
            responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
            statsDUtils.pushResponse("v2/fetchMerchantInfo", responseMap);
        } catch (Exception exception) {
            LOGGER.error("Error in pushing response message " + "v2/fetchMerchantInfo" + "to grafana", exception);
        }

        LOGGER.info("Fetch Merchant info response returned for API: /v2/fetchMerchantInfo is: {}", response);
        return response;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/v1/fetchMerchantConfig", method = { RequestMethod.POST })
    @ApiOperation(value = "/fetchMerchantConfig", notes = "Fetch Merchant static config")
    public MerchantStaticConfigResponse fetchMerchantStaticConfig(
            @ApiParam(required = true) @RequestBody MerchantStaticConfigRequest merchantStaticConfigRequest)
            throws Exception {
        EXT_LOGGER.customInfo("Fetch merchant static config API called for request: {}", merchantStaticConfigRequest);
        EnvInfoUtil.setChannelDFromUserAgent(merchantStaticConfigRequest.getHead());
        IRequestProcessor<MerchantStaticConfigRequest, MerchantStaticConfigResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_MERCHANT_STATIC_CONFIG);
        MerchantStaticConfigResponse merchantStaticConfigResponse = requestProcessor
                .process(merchantStaticConfigRequest);
        try {
            Map<String, String> responseMap = new HashMap<>();
            responseMap
                    .put("RESPONSE_STATUS", merchantStaticConfigResponse.getBody().getResultInfo().getResultStatus());
            responseMap.put("RESPONSE_MESSAGE", merchantStaticConfigResponse.getBody().getResultInfo().getResultMsg());
            statsDUtils.pushResponse("v1/fetchMerchantConfig", responseMap);
        } catch (Exception exception) {
            LOGGER.error("Error in pushing response message " + "v1/fetchMerchantConfig" + "to grafana", exception);
        }
        EXT_LOGGER.customInfo("Fetch merchant static config API response: {}", merchantStaticConfigResponse);
        return merchantStaticConfigResponse;
    }
}