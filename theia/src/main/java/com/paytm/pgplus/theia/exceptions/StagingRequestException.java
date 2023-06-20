package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.utils.StagingParamValidatorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class StagingRequestException extends RuntimeException {
    private static final long serialVersionUID = 8872092576584634450L;
    private StagingParamValidatorResponse stagingParamValidatorResponse;

    public StagingRequestException() {
    }

    public StagingRequestException(String message) {
        super(message);
    }

    public StagingRequestException(String message, StagingParamValidatorResponse stagingParamValidatorResponse) {
        super(message);
        this.stagingParamValidatorResponse = stagingParamValidatorResponse;
    }

    public StagingParamValidatorResponse getStagingParamValidatorResponse() {
        return this.stagingParamValidatorResponse;
    }
}
