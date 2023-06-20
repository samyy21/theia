package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.biz.core.model.oauth.UserDetailsBiz;
import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.facade.emisubvention.models.GenericEmiSubventionResponse;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.emisubvention.models.response.ValidateResponse;
import com.paytm.pgplus.facade.emisubvention.service.ISubventionEmiService;
import com.paytm.pgplus.request.InitiateTransactionRequestBody;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseCodeConstants;
import com.paytm.pgplus.theia.emiSubvention.constants.ResponseMessage;
import com.paytm.pgplus.theia.emiSubvention.helper.ChecksumValidator;
import com.paytm.pgplus.theia.emiSubvention.helper.SubventionEmiServiceHelper;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequest;
import com.paytm.pgplus.theia.emiSubvention.model.response.validate.ValidateEmiResponse;
import com.paytm.pgplus.theia.emiSubvention.util.EmiSubventionUtils;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.INativeValidationService;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.paymentoffer.helper.TokenValidationHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.REQUEST_ID;

@Service("validateEmiSubventionProcessor")
public class ValidateEmiSubventionProcessor
        extends
        AbstractRequestProcessor<ValidateEmiRequest, ValidateEmiResponse, ValidateRequest, GenericEmiSubventionResponse<ValidateResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateEmiSubventionProcessor.class);

    @Autowired
    private TokenValidationHelper tokenValidationHelper;
    @Autowired
    private ISubventionEmiService subventionEmiService;

    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;

    @Autowired
    private ChecksumValidator checksumValidator;

    @Autowired
    private EmiSubventionUtils emiSubventionUtils;

    @Autowired
    @Qualifier("nativeValidationService")
    private INativeValidationService nativeValidationService;

    @Override
    protected ValidateRequest preProcess(ValidateEmiRequest request) {

        validateRequest(request);
        // basic validations to be done
        if (TokenType.SSO == request.getHead().getTokenType()) {
            UserDetailsBiz userDetailsBiz = tokenValidationHelper.validateToken(request.getHead().getToken(), request
                    .getHead().getTokenType(), request.getBody(), request.getBody().getMid());

            if (StringUtils.isBlank(request.getBody().getCustomerId())) {
                request.getBody().setCustomerId(userDetailsBiz.getUserId());
            }
        } else if (TokenType.CHECKSUM == request.getHead().getTokenType()) {
            boolean isValidated = checksumValidator.validateChecksum(checksumValidator.getBodyString(), request
                    .getBody().getMid(), request.getHead().getToken());
            if (!isValidated) {
                throw new RequestValidationException(
                        new ResultInfo(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultCodeId(),
                                ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg(),
                                ResponseMessage.INVALID_CHECKSUM));
            }
        } else if (TokenType.ACCESS.equals(request.getHead().getTokenType())) {
            emiSubventionUtils.validateAccessToken(request.getBody().getMid(), request.getBody().getReferenceId(),
                    request.getHead().getToken());
        } else if (TokenType.TXN_TOKEN.equals(request.getHead().getTokenType()) && null != request.getHead().getToken()) {
            InitiateTransactionRequestBody orderDetail = nativeValidationService.validateTxnToken(request.getHead()
                    .getToken());
            nativeValidationService.validateMidOrderId(orderDetail.getMid(), orderDetail.getOrderId());
        } else {
            LOGGER.info("tokenType = {} is not supported", request.getHead().getTokenType());
            throw new RequestValidationException(new ResultInfo(
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultStatus(),
                    ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getCode(), ResponseCodeConstants.VALIDATION_FAILED,
                    ResponseMessage.TOKEN_TYPE_NOT_SUPPORTED));
        }
        MDC.put(REQUEST_ID, request.getHead().getRequestId());
        String tokenType = String.valueOf(request.getHead().getTokenType());

        ValidateRequest serviceRequest = subventionEmiServiceHelper.prepareEmiServiceRequest(request.getBody(), request
                .getHead().getToken(), tokenType);
        return serviceRequest;
    }

    @Override
    protected GenericEmiSubventionResponse<ValidateResponse> onProcess(ValidateEmiRequest request,
            ValidateRequest serviceRequest) throws Exception {
        return subventionEmiService.validateSubventionEmi(serviceRequest);
    }

    @Override
    protected ValidateEmiResponse postProcess(ValidateEmiRequest request, ValidateRequest serviceRequest,
            GenericEmiSubventionResponse<ValidateResponse> serviceResponse) throws Exception {
        ValidateEmiResponse validateEmiResponse = subventionEmiServiceHelper.prepareValidateEmiResponse(
                serviceResponse, request, serviceRequest);
        validateEmiResponse.getBody().setResultInfo(
                new com.paytm.pgplus.response.ResultInfo(ResultCode.SUCCESS.getResultStatus(), ResultCode.SUCCESS
                        .getResultCodeId(), ResultCode.SUCCESS.getResultMsg()));
        return validateEmiResponse;
    }

    private void validateRequest(ValidateEmiRequest request) {
        validateRequestHead(request);
        subventionEmiServiceHelper.validateRequestBody(request.getBody());
    }

    private void validateRequestHead(ValidateEmiRequest request) {
        if (StringUtils.isBlank(request.getBody().getCustomerId())
                && !(TokenType.SSO == request.getHead().getTokenType())) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_CUST_ID));
        }
        if (request.getHead().getRequestId() == null) {
            throw new RequestValidationException(new ResultInfo(ResultCode.MISSING_MANDATORY_ELEMENT.getResultStatus(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultCodeId(),
                    ResultCode.MISSING_MANDATORY_ELEMENT.getResultMsg(), ResponseMessage.EMPTY_REQUEST_ID));
        }
    }

}
