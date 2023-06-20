package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccountNumberMismatchException extends RuntimeException {

    private static final long serialVersionUID = -3426321756160352250L;

    private PaymentRequestBean paymentRequestBean;

    private ResponseConstants responseConstant;

    private ResultInfo resultInfo;

    public AccountNumberMismatchException() {
    }

    public AccountNumberMismatchException(String message) {
        super(message);
    }

    public AccountNumberMismatchException(String message, PaymentRequestBean paymentRequestBean) {
        super(message);
        this.paymentRequestBean = paymentRequestBean;
    }

    public AccountNumberMismatchException(String message, PaymentRequestBean paymentRequestBean,
            ResponseConstants responseConstant) {
        super(message);
        this.paymentRequestBean = paymentRequestBean;
        this.responseConstant = responseConstant;
        // this is to make result info wrt responseConstant for
        // enhanceErrorResponse
        this.resultInfo = OfflinePaymentUtils.resultInfo(null);
        if (responseConstant != null) {
            this.resultInfo = new ResultInfo(TheiaConstant.ExtraConstants.FAILURE, responseConstant.getCode(),
                    responseConstant.name(), responseConstant.getMessage(), true);
        }
    }

    public PaymentRequestBean getpaymentRequestBean() {
        return this.paymentRequestBean;
    }

    public ResponseConstants getResponseConstant() {
        return this.responseConstant;
    }

    public ResultInfo getResultInfo() {
        return this.resultInfo;
    }
}
