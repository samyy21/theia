package com.paytm.pgplus.theia.enums;

/**
 * @author kartik
 * @date 11-07-2017
 */
public enum CloseOrderStatus {

    SUCCESS("S", "01", "SUCCESS"), INVALID_REQUEST("F", "02", "Invalid Request"), INVALID_ORDER_STATUS("F", "03",
            "Order status is invalid"), ORDER_ALREADY_CLOSED("F", "04", "Order status is closed"), INTERNAL_PROCESSING_ERROR(
            "F", "05", "Internal Processing Error");

    private String status;
    private String statusCode;
    private String statusMessage;

    CloseOrderStatus(String status, String statusCode, String statusMessage) {
        this.status = status;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}
