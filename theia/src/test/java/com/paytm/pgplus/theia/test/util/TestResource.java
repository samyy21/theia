package com.paytm.pgplus.theia.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kartik
 * @date 26-05-2017
 */
public class TestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResource.class);

    private static volatile TestResource INSTANCE;
    private ObjectMapper objectMapper;

    private Properties testProperties;
    private static final String TEST_FILE = "testresource.properties";

    private TestResource() {
        if (INSTANCE != null) {
            throw new IllegalArgumentException("TestResource Instance already exits!");
        }
        loadTestProperties(TEST_FILE);
        initiateObjectMapper();
    }

    public static TestResource getInstance() {
        if (INSTANCE == null) {
            synchronized (TestResource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TestResource();
                }
            }
        }
        return INSTANCE;
    }

    private void loadTestProperties(String fileName) {
        testProperties = new Properties();
        try (InputStream is1 = getClass().getClassLoader().getResourceAsStream(fileName)) {
            testProperties.load(is1);
        } catch (IOException e) {
            LOGGER.error("");
        }
    }

    private void initiateObjectMapper() {
        objectMapper = new ObjectMapper();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Properties getTestProperties() {
        return testProperties;
    }

}
