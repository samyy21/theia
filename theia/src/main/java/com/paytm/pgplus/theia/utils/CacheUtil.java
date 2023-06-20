package com.paytm.pgplus.theia.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheUtil {
    private static final Map<String, List<Field>> cacheLocaleFieldsInClass = new HashMap<>();

    public static List<Field> getLocaleFieldsInClass(String className) {
        return cacheLocaleFieldsInClass.get(className);
    }

    public static void setLocaleFieldsInClass(String className, List<Field> localeFields) {
        cacheLocaleFieldsInClass.put(className, localeFields);
    }
}
