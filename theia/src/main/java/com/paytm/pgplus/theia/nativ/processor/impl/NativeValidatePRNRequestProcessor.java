package com.paytm.pgplus.theia.nativ.processor.impl;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.response.ResponseHeader;
import com.paytm.pgplus.theia.models.PRNValidationRequest;
import com.paytm.pgplus.theia.models.PRNValidationResponse;
import com.paytm.pgplus.theia.nativ.model.prn.NativeValidatePRNRequest;
import com.paytm.pgplus.theia.nativ.model.prn.NativeValidatePRNResponse;
import com.paytm.pgplus.theia.nativ.model.prn.NativeValidatePRNResponseBody;
import com.paytm.pgplus.theia.nativ.processor.AbstractRequestProcessor;
import com.paytm.pgplus.theia.nativ.utils.NativePaymentUtil;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.services.impl.PRNValidationHelper;
import com.paytm.pgplus.theia.utils.EventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;

@Service("nativeValidatePRNRequestProcessor")
public class NativeValidatePRNRequestProcessor
        extends
        AbstractRequestProcessor<NativeValidatePRNRequest, NativeValidatePRNResponse, PRNValidationRequest, PRNValidationResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeValidatePRNRequestProcessor.class);

    @Autowired
    private PRNValidationHelper prnValidationHelper;

    @Override
    protected PRNValidationRequest preProcess(NativeValidatePRNRequest request) {
        validatePRNValidationRequest(request);
        PRNValidationRequest prnValidationRequest = new PRNValidationRequest(request.getBody().getMid(), request
                .getBody().getOrderId(), request.getBody().getPrnCode());
        return prnValidationRequest;
    }

    @Override
    protected PRNValidationResponse onProcess(NativeValidatePRNRequest request,
            PRNValidationRequest prnValidationRequest) throws Exception {
        Optional<HttpServletRequest> httpServletRequest = getCurrentHttpRequest();
        PRNValidationResponse prnValidationResponse = prnValidationHelper.validatePRNWithPlatformPlus(
                httpServletRequest.get(), prnValidationRequest);
        return prnValidationResponse;
    }

    @Override
    protected NativeValidatePRNResponse postProcess(NativeValidatePRNRequest request,
            PRNValidationRequest prnValidationRequest, PRNValidationResponse prnValidationResponse) throws Exception {
        NativeValidatePRNResponseBody body = new NativeValidatePRNResponseBody(prnValidationResponse.getStatus(),
                prnValidationResponse.isRetryAllowed());
        if ("FAIL".equals(body.getStatus()))
            body.setResultInfo(NativePaymentUtil.resultInfo(ResultCode.INVALID_PRN));
        else if ("SUCCESS".equals(body.getStatus())) {
            body.setResultInfo(NativePaymentUtil.resultInfoForSuccess());
            EventUtils.pushTheiaEvents(EventNameEnum.PRN_VALIDATED);
        }
        return new NativeValidatePRNResponse(new ResponseHeader(), body);
    }

    private static Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional
                .ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(requestAttributes -> ServletRequestAttributes.class.isAssignableFrom(requestAttributes
                        .getClass())).map(requestAttributes -> ((ServletRequestAttributes) requestAttributes))
                .map(ServletRequestAttributes::getRequest);
    }

    private void validatePRNValidationRequest(NativeValidatePRNRequest request) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final Set<ConstraintViolation<NativeValidatePRNRequest>> validate = validator.validate(request);
        for (ConstraintViolation violation : validate) {
            LOGGER.error(violation.getMessage());
            throw RequestValidationException.getException();
        }
    }
}
