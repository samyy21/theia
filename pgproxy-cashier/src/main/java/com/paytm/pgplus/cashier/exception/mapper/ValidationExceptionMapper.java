/**
 *
 */
package com.paytm.pgplus.cashier.exception.mapper;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    StringBuilder errorMsg;

    @Override
    public Response toResponse(ConstraintViolationException ex) {

        for (ConstraintViolation<?> obj : ex.getConstraintViolations()) {
            errorMsg.append(obj.getPropertyPath().toString() + " ");
            errorMsg.append(obj.getMessage() + "\n");
            break;
        }
        LOGGER.error("Constraint voilation error");
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).build();
    }
}