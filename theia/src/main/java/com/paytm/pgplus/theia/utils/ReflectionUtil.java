package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ReflectionUtil {
    private static final String REGIONAL_SUFFIX = "Regional";
    private static final String LIST_TYPE = "List";
    private static final String SET_TYPE = "Set";
    public static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

    // recursive call
    public static void populateLocaleFieldsInApiResponse(Object object, Map<String, String> localeStringMap,
            Set<String> notFoundText) {
        if (object != null && !object.getClass().getTypeName().contains("java.lang")
                && !object.getClass().getTypeName().contains("java.util")
                && !object.getClass().getTypeName().contains("EnhancedCashierMgvPayMode")) {
            populateLocaleFieldsInObject(object, localeStringMap, notFoundText);
            for (Field nonLocaleClassField : getNonLocaleClassFields(object)) {
                try {
                    nonLocaleClassField.setAccessible(true);
                    if (nonLocaleClassField.get(object) instanceof List<?>) {
                        for (Object listObject : (List<?>) nonLocaleClassField.get(object)) {
                            populateLocaleFieldsInApiResponse(listObject, localeStringMap, notFoundText);
                        }
                    } else if (nonLocaleClassField.get(object) instanceof Map<?, ?>) {
                        populateLocaleFieldsInMapObject(nonLocaleClassField, localeStringMap, notFoundText, object);
                    } else {
                        populateLocaleFieldsInApiResponse(nonLocaleClassField.get(object), localeStringMap,
                                notFoundText);
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("Error while getting object of non-locale field in object " + object, e);
                }
            }
        }
    }

    private static void populateLocaleFieldsInMapObject(Field nonLocaleClassField, Map<String, String> localeStringMap,
            Set<String> notFoundText, Object object) throws IllegalAccessException {
        ParameterizedType genericMapType = (ParameterizedType) nonLocaleClassField.getGenericType();
        Class<?> valueClass = (Class<?>) genericMapType.getActualTypeArguments()[1];
        if (!valueClass.equals(String.class) && !valueClass.equals(Object.class)
                && !ClassUtils.isPrimitiveOrWrapper(valueClass)) {
            Map nonLocaleMap = (Map) nonLocaleClassField.get(object);
            for (Object valueObject : nonLocaleMap.values()) {
                populateLocaleFieldsInApiResponse(valueObject, localeStringMap, notFoundText);
            }
        }
    }

    public static void populateLocaleFieldsInObject(Object object, Map<String, String> localeStringMap,
            Set<String> notFoundText) {
        for (Field field : getLocaleFieldsInObject(object)) {
            field.setAccessible(true);
            // either List<String> or Set<String> or String
            if (field.getType().equals(List.class)) {
                updateListValues(field, object, localeStringMap, LIST_TYPE, notFoundText);
            } else if (field.getType().equals(Set.class)) {
                updateListValues(field, object, localeStringMap, SET_TYPE, notFoundText);
            } else {
                setRegionalFieldValue(field, object, localeStringMap, notFoundText);
            }
        }
    }

    private static List<Field> getLocaleFieldsInObject(Object object) {
        String objectClass = object.getClass().toString();
        List<Field> localeFields = CacheUtil.getLocaleFieldsInClass(objectClass);
        if (localeFields == null) {
            localeFields = FieldUtils.getAllFieldsList(object.getClass());
            localeFields = localeFields.stream().filter(v -> v.getAnnotation(LocaleField.class) != null)
                    .collect(Collectors.toList());
            CacheUtil.setLocaleFieldsInClass(objectClass, localeFields);
        }
        return localeFields;
    }

    public static void setRegionalFieldValue(Field field, Object object, Map<String, String> localeStringMap,
            Set<String> notFoundText) {
        String regionalFieldName = field.getName() + REGIONAL_SUFFIX;
        try {
            Field regionalField = getField(object.getClass(), regionalFieldName);
            regionalField.setAccessible(true);
            String plainString = (String) field.get(object);
            if (localeStringMap.get(plainString) != null) {
                regionalField.set(object, localeStringMap.get(plainString));
            } else {
                notFoundText.add(plainString);
                regionalField.set(object, plainString);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error(String.format("Regional field named %s not found in class %s.", regionalFieldName, object
                    .getClass().getName()), e);
        }
    }

    public static List<Field> getNonLocaleClassFields(Object object) {
        List<Field> fieldsInClassAndSuperClass = FieldUtils.getAllFieldsList(object.getClass());
        // todo keep private method
        if (CollectionUtils.isEmpty(fieldsInClassAndSuperClass)) {
            return new ArrayList<>();
        }
        return fieldsInClassAndSuperClass.stream().filter(v -> v.getAnnotation(LocaleField.class) == null)
                .filter(v -> !v.getType().isPrimitive()).filter(v -> !v.getType().isEnum())
                .filter(v -> !v.getType().toString().contains("java.lang")).filter(Objects::nonNull)
                .filter(v -> !v.getGenericType().getTypeName().contains("Logger")).collect(Collectors.toList());
    }

    private static void updateListValues(Field plainField, Object object, Map<String, String> localeStringMap,
            String classType, Set<String> notFoundText) {
        try {
            String regionalFieldName = plainField.getName() + REGIONAL_SUFFIX;
            Field regionalField = getField(object.getClass(), regionalFieldName);
            regionalField.setAccessible(true);
            if (classType.equals(SET_TYPE)) {
                Set<String> plainStringList = (Set<String>) plainField.get(object);
                Set<String> regionalStringSet = new HashSet<>(getRegionalList(new ArrayList<>(plainStringList),
                        localeStringMap, notFoundText));
                regionalField.set(object, regionalStringSet);
            } else if (classType.equals(LIST_TYPE)) {
                List<String> plainStringList = (List<String>) plainField.get(object);
                regionalField.set(object, getRegionalList(plainStringList, localeStringMap, notFoundText));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error(String.format("Error while adding regional values in field %s of class %s", plainField, object
                    .getClass().getName()), e);
        }
    }

    private static List<String> getRegionalList(List<String> plainStringList, Map<String, String> localeStringMap,
            Set<String> notFoundText) {
        List<String> regionalFieldList = new ArrayList<>();
        if (plainStringList != null) {
            for (String plainString : plainStringList) {
                if (localeStringMap.get(plainString) != null) {
                    regionalFieldList.add(localeStringMap.get(plainString));
                } else {
                    // todo: add unavailable string in kafka queue
                    notFoundText.add(plainString);
                    regionalFieldList.add(plainString);
                }
            }
        }
        return regionalFieldList;
    }

    public static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }
}
