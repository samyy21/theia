/*
 * This File is  the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.cashier.exception.mapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * @author amit.dubey
 *
 */
@Provider
public class UnrecognizedPropertyExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

    @Override
    public Response toResponse(final UnrecognizedPropertyException exception) {
        final String errMsg = exception.getMessage();
        return Response.status(getHttpStatus(exception)).entity(errMsg).type(MediaType.APPLICATION_JSON).build();
    }

    private int getHttpStatus(final Throwable error) {
        if (error instanceof WebApplicationException) {
            return (((WebApplicationException) error).getResponse().getStatus());
        }
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }
}