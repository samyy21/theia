package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.theia.offline.exceptions.BaseException;

public class EdcMerchantLimitBreachException extends BaseException {
    private static final long serialVersionUID = -7070804419809072048L;
    private String message;

    public EdcMerchantLimitBreachException(ExceptionBuilder exceptionBuilder) {
        this.message = exceptionBuilder.message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static class ExceptionBuilder {

        private String message;

        public ExceptionBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public EdcMerchantLimitBreachException build() {
            return new EdcMerchantLimitBreachException(this);
        }
    }
}
