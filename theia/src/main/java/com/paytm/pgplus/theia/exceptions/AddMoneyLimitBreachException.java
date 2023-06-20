package com.paytm.pgplus.theia.exceptions;

import com.paytm.pgplus.theia.offline.exceptions.BaseException;

public class AddMoneyLimitBreachException extends BaseException {
    private static final long serialVersionUID = -8873396865410216764L;
    private String message;

    public AddMoneyLimitBreachException(AddMoneyLimitBreachException.ExceptionBuilder exceptionBuilder) {
        this.message = exceptionBuilder.message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static class ExceptionBuilder {

        private String message;

        public AddMoneyLimitBreachException.ExceptionBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public AddMoneyLimitBreachException build() {
            return new AddMoneyLimitBreachException(this);
        }
    }
}
