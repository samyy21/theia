/*
 * This code contains copyright information which is the proprietary property
 *
 * of Tarang Software Technologies Pvt Ltd . No part of this code may be reproduced, stored or
 * transmitted in any form without the prior written permission.
 *
 * Copyright (C) Tarang Software Technologies Pvt Ltd 2012. All rights reserved.
 * ------------------------------------------------------------------------------ Version : 1.0
 * Created on : 01 August 2012 Author : Saravanan P Description : This Class will give the security
 * exception details. ------------------------------------------------------------------------------
 * Change History ------------------------------------------------------------------------------
 *
 * ------------------------------------------------------------------------------
 */
package com.paytm.pgplus.biz.exception;

/**
 * This Class will give the security exception details.
 */
public class SecurityException extends BaseException {

    private static final long serialVersionUID = -3956900350777254445L;
    private String errorCode;
    private String errorMessage;

    /**
     * Instantiates a new security exception.
     *
     * @param errorCode
     *            the error code
     * @param errorMessage
     *            the error message
     */
    public SecurityException(final String errorCode, final String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * Instantiates a new security exception.
     *
     * @param errorMessage
     *            the error message
     */
    public SecurityException(final String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new security exception.
     *
     * @param cause
     *            the cause
     */
    public SecurityException(final Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new security exception.
     *
     * @param errorMessage
     *            the error message
     * @param cause
     *            the cause
     */
    public SecurityException(final String errorMessage, final Throwable cause) {
        super(errorMessage, cause);
    }

    /**
     * @return the errorCode
     */
    @Override
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode
     *            the errorCode to set
     */
    @Override
    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return the errorMessage
     */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     *            the errorMessage to set
     */
    @Override
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
