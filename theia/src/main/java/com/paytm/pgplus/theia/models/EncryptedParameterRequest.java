package com.paytm.pgplus.theia.models;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

public class EncryptedParameterRequest implements Serializable {

    private static final long serialVersionUID = -1920962734049536362L;
    private HttpServletRequest request;
    private String errorCode;
    private String errorMsg;

    public EncryptedParameterRequest() {
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}
