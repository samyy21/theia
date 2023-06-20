package com.paytm.pgplus.biz.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by prashant on 5/8/16.
 */
public class ObjectMapperUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperUtil.class);

    /**
     *
     * @param source
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getListOfObject(String source, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(source, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> Map<String, T> getObjectValueMap(Object obj) {
        return OBJECT_MAPPER.convertValue(obj, Map.class);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
