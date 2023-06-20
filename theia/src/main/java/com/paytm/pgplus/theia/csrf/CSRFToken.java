package com.paytm.pgplus.theia.csrf;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by ankitsinghal on 18/05/17.
 */
public class CSRFToken implements Serializable {

    private static final long serialVersionUID = 428857123681138674L;

    public static final String CSRF_PARAM_NAME = "CSRF_PARAM";
    public static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private final String token;

    private final String parameterName;

    private final String headerName;

    public CSRFToken(String token) {
        this.parameterName = CSRF_PARAM_NAME;
        this.headerName = CSRF_HEADER_NAME;
        this.token = Objects.toString(token, "");
    }

    public CSRFToken(String headerName, String parameterName, String token) {
        Assert.hasLength(headerName, "headerName cannot be null or empty");
        Assert.hasLength(parameterName, "parameterName cannot be null or empty");
        this.headerName = headerName;
        this.parameterName = parameterName;
        this.token = Objects.toString(token, "");
    }

    public String getHeaderName() {
        return this.headerName;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public String getToken() {
        return this.token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CSRFToken csrfToken = (CSRFToken) o;

        return token != null ? token.equals(csrfToken.token) : csrfToken.token == null;
    }

    @Override
    public int hashCode() {
        return token != null ? token.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "[" + token + ']';
    }

}
