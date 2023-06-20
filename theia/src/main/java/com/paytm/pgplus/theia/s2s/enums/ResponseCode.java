package com.paytm.pgplus.theia.s2s.enums;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;

public enum ResponseCode {

    // Validation Exceptions
    SUCCESS_RESPONSE_CODE("RC-00001", "SUCCESS", "S", "Success", null), INVALID_PAYMENT_DETAILS("RC-00002",
            "INVALID_PAYMENT_DETAILS", "F", "Invalid Payment Details", ResponseConstants.INVALID_PAYMENT_DETAILS), INVALID_REQUEST_TYPE(
            "RC-00003", "INVALID_REQUEST_TYPE", "F", "Invalid Request Type", ResponseConstants.INVALID_REQUEST_TYPE), INVALID_TXN_AMOUNT(
            "RC-00004", "INVALID_TXN_AMOUNT", "F", "Invalid Txn Amount", ResponseConstants.INVALID_TXN_AMOUNT), INVALID_ORDER_ID(
            "RC-00005", "INVALID_ORDER_ID", "F", "Invalid Order ID", ResponseConstants.INVALID_ORDER_ID), INVALID_CARD_NO(
            "RC-00006", "INVALID_CARD_NO", "F", "Invalid Card No", ResponseConstants.INVALID_CARD_NO), INVALID_MONTH(
            "RC-00007", "INVALID_MONTH", "F", "Invalid Month", ResponseConstants.INVALID_MONTH), INVALID_YEAR(
            "RC-00008", "INVALID_YEAR", "F", "Invalid Year", ResponseConstants.INVALID_YEAR), INVALID_CVV("RC-00009",
            "INVALID_CVV", "F", "You have entered wrong CVV for this card. Please try again with correct CVV",
            ResponseConstants.INVALID_CVV), INVALID_PAYMENTMODE("RC-00010", "INVALID_PAYMENTMODE", "F",
            "Invalid payment mode", ResponseConstants.INVALID_PAYMENTMODE), INVALID_CUST_ID("RC-00011",
            "INVALID_CUST_ID", "F", "Invalid CustID", ResponseConstants.INVALID_CUST_ID), INVALID_INDUSTRY_TYPE_ID(
            "RC-00012", "INVALID_INDUSTRY_TYPE", "F", "Invalid Industry Type",
            ResponseConstants.INVALID_INDUSTRY_TYPE_ID), INVALID_CHANNEL("RC-00013", "INVALID_CHANNEL", "F",
            "Invalid channel", ResponseConstants.INVALID_CHANNEL), INVALID_MID("RC-00014", "INVALID_MID", "F",
            "Invalid MID", ResponseConstants.INVALID_MID), INVALID_MOBILE_NUMBER("RC-00015", "INVALID_MOBILE_NUMBER",
            "F", "Invalid Mobile Number", ResponseConstants.INVALID_MOBILE_NUMBER), INVALID_PARAM("RC-00016",
            "INVALID_PARAM", "F", "System Error, invalid param", ResponseConstants.INVALID_PARAM), FGW_BANK_FORM_RETRIEVE_FAILED(
            "RC-00017", "BANK_FORM_RETRIEVE_FAILED", "F", "Invalid Payment Request",
            ResponseConstants.FGW_BANK_FORM_RETRIEVE_FAILED), MERCHANT_VELOCITY_LIMIT_BREACH("RC-00018",
            "MERCHANT_VELOCITY_LIMIT_BREACH", "F", "Merchant velocity limit breached", null), ACCESS_DENIED("RC-00019",
            "ACCESS_DENIED", "F", "Invalid Request Type", ResponseConstants.ACCESS_DENIED), INVALID_CHECKSUM(
            "RC-00020", "INVALID_CHECKSUM", "F", "Checksum provided is invalid", null), INVALID_CLIENT("RC-00021",
            "INVALID_CLIENT", "F", "Invalid client id", null), INVALID_API_VERSION("RC-00022", "INVALID_API_VERSION",
            "F", "Invalid api version", null), INVALID_WEBSITE("RC-00023", "INVALID_WEBSITE", "F", "Invalid website",
            null), INVALID_REQUEST_HEADER("RC-00024", "INVALID_REQUEST_HEADER", "F", "Invalid request header", null), INVALID_REQUEST_BODY(
            "RC-00025", "INVALID_REQUEST_BODY", "F", "Invalid request body", null), INTERNAL_PROCESSING_ERROR(
            "RC-00026", "INTERNAL_PROCESSING_ERROR", "F", "Internal Processing Error",
            ResponseConstants.INTERNAL_PROCESSING_ERROR), FGW_INVALID_VPA("RC-00027", "INVALID_VPA", "F",
            "Invalid VPA", ResponseConstants.FGW_INVALID_VPA);

    private String resultCodeId;
    private String resultCode;
    private String resultStatus;
    private String resultMsg;
    private ResponseConstants responseConstant;

    private ResponseCode(String resultCodeId, String resultCode, String resultStatus, String resultMsg,
            ResponseConstants responseConstant) {
        this.resultCodeId = resultCodeId;
        this.resultCode = resultCode;
        this.resultStatus = resultStatus;
        this.resultMsg = resultMsg;
        this.responseConstant = responseConstant;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public ResponseConstants getResponseConstant() {
        return responseConstant;
    }

    public void setResponseConstant(ResponseConstants responseConstant) {
        this.responseConstant = responseConstant;
    }

    public static ResponseCode getResponseCodeByResponseConstant(ResponseConstants responseConstant) {
        if (responseConstant != null) {
            for (ResponseCode responseCode : values()) {
                if (responseCode.getResponseConstant() != null
                        && responseCode.getResponseConstant().equals(responseConstant)) {
                    return responseCode;
                }
            }
        }
        return ResponseCode.INTERNAL_PROCESSING_ERROR;
    }

}
