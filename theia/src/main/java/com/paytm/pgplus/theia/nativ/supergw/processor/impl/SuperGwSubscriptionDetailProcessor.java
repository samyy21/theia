package com.paytm.pgplus.theia.nativ.supergw.processor.impl;

import com.paytm.pgplus.facade.constants.FacadeConstants;
import com.paytm.pgplus.request.SubscriptionTransactionRequest;
import com.paytm.pgplus.response.SubscriptionTransactionResponse;
import com.paytm.pgplus.theia.exceptions.InvalidRequestParameterException;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.impl.NativeSubscriptionTransactionRequestProcessor;
import com.paytm.pgplus.theia.nativ.supergw.util.SuperGwValidationUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Service("superGwSubscriptionDetailProcessor")
public class SuperGwSubscriptionDetailProcessor
        extends
        AbstractRequestProcessor<SubscriptionTransactionRequest, SubscriptionTransactionResponse, SubscriptionTransactionRequest, SubscriptionTransactionResponse> {

    @Autowired
    private NativeSubscriptionTransactionRequestProcessor nativeSubscriptionTransactionRequestProcessor;

    @Autowired
    private Environment environment;

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperGwSubscriptionDetailProcessor.class);

    @Override
    protected SubscriptionTransactionRequest preProcess(SubscriptionTransactionRequest request) {
        validateJwt(request);
        validateRequest(request);
        request.setSuperGwHit(true);
        return request;
    }

    @Override
    protected SubscriptionTransactionResponse onProcess(SubscriptionTransactionRequest request,
            SubscriptionTransactionRequest serviceReq) throws Exception {

        SubscriptionTransactionResponse subscriptionTransactionResponse = null;
        subscriptionTransactionResponse = nativeSubscriptionTransactionRequestProcessor.process(request);
        return subscriptionTransactionResponse;
    }

    @Override
    protected SubscriptionTransactionResponse postProcess(SubscriptionTransactionRequest request,
            SubscriptionTransactionRequest serviceReq, SubscriptionTransactionResponse serviceRes) throws Exception {

        return serviceRes;
    }

    public void validateJwt(SubscriptionTransactionRequest request) {
        String clientId = request.getHead().getClientId();
        if (StringUtils.isBlank(clientId)) {
            throw new InvalidRequestParameterException("clientId can't be null");
        }
        String clientSecret = environment.getProperty(clientId);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put(FacadeConstants.FREQUENCY_UNIT, request.getBody().getSubscriptionFrequencyUnit());
        jwtClaims.put(FacadeConstants.MID, request.getBody().getMid());
        jwtClaims.put(FacadeConstants.ORDER_ID, request.getBody().getOrderId());
        jwtClaims.put(FacadeConstants.AMOUNT_TYPE, request.getBody().getSubscriptionAmountType());
        jwtClaims.put(FacadeConstants.CUST_ID, request.getBody().getUserInfo().getCustId());
        jwtClaims.put(FacadeConstants.SUBS_EXPIRY_DATE, request.getBody().getSubscriptionExpiryDate());
        SuperGwValidationUtil.validateJwtToken(jwtClaims, clientId, clientSecret);
    }

    private void validateRequest(SubscriptionTransactionRequest request) {

        if (isEmpty(request.getBody().getSubscriptionFrequencyUnit()) || isEmpty(request.getBody().getMid())
                || isEmpty(request.getBody().getOrderId()) || null == request.getBody().getSubscriptionAmountType()
                || (request.getBody().getUserInfo() == null) || isEmpty(request.getBody().getUserInfo().getCustId())
                || isEmpty(request.getBody().getSubscriptionExpiryDate())) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);

        }

    }

}