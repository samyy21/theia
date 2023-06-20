package com.paytm.pgplus.theia.enums;

public enum ValidationResults {

    CHECKSUM_VALIDATION_FAILURE(true), UNKNOWN_VALIDATION_FAILURE(false), VALIDATION_SUCCESS, MERCHANT_SPECIFIC_VALIDATION_FAILURE, INVALID_REQUEST;

    boolean isInsertRequired;

    private ValidationResults(boolean isInsertRequired) {
        this.isInsertRequired = isInsertRequired;
    }

    private ValidationResults() {
    }

}
