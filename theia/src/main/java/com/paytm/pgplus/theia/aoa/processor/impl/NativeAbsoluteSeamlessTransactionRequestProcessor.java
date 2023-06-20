package com.paytm.pgplus.theia.aoa.processor.impl;

import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.request.InitiateTransactionRequest;
import com.paytm.pgplus.response.InitiateTransactionResponse;
import com.paytm.pgplus.theia.aoa.helper.AOAPaymentHelper;
import com.paytm.pgplus.theia.aoa.helper.ValidationHelper;
import com.paytm.pgplus.theia.aoa.model.orderpay.CreateOrderAndPaymentRequest;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("nativeAbsoluteSeamlessTransactionRequestProcessor")
public class NativeAbsoluteSeamlessTransactionRequestProcessor
        extends
        AbstractRequestProcessor<CreateOrderAndPaymentRequest, NativeJsonResponse, CreateOrderAndPaymentRequest, NativeJsonResponse> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NativeAbsoluteSeamlessTransactionRequestProcessor.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private ValidationHelper validationHelper;

    @Autowired
    private AOAPaymentHelper paymentHelper;

    @Override
    protected CreateOrderAndPaymentRequest preProcess(CreateOrderAndPaymentRequest request) throws Exception {
        if (request.getBody().getOrderDetails() == null) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }

        validationHelper.validate(request);

        request.getBody().getOrderDetails().setRequestType(request.getBody().getRequestType());
        request.getBody().getOrderDetails().setMid(request.getBody().getMid());
        request.getBody().getOrderDetails().setOrderId(request.getBody().getOrderId());

        return request;
    }

    @Override
    protected NativeJsonResponse onProcess(CreateOrderAndPaymentRequest request,
            CreateOrderAndPaymentRequest createOrderAndPaymentRequest) throws Exception {
        NativeInitiateRequest nativeInitiateRequest = new NativeInitiateRequest();
        InitiateTransactionRequest initiateTransactionRequest = new InitiateTransactionRequest();
        initiateTransactionRequest.setHead(request.getHead());
        initiateTransactionRequest.setBody(request.getBody().getOrderDetails());
        LOGGER.info("InitiateTransaction Request: {}", initiateTransactionRequest);

        nativeInitiateRequest.setInitiateTxnReq(initiateTransactionRequest);
        IRequestProcessor<NativeInitiateRequest, InitiateTransactionResponse> requestProcessor = requestProcessorFactory
                .getRequestProcessor(RequestProcessorFactory.RequestType.INITIATE_TRANSACTION_REQUEST);
        // skipping order-creation in initiate
        nativeInitiateRequest.setOrderCreateInInitiate(false);
        InitiateTransactionResponse response = requestProcessor.process(nativeInitiateRequest);
        LOGGER.info("InitiateTransaction response returned for absolute seamless transaction flow is: {}", response);

        return processPaymentRequest(request, response);
    }

    @Override
    protected NativeJsonResponse postProcess(CreateOrderAndPaymentRequest request,
            CreateOrderAndPaymentRequest nativeInitiateRequest, NativeJsonResponse response) throws Exception {
        LOGGER.info("Response returned for processCreateOrderAndPay is: {}", response);
        return response;
    }

    private NativeJsonResponse processPaymentRequest(CreateOrderAndPaymentRequest request,
            InitiateTransactionResponse initiateTransactionResponse) throws Exception {
        String nativePaymentResponse;
        String txnToken;
        if (initiateTransactionResponse != null && initiateTransactionResponse.getBody() != null) {
            txnToken = initiateTransactionResponse.getBody().getTxnToken();
            nativePaymentResponse = paymentHelper.processNativeJsonRequest(request, txnToken, null);
        } else {
            throw new NativeFlowException.ExceptionBuilder(ResultCode.FAILED).isNativeJsonRequest(true).build();
        }

        NativeJsonResponse nativeJsonResponse = null;
        try {
            nativeJsonResponse = JsonMapper.mapJsonToObject(nativePaymentResponse, NativeJsonResponse.class);
        } catch (Exception e) {
            LOGGER.error("Exception while parsing txn info", e);
            throw e;
        }
        if (nativeJsonResponse != null) {
            Map<String, String> map = paymentHelper.getAdditionalInfoMap(nativeJsonResponse.getBody()
                    .getAdditionalInfo(), txnToken, null);
            nativeJsonResponse.getBody().setAdditionalInfo(map);
        }
        return nativeJsonResponse;
    }
}
