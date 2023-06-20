package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.facade.coft.model.FetchTokenDetailRequest;
import com.paytm.pgplus.facade.coft.model.FetchTokenDetailRequestBody;
import com.paytm.pgplus.facade.coft.model.FetchTokenDetailResponse;
import com.paytm.pgplus.facade.coft.model.GenerateTokenDataRequestHead;
import com.paytm.pgplus.facade.coft.service.ICoftService;
import com.paytm.pgplus.theia.accesstoken.enums.ResultCode;
import com.paytm.pgplus.theia.accesstoken.exception.RequestValidationException;
import com.paytm.pgplus.theia.nativ.model.cardtoken.CardTokenDetailResponse;
import com.paytm.pgplus.theia.nativ.model.cardtoken.CardTokenDetailResponseBody;
import com.paytm.pgplus.theia.nativ.model.cardtoken.FetchCardTokenDetailRequest;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("fetchCardTokenDetailRequestProcessor")
public class FetchCardTokenDetailRequestProcessor
        extends
        AbstractRequestProcessor<FetchCardTokenDetailRequest, CardTokenDetailResponse, FetchTokenDetailRequest, FetchTokenDetailResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchCardTokenDetailRequestProcessor.class);

    @Autowired
    @Qualifier("CoftService")
    private ICoftService coftService;

    @Autowired
    private TokenValidationHelper tokenValidationHelper;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected FetchTokenDetailRequest preProcess(FetchCardTokenDetailRequest request) throws Exception {
        validateRequest(request);
        switch (request.getHead().getTokenType()) {
        case SSO:
            tokenValidationHelper.validateSSOToken(request.getHead().getToken(), null);
            break;
        case TXN_TOKEN:
            nativeValidationService.validateTxnToken(request.getHead().getTxnToken());
            break;
        default:
            throw RequestValidationException.getException(ResultCode.INVALID_TOKEN_TYPE);
        }
        return transformRequest(request);
    }

    @Override
    protected FetchTokenDetailResponse onProcess(FetchCardTokenDetailRequest request,
            FetchTokenDetailRequest fetchTokenDetailRequest) throws Exception {
        return coftService.fetchTokenDetail(fetchTokenDetailRequest);
    }

    @Override
    protected CardTokenDetailResponse postProcess(FetchCardTokenDetailRequest request,
            FetchTokenDetailRequest fetchTokenDetailRequest, FetchTokenDetailResponse fetchTokenDetailResponse)
            throws Exception {
        CardTokenDetailResponse cardTokenDetailResponse = new CardTokenDetailResponse();
        cardTokenDetailResponse.setHead(fetchTokenDetailResponse.getHead());
        CardTokenDetailResponseBody cardTokenDetailResponseBody = new CardTokenDetailResponseBody();
        cardTokenDetailResponseBody.setResultInfo(fetchTokenDetailResponse.getBody().getResultInfo());
        cardTokenDetailResponseBody.setTokenInfo(fetchTokenDetailResponse.getBody().getTokenInfo());
        cardTokenDetailResponse.setBody(cardTokenDetailResponseBody);
        return cardTokenDetailResponse;
    }

    private void validateRequest(FetchCardTokenDetailRequest request) {
        if (request == null || request.getHead() == null || request.getBody() == null) {
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
        validateToken(request.getHead().getTokenType(), request.getHead().getToken());
        if (StringUtils.isBlank(request.getBody().getTokenIndexNumber())
                && StringUtils.isBlank(request.getBody().getOrderId())) {
            LOGGER.error("received both orderID and tokenIndexNumber empty");
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
    }

    private void validateToken(TokenType tokenType, String token) {
        if (Objects.isNull(tokenType) || StringUtils.isBlank(token)) {
            LOGGER.error("tokenType or token is empty");
            throw RequestValidationException.getException(ResultCode.MISSING_MANDATORY_ELEMENT);
        }
    }

    private FetchTokenDetailRequest transformRequest(FetchCardTokenDetailRequest request) {
        FetchTokenDetailRequest fetchTokenDetailRequest = new FetchTokenDetailRequest();
        fetchTokenDetailRequest.setHead(new GenerateTokenDataRequestHead());
        FetchTokenDetailRequestBody tokenDetailRequestBody = new FetchTokenDetailRequestBody();
        tokenDetailRequestBody.setMid("PAYTM197");
        tokenDetailRequestBody.setRequestId(request.getBody().getOrderId());
        tokenDetailRequestBody.setTokenIndexNumber(request.getBody().getTokenIndexNumber());
        fetchTokenDetailRequest.setBody(tokenDetailRequestBody);
        return fetchTokenDetailRequest;
    }
}
