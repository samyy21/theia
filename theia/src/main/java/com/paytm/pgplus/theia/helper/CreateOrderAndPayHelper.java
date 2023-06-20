package com.paytm.pgplus.theia.helper;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.theia.aoa.enums.AOARequestType;
import com.paytm.pgplus.theia.aoa.model.orderpay.CreateOrderAndPaymentRequest;
import com.paytm.pgplus.theia.aoa.processor.factory.AoARequestProcessorFactory;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
public class CreateOrderAndPayHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrderAndPayHelper.class);

    @Autowired
    private AoARequestProcessorFactory requestProcessorFactory;

    public NativeJsonResponse processOrderAndPay(HttpServletRequest httpServletRequest) {
        LOGGER.info("Processing CreateOrderAndPayment Request");
        CreateOrderAndPaymentRequest request = null;
        try {
            String requestData = IOUtils.toString(httpServletRequest.getInputStream(), Charsets.UTF_8.name());
            request = JsonMapper.mapJsonToObject(requestData, CreateOrderAndPaymentRequest.class);
        } catch (IOException | FacadeCheckedException e) {
            LOGGER.error("Error while parsing request {}", e);
        }

        NativeJsonResponse nativeJsonResponse = null;
        if (request != null && request.getBody() != null && request.getBody().getRequestType() != null) {
            String requestType = request.getBody().getRequestType();
            if (!AOARequestType.isValidRequestType(requestType)) {
                throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
            }
            request.getBody().setRequest(httpServletRequest);
            LOGGER.info("CreateOrderAndPayment request received: {}", request);
            try {
                IRequestProcessor<CreateOrderAndPaymentRequest, NativeJsonResponse> requestProcessor = requestProcessorFactory
                        .getRequestProcessor(AoARequestProcessorFactory.RequestType.valueOf(requestType));
                nativeJsonResponse = requestProcessor.process(request);
            } catch (NativeFlowException nfe) {
                LOGGER.error("Error while processing payment {}", nfe);
                throw nfe;
            } catch (RequestValidationException e) {
                LOGGER.error("Error while processing payment {}", e);
                throw e;
            } catch (Exception e) {
                LOGGER.error("Error while processing payment {}", e);
                throw RequestValidationException.getException(ResultCode.FAILED);
            }
        } else {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        LOGGER.info("Final Response sent in CreateOrderAndPayment : {}", nativeJsonResponse);
        return nativeJsonResponse;
    }
}
