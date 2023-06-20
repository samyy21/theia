package com.paytm.pgplus.theia.aoa.processor.impl;

import com.paytm.pgplus.biz.utils.FF4JUtil;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.facade.utils.JsonMapper;
import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.response.SubscriptionTransactionResponse;
import com.paytm.pgplus.theia.aoa.helper.AOAPaymentHelper;
import com.paytm.pgplus.theia.aoa.helper.ValidationHelper;
import com.paytm.pgplus.theia.aoa.model.orderpay.CreateOrderAndPaymentRequest;
import com.paytm.pgplus.theia.models.NativeJsonResponse;
import com.paytm.pgplus.theia.nativ.exception.NativeFlowException;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.AOAUtils;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("nativeAbsoluteSubscriptionTransactionRequestProcessor")
public class NativeAbsoluteSubscriptionTransactionRequestProcessor
        extends
        AbstractRequestProcessor<CreateOrderAndPaymentRequest, NativeJsonResponse, CreateOrderAndPaymentRequest, NativeJsonResponse> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NativeAbsoluteSubscriptionTransactionRequestProcessor.class);

    @Autowired
    private RequestProcessorFactory requestProcessorFactory;

    @Autowired
    private ValidationHelper validationHelper;

    @Autowired
    private AOAPaymentHelper paymentHelper;

    @Autowired
    @Qualifier("aoaUtils")
    private AOAUtils aoaUtils;

    @Autowired
    private FF4JUtil ff4JUtil;

    @Override
    protected CreateOrderAndPaymentRequest preProcess(CreateOrderAndPaymentRequest request) throws Exception {
        if (request.getBody().getSubscriptionDetails() == null) {
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        validationHelper.validate(request);

        request.getBody().getSubscriptionDetails().setRequestType(request.getBody().getRequestType());
        request.getBody().getSubscriptionDetails().setMid(request.getBody().getMid());
        request.getBody().getSubscriptionDetails().setOrderId(request.getBody().getOrderId());

        EventUtils.pushTheiaEvents(
                EventNameEnum.ORDER_INITIATED,
                new ImmutablePair<>("REQUEST_TYPE", String
                        .valueOf(RequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION)));

        return request;
    }

    @Override
    protected NativeJsonResponse onProcess(CreateOrderAndPaymentRequest request,
            CreateOrderAndPaymentRequest createOrderAndPaymentRequest) throws Exception {
        // Create Subscription request
        SubscriptionTransactionRequest subscriptionTransactionRequest = new SubscriptionTransactionRequest();
        subscriptionTransactionRequest.setHead(request.getHead());
        subscriptionTransactionRequest.setBody(request.getBody().getSubscriptionDetails());
        subscriptionTransactionRequest.setSkipOrderCreateInSubs(true);
        subscriptionTransactionRequest.setSkipSubsContractValidation(true);
        LOGGER.info("CreateSubscription Request: {}", subscriptionTransactionRequest);

        IRequestProcessor<SubscriptionTransactionRequest, SubscriptionTransactionResponse> requestProcessor = null;

        if (aoaUtils.isAOAMerchant(request.getBody().getMid())) {
            requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_AOA_SUBSCRIPTION);
        } else {
            requestProcessor = requestProcessorFactory
                    .getRequestProcessor(RequestProcessorFactory.RequestType.NATIVE_SUBSCRIPTION);
        }

        SubscriptionTransactionResponse response = requestProcessor.process(subscriptionTransactionRequest);
        LOGGER.info("CreateSubscription response returned for absolute subs transaction flow is: {}", response);

        return processPaymentRequest(request, response);
    }

    @Override
    protected NativeJsonResponse postProcess(CreateOrderAndPaymentRequest request,
            CreateOrderAndPaymentRequest subscriptionTransactionRequest, NativeJsonResponse response) throws Exception {
        LOGGER.info("Response returned for processCreateOrderAndPay is: {}", response);
        return response;
    }

    private NativeJsonResponse processPaymentRequest(CreateOrderAndPaymentRequest request,
            SubscriptionTransactionResponse subscriptionTransactionResponse) throws Exception {
        String nativePaymentResponse;
        String txnToken;
        String subsId;
        if (subscriptionTransactionResponse != null && subscriptionTransactionResponse.getBody() != null) {
            txnToken = subscriptionTransactionResponse.getBody().getTxnToken();
            subsId = subscriptionTransactionResponse.getBody().getSubscriptionId();
            nativePaymentResponse = paymentHelper.processNativeJsonRequest(request, txnToken, subsId);
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
                    .getAdditionalInfo(), txnToken, subsId);
            nativeJsonResponse.getBody().setAdditionalInfo(map);
        }
        return nativeJsonResponse;
    }
}
