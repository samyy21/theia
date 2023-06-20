package com.paytm.pgplus.theia.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorUtils<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorUtils.class);

    public static <T> T printErrorAndReturn(String errorMessage, T returnValue) {
        LOGGER.error(errorMessage);
        return returnValue;
    }
}
