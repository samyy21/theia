/**
 * 
 */
package com.paytm.pgplus.session.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * @createdOn 07-Mar-2016
 * @author kesari
 */
public class CustomJsonMapper {

    /**
     * Json Object mapper to marshal and unmarshal java and json objects
     */
    private ObjectMapper jacksonMapper = new ObjectMapper();

    /**
     * Default constructor defining common configurations for mapper to marshal
     * and unmarshal java and json objects
     */
    private CustomJsonMapper() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
        jacksonMapper.setDateFormat(dateFormat);
        jacksonMapper.setTimeZone(TimeZone.getDefault());
        jacksonMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

    /**
     * Form Java Objejct to json String
     * 
     * @param javaObject
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static String toJsonString(Object javaObject) throws JsonGenerationException, JsonMappingException,
            IOException {
        return getInstance().jacksonMapper.writeValueAsString(javaObject);
    }

    /**
     * 
     * @param jsonNode
     * @param clazz
     * @return
     * @throws JsonProcessingException
     */
    public static <T> T toJavaObject(JsonNode jsonNode, Class<T> clazz) throws JsonProcessingException {
        return getInstance().jacksonMapper.treeToValue(jsonNode, clazz);
    }

    /**
     * 
     * @param inputStream
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    public static JsonNode readTree(InputStream inputStream) throws IOException, JsonProcessingException {
        return getInstance().jacksonMapper.readTree(inputStream);
    }

    /**
     * 
     * @param jsonData
     * @return
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static JsonNode stringToJson(String jsonData) throws JsonProcessingException, IOException {
        return getInstance().jacksonMapper.readTree(jsonData);
    }

    /**
     * Singleton instance generator class
     * 
     * @createdOn 07-Mar-2016
     * @author kesari
     */
    private static class CustomJsonMapperUnitGenerator {
        /**
         * Singleton instance
         */
        private static final CustomJsonMapper INSTANCE = new CustomJsonMapper();
    }

    /**
     * Singleton instance generator
     * 
     * @return
     */
    private static CustomJsonMapper getInstance() {
        return CustomJsonMapperUnitGenerator.INSTANCE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone not supported !");
    }

}
