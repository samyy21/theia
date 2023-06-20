package com.paytm.pgplus.theia.accesstoken.controller;

import com.paytm.pgplus.stats.util.AWSStatsDUtils;
import com.paytm.pgplus.theia.accesstoken.annotation.AccessTokenControllerAdvice;
import com.paytm.pgplus.theia.accesstoken.model.request.CreateAccessTokenRequest;
import com.paytm.pgplus.theia.accesstoken.model.response.CreateAccessTokenResponse;
import com.paytm.pgplus.theia.accesstoken.processor.IRequestProcessor;
import com.paytm.pgplus.theia.accesstoken.processor.factory.ProcessorFactory;
import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@AccessTokenControllerAdvice
@RestController
@RequestMapping("api/v1")
public class AccessTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenController.class);

    @Autowired
    private ProcessorFactory processorFactory;

    @Autowired
    private NativePaymentUtil nativePaymentUtil;
    @Autowired
    private AWSStatsDUtils statsDUtils;

    @RequestMapping(value = "/token/create", method = { RequestMethod.POST })
    public CreateAccessTokenResponse createAccessToken(@RequestBody CreateAccessTokenRequest request) throws Exception {

        long startTime = System.currentTimeMillis();

        CreateAccessTokenResponse response = null;
        try {
            AccessTokenUtils.setRequestHeader(request.getHead());
            LOGGER.info("Request for API: /token/create is: {}", request);
            try {
                Map<String, String> metadata = new HashMap<String, String>();
                if (request != null && request.getBody() != null) {
                    metadata.put(TheiaConstant.EventLogConstants.MID, request.getBody().getMid());
                }
                nativePaymentUtil.logNativeRequests((request.getHead() == null ? null : request.getHead().toString()),
                        metadata);
            } catch (Exception ex) {

            }

            IRequestProcessor<CreateAccessTokenRequest, CreateAccessTokenResponse> requestProcessor = processorFactory
                    .getRequestProcessor(ProcessorFactory.RequestType.CREATE_ACCESS_TOKEN_REQUEST);

            response = requestProcessor.process(request);
            try {
                nativePaymentUtil.logNativeResponse(response == null ? null : nativePaymentUtil.getResultInfo(response
                        .getBody()));
            } catch (Exception ex) {

            }
            try {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("RESPONSE_STATUS", response.getBody().getResultInfo().getResultStatus());
                responseMap.put("RESPONSE_MESSAGE", response.getBody().getResultInfo().getResultMsg());
                statsDUtils.pushResponse("token/create", responseMap);
            } catch (Exception exception) {
                LOGGER.error("Error in pushing response message " + "token/create" + "to grafana", exception);
            }

            LOGGER.info("Response for API: /token/create is: {}", response);
            return response;
        } finally {
            LOGGER.info("Total time taken for createAccessToken is {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
