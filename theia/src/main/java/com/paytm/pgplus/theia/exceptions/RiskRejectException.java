package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.offline.exceptions.BaseException;

public class RiskRejectException extends BaseException {

    private static final long serialVersionUID = -1311952557961444537L;

    private ResponseConstants responseConstant;

    private String customCallbackMsg;

    private boolean isRetryAllowed;

    private boolean isRedirectEnhanceFlow;

    public ResponseConstants getResponseConstant() {
        return responseConstant;
    }

    public String getCustomCallbackMsg() {
        return customCallbackMsg;
    }

    public boolean isRetryAllowed() {
        return isRetryAllowed;
    }

    public boolean isRedirectEnhanceFlow() {
        return isRedirectEnhanceFlow;
    }

    public RiskRejectException(ExceptionBuilder builder) {
        this.responseConstant = builder.responseConstant;
        this.customCallbackMsg = builder.customCallbackMsg;
        this.isRetryAllowed = builder.isRetryAllowed;
        this.isRedirectEnhanceFlow = builder.isRedirectEnhanceFlow;
    }

    public static class ExceptionBuilder {
        private ResponseConstants responseConstant;
        private String customCallbackMsg;
        private boolean isRetryAllowed;
        private boolean isRedirectEnhanceFlow;

        public ExceptionBuilder setResponseConstant(ResponseConstants responseConstant) {
            this.responseConstant = responseConstant;
            return this;
        }

        public ExceptionBuilder setCustomCallbackMsg(String customCallbackMsg) {
            this.customCallbackMsg = customCallbackMsg;
            return this;
        }

        public ExceptionBuilder setRetryAllowed(boolean isRetryAllowed) {
            this.isRetryAllowed = isRetryAllowed;
            return this;
        }

        public ExceptionBuilder setRedirectEnhanceFlow(boolean isRedirectEnhanceFlow) {
            this.isRedirectEnhanceFlow = isRedirectEnhanceFlow;
            return this;
        }

        public RiskRejectException build() {
            return new RiskRejectException(this);
        }

    }

}
