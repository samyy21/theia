package com.paytm.pgplus.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Created by Naman on 19/05/17.
 */
public class RequestUtils {

    private static final String DEFAULT_PROPERTIES = "Test.Main.Payload";

    private static ObjectMapper objectMapper;

    static {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        TimeZone timeZone = TimeZone.getTimeZone("IST");
        sdf.setTimeZone(timeZone);

        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(sdf);
        objectMapper.setTimeZone(TimeZone.getTimeZone("IST"));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T mapJsonToObject(final String jsonObject, final Class<T> clazz) throws IOException {
        final T returnValue = objectMapper.readValue(jsonObject, clazz);
        return returnValue;
    }

    public static WorkFlowRequestBean createWorkFlowRequestBean(String key, Properties properties) throws IOException,
            IllegalAccessException, NoSuchFieldException {

        String jsonOriginalPayLoad = properties.getProperty(DEFAULT_PROPERTIES);

        WorkFlowRequestBean workFlowRequestBean = (WorkFlowRequestBean) mapJsonToObject(jsonOriginalPayLoad,
                WorkFlowRequestBean.class);

        String testCaseJson = properties.get(key).toString();

        WorkFlowRequestBean workFlowRequestBeanTestCasePayload = (WorkFlowRequestBean) mapJsonToObject(testCaseJson,
                WorkFlowRequestBean.class);

        // Map<String, String> testCaseValues = (Map<String, String>)
        // mapJsonToObject(testCaseJson, Map.class);

        Field[] fields = workFlowRequestBean.getClass().getDeclaredFields();

        for (Field field : fields) {

            String fieldName = field.getName();

            if (Modifier.isTransient(field.getModifiers()) || "serialVersionUID".equals(fieldName)) {
                continue;
            }

            Field payloadField = workFlowRequestBeanTestCasePayload.getClass().getDeclaredField(fieldName);
            payloadField.setAccessible(true);
            Object fieldValue = payloadField.get(workFlowRequestBeanTestCasePayload);

            if (null != fieldValue) {
                field.setAccessible(true);
                field.set(workFlowRequestBean, fieldValue);
            }
        }

        return workFlowRequestBean;
    }
}