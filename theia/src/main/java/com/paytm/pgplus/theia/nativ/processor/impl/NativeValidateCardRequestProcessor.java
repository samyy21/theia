package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.common.signature.JWTWithHmacSHA256;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseCodeConstants;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.emiSubvention.helper.ChecksumValidator;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardRequest;
import com.paytm.pgplus.theia.nativ.model.validateCard.ValidateCardResponse;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.service.ICardValidationService;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.BinDetailException;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import com.paytm.pgplus.theia.utils.MerchantExtendInfoUtils;
import jdk.nashorn.internal.parser.Token;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Service("nativeValidateCardRequestProcessor")
public class NativeValidateCardRequestProcessor extends
        AbstractRequestProcessor<ValidateCardRequest, ValidateCardResponse, ValidateCardRequest, ValidateCardResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeValidateCardRequestProcessor.class);

    @Autowired
    private ChecksumValidator checksumValidator;

    @Autowired
    private ICardValidationService cardValidationService;

    @Autowired
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected ValidateCardRequest preProcess(ValidateCardRequest request) {

        validate(request);
        request.getHead().setTxnToken(request.getHead().getToken());
        request.getHead().setTokenType(TokenType.GUEST);
        return request;
    }

    @Override
    protected ValidateCardResponse onProcess(ValidateCardRequest request, ValidateCardRequest servicerequest)
            throws Exception {
        return cardValidationService.validateCardandFetchDetails(request);
    }

    @Override
    protected ValidateCardResponse postProcess(ValidateCardRequest request, ValidateCardRequest servicerequest,
            ValidateCardResponse cardValidationResponse) throws Exception {
        cardValidationResponse.getBody().setResultInfo(
                new com.paytm.pgplus.response.ResultInfo(ResultCode.SUCCESS.getResultStatus(), ResultCode.SUCCESS
                        .getResultCodeId(), ResultCode.SUCCESS.getResultMsg()));
        return cardValidationResponse;
    }

    private void validate(ValidateCardRequest request) {

        if (TokenType.CHECKSUM == request.getHead().getTokenType()) {
            boolean isValidated = checksumValidator.validateChecksum(checksumValidator.getBodyString(), request
                    .getBody().getMid(), request.getHead().getToken());
            if (!isValidated) {
                throw new RequestValidationException(
                        new ResultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                                ResponseMessage.INVALID_CHECKSUM));
            }
        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseCodeConstants.VALIDATION_FAILED,
                    ResponseMessage.TOKEN_TYPE_NOT_SUPPORTED));
        }
        if (null == request.getBody() || null == request.getBody().getBin() || request.getBody().getBin().length() < 6) {
            LOGGER.info("validation failed as invalid BIN  is Passed");
            throw BinDetailException.getException(ResultCode.BIN_NUMBER_EXCEPTION);
        }

        if (StringUtils.isBlank(request.getBody().getMid()) || request.getBody().getMid() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_MID));
        }

        String merchantKey = merchantExtendInfoUtils.getMerchantKey(request.getBody().getMid());
        if (isEmpty(merchantKey)) {
            throw RequestValidationException.getException(ResultCode.INVALID_MID);
        }

    }

}
