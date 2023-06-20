package com.paytm.pgplus.theia.nativ.controller;

import com.paytm.pgplus.theia.nativ.annotations.NativeControllerAdvice;
import com.paytm.pgplus.theia.nativ.model.sendNotification.SendNotificationRequest;
import com.paytm.pgplus.theia.nativ.model.sendNotification.SendNotificationResponse;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.exceptions.SessionExpiredException;
import com.paytm.pgplus.theia.utils.ProcessTransactionUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
@NativeControllerAdvice
public class NativeSendNotificationAppInvokeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeSendNotificationAppInvokeController.class);

    @Autowired
    private NativePaymentUtil nativePaymentUtil;

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    ProcessTransactionUtil processTransactionUtil;

    @RequestMapping(value = "/sendNotification", method = { RequestMethod.POST })
    public SendNotificationResponse sendNotification(
            @ApiParam(required = true) @RequestBody SendNotificationRequest request) throws Exception {

        SendNotificationResponse response = null;
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Native request received for API: /sendNotification is: {}", request);
            nativePaymentUtil.logNativeRequests(request.getHead().toString());
            IRequestProcessor<SendNotificationRequest, SendNotificationResponse> requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.SEND_NOTIFICATION_APP_INVOKE);
            response = requestProcessor.process(request);
            if (response != null && response.getBody() != null && response.getBody().getResultInfo() != null) {
                processTransactionUtil.pushNativePaymentEvent(request.getBody().getMid(), request.getBody()
                        .getOrderId(), "SEND_NOTIFICATION_RESPONSE_".concat(response.getBody().getResultInfo()
                        .getResultStatus()));
            }
        } catch (RequestValidationException e) {
            LOGGER.error("Invalid Request {} ", e);
            throw new RequestValidationException(e.getResultInfo());
        } catch (SessionExpiredException e) {
            LOGGER.error("Session Expired {} ", e);
            throw new SessionExpiredException(e.getResultInfo());
        } catch (BaseException e) {
            throw new BaseException(e.getResultInfo());
        } finally {
            LOGGER.info("Total time taken for NativeSendNotificationAppInvokeController is {} ms",
                    System.currentTimeMillis() - startTime);
            LOGGER.info("Native response sent for API: /sendNotification is: {}", response);
            nativePaymentUtil.logNativeResponse(startTime);
        }
        return response;
    }
}