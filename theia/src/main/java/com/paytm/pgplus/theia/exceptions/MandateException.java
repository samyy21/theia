package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.common.model.ResultInfo;
import com.paytm.pgplus.payloadvault.subscription.response.ProcessedMandateResponse;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.models.ProcessedBmResponse;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;
import com.paytm.pgplus.theia.offline.utils.OfflinePaymentUtils;

public class MandateException extends BaseException {

    private static final long serialVersionUID = -3958706374492936045L;

    private String callBackUrl;

    private ProcessedBmResponse mandateResponse;

    private boolean isHtml;

    private PaymentRequestBean requestBean;

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public ProcessedBmResponse getMandateResponse() {
        return mandateResponse;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public PaymentRequestBean getRequestBean() {
        return requestBean;
    }

    private MandateException(ExceptionBuilder builder) {
        super(builder.resultInfo);
        this.callBackUrl = builder.callBackUrl;
        this.mandateResponse = builder.processedMandateResponse;
        this.isHtml = builder.isHtml;
        this.requestBean = builder.requestBean;
    }

    public static class ExceptionBuilder {
        private String callBackUrl;
        private ResultInfo resultInfo;
        private ProcessedBmResponse processedMandateResponse;
        private boolean isHtml;
        private PaymentRequestBean requestBean;

        public ExceptionBuilder(String callBackUrl, ResultInfo resultInfo, boolean isHtml) {
            this.callBackUrl = callBackUrl;
            this.resultInfo = resultInfo;
            this.isHtml = isHtml;
        }

        public ExceptionBuilder(String callBackUrl, String resultStatus, String resultCode, String resultMessage,
                boolean isHtml) {
            this.callBackUrl = callBackUrl;
            this.resultInfo = new ResultInfo(resultStatus, resultCode, resultMessage);
            this.isHtml = isHtml;
        }

        public ExceptionBuilder(String callBackUrl, ResponseConstants responseConstant, boolean isHtml) {
            this.callBackUrl = callBackUrl;
            this.isHtml = isHtml;
            this.resultInfo = OfflinePaymentUtils.resultInfo(null);
            if (null != responseConstant) {
                this.resultInfo.setResultCodeId(responseConstant.getCode());
                this.resultInfo.setResultMsg(responseConstant.getMessage());
                this.resultInfo.setResultCode(responseConstant.getCode());
            }
        }

        public ExceptionBuilder setProsessedResponse(ProcessedBmResponse prosessedResponse) {
            this.processedMandateResponse = prosessedResponse;
            return this;
        }

        public ExceptionBuilder(String callBackUrl,
                com.paytm.pgplus.payloadvault.subscription.response.ResultInfo resultInfo, boolean isHtml) {
            this.callBackUrl = callBackUrl;
            this.resultInfo = new ResultInfo(resultInfo.getStatus(), resultInfo.getCode(), resultInfo.getMessage());
            this.isHtml = isHtml;
        }

        public ExceptionBuilder setRequestBean(PaymentRequestBean requestBean) {
            this.requestBean = requestBean;
            return this;
        }

        public MandateException build() {
            return new MandateException(this);
        }
    }
}
