package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseCodeConstants;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.emiSubvention.helper.ChecksumValidator;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationResponse;
import com.paytm.pgplus.theia.nativ.model.validateCard.CardNumberValidationResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.ICardNumberValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.MidDoesnotMatchException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

//import com.paytm.pgplus.response.ResultInfo;

@Service("nativeCardNumberValidationRequestProcessor")
public class NativeCardNumberValidationRequestProcessor
        extends
        AbstractRequestProcessor<CardNumberValidationRequest, CardNumberValidationResponse, CardNumberValidationRequest, CardNumberValidationResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeCardNumberValidationRequestProcessor.class);

    @Autowired
    private ChecksumValidator checksumValidator;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("cardNumberValidationService")
    private ICardNumberValidationService cardNumberValidationService;

    @Override
    protected CardNumberValidationRequest preProcess(CardNumberValidationRequest request) {
        validateRequestCheck(request);
        return request;
    }

    @Override
    protected CardNumberValidationResponse onProcess(CardNumberValidationRequest request,
            CardNumberValidationRequest serviceRequest) {
        CardNumberValidationResponse cardNumberValidationResponse = null;
        try {
            cardNumberValidationResponse = cardNumberValidationService.fetchCardNumberValidationDetail(request);
        } catch (Exception e) {
            LOGGER.error("Exception Occurred in card validation API {}", e);
            return getCardNumberException();
        }
        return cardNumberValidationResponse;
    }

    @Override
    protected CardNumberValidationResponse postProcess(CardNumberValidationRequest request,
            CardNumberValidationRequest serviceRequest, CardNumberValidationResponse cardNumberValidationResponse)
            throws Exception {

        if (cardNumberValidationResponse != null && cardNumberValidationResponse.getBody() != null) {
            if (cardNumberValidationResponse.getBody().getResultInfo() == null)
                cardNumberValidationResponse.getBody().setResultInfo(
                        new com.paytm.pgplus.response.ResultInfo(ResultCode.SUCCESS.getResultStatus(),
                                ResultCode.SUCCESS.getResultCodeId(), ResultCode.SUCCESS.getResultMsg()));
        } else {
            LOGGER.error("Exception Occurred while fetching the response of card validation API is null");
            return getCardNumberException();
        }
        return cardNumberValidationResponse;
    }

    private void validateRequestCheck(CardNumberValidationRequest request) {
        if (request == null || request.getBody() == null || request.getHead() == null) {
            LOGGER.info("request body is null or body is null or header is null Exception occurred for cardValidation API");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }

        if (request.getHead().getTokenType() == null || StringUtils.isBlank(request.getHead().getTokenType().getType())
                || !TokenType.CHECKSUM.getType().equalsIgnoreCase(request.getHead().getTokenType().getType())) {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseCodeConstants.VALIDATION_FAILED,
                    ResponseMessage.TOKEN_TYPE_NOT_SUPPORTED));
        }
        if (StringUtils.isBlank(request.getBody().getMid())) {
            LOGGER.info("empty mid Exception occurred for cardValidation API");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
        requestValidation(request);
        validateMidRequestId(request.getBody().getMid());

    }

    public void validateMidRequestId(String mid) {
        String requestMid = MDC.get(TheiaConstant.RequestParams.MID);
        if (!mid.equals(requestMid)) {
            throw MidDoesnotMatchException.getException();
        }
    }

    private void requestValidation(CardNumberValidationRequest request) {
        if (StringUtils.isBlank(request.getBody().getCardNumber())
                || StringUtils.isBlank(request.getBody().getExpireDate())
                || StringUtils.isBlank(request.getBody().getRequestId())
                || validateLengthAndNumericCardAndExpiry(request.getBody().getCardNumber(), request.getBody()
                        .getExpireDate(), request.getBody().getRequestId())) {
            LOGGER.error("length of card and Expiry date is invalid Exception Occurred for cardValidation API");
            throw RequestValidationException.getException(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION);
        }
    }

    private boolean validateLengthAndNumericCardAndExpiry(String card, String expiry, String requestId) {
        if (card.length() < 12 || !StringUtils.isNumeric(card) || card.length() > 20 || expiry.length() != 6
                || !StringUtils.isNumeric(expiry) || requestId.length() > 50) {
            return true;
        }
        return false;
    }

    private CardNumberValidationResponse getCardNumberException() {
        CardNumberValidationResponse cardNumberValidationResponse;
        cardNumberValidationResponse = new CardNumberValidationResponse();
        cardNumberValidationResponse.setHead(new ResponseHeader());
        CardNumberValidationResponseBody exceptionBody = new CardNumberValidationResponseBody();
        exceptionBody.setPerformanceStatus("U");
        exceptionBody.setResultInfo(new com.paytm.pgplus.response.ResultInfo(
                com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getResultStatus(),
                com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getResultCodeId(),
                com.paytm.pgplus.common.enums.ResultCode.SYSTEM_ERROR.getResultMsg()));
        cardNumberValidationResponse.setBody(exceptionBody);
        return cardNumberValidationResponse;
    }
}
