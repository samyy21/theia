package com.paytm.pgplus.biz.exception;

public class LooperServiceCheckedException extends Exception {

    private static final long serialVersionUID = 1L;

    public LooperServiceCheckedException() {
        super();
    }

    public LooperServiceCheckedException(String message) {
        super(message);
    }

    public LooperServiceCheckedException(Throwable cause) {
        super(cause);
    }

    public LooperServiceCheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}
