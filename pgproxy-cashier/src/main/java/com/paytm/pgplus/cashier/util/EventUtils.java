package com.paytm.pgplus.cashier.util;

import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.log.EventLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventUtils {
    private static final String DEFAULT_PARAM_VALUE = "";

    public static void pushTheiaEvents(EventNameEnum loggerEvents, Map<String, String> metaData) {
        String mid = MDC.get("MID") != null ? MDC.get("MID") : DEFAULT_PARAM_VALUE;
        String orderId = MDC.get("ORDER_ID") != null ? MDC.get("ORDER_ID") : DEFAULT_PARAM_VALUE;
        EventLogger.pushEventLog(mid, orderId, loggerEvents, 1, metaData);
    }

    public static void pushTheiaEvents(EventNameEnum loggerEvents) {
        String mid = MDC.get("MID") != null ? MDC.get("MID") : DEFAULT_PARAM_VALUE;
        String orderId = MDC.get("ORDER_ID") != null ? MDC.get("ORDER_ID") : DEFAULT_PARAM_VALUE;
        EventLogger.pushEventLog(mid, orderId, loggerEvents, 1, null);
    }

    public static void pushTheiaEvents(EventNameEnum loggerEvents, Pair<String, String>... args) {
        Map<String, String> metaData = new LinkedHashMap<>();
        if (args != null) {
            for (Pair<String, String> pair : args) {
                metaData.put(pair.getKey(), pair.getValue());
            }
        }
        pushTheiaEvents(loggerEvents, metaData);
    }
}
