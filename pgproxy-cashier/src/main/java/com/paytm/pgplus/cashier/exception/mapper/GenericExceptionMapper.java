/**
 *
 */
package com.paytm.pgplus.cashier.exception.mapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(final Throwable error) {
        final String errMsg = error.getMessage();

        return Response.status(getHttpStatus(error)).entity(errMsg).type(MediaType.APPLICATION_JSON).build();
    }

    private int getHttpStatus(final Throwable error) {
        if (error instanceof WebApplicationException) {
            return (((WebApplicationException) error).getResponse().getStatus());
        }
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }
}