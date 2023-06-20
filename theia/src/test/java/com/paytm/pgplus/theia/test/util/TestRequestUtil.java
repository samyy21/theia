package com.paytm.pgplus.theia.test.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kartik
 * @date 26-05-2017
 */
public class TestRequestUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(TestRequestUtil.class);

    public static <T> T mapJsonToObject(ObjectMapper objectMapper, String jsonString, Class<T> clazz) {
        T obj = null;
        try {
            if (StringUtils.isNotBlank(jsonString)) {
                obj = objectMapper.readValue(jsonString, clazz);
            } else {
                LOGGER.error("jsonString passed is blank");
            }
        } catch (Exception e) {
            LOGGER.error("Error in parsing json string", e);
        }
        return obj;
    }

    public static String getOrderID() {
        return "ORDER" + System.currentTimeMillis();
    }
}
