package com.paytm.pgplus.theia.offline.utils;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseGenerator.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static Response generateResponse(Object o) {
        try {
            return Response.ok().entity(OBJECT_MAPPER.writeValueAsString(o)).build();
        } catch (JsonProcessingException e) {
            LOGGER.error(" Error while objectMapper ", e);
            return Response.ok().entity(o).build();
        }
    }
}
