package com.paytm.pgplus.theia.offline.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.paytm.pgplus.facade.user.models.CredsAllowed;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;
import java.io.IOException;
import java.util.Map;

public class CustomObjectMapperUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        SimpleModule sm = new SimpleModule();
        sm.addSerializer(CredsAllowed.class, new CustomCredsAllowedSerializer());
        mapper.registerModule(sm);
    }

    public static String convertToString(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public static Map<Object, Object> jsonToMap(String str) throws IOException {
        try {
            return mapper.readValue(str, Map.class);
        } catch (IOException e) {
            throw e;
        }
    }
}
