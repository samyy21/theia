package com.paytm.pgplus.cashier.exception;

public class RiskVerificationRequiredException extends RuntimeException {

    private static final long serialVersionUID = 3142599088162651806L;

    private String securityId;

    private String method;

    public RiskVerificationRequiredException(String message) {
        super(message);
    }

    public RiskVerificationRequiredException(String securityId, String method) {
        this.method = method;
        this.securityId = securityId;
    }

    public String getSecurityId() {
        return securityId;
    }

    public String getMethod() {
        return method;
    }
}
