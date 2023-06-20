package com.paytm.pgplus.theia.utils;

import com.google.common.collect.ImmutableMap;
import com.paytm.pgplus.theia.enums.LanguageCodes;
import com.paytm.pgplus.theia.enums.UPIAppNamesRegional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class UPIRegionalNameUtil {
    private static Map<String, Map<String, String>> upiAppNamesRegional;
    private static final Logger LOGGER = LoggerFactory.getLogger(UPIRegionalNameUtil.class);

    static {
        upiAppNamesRegional = new HashMap<>();
        upiAppNamesRegional.put(LanguageCodes.Hindi.getLocale(), createUPIRegionalMap("hi"));
        upiAppNamesRegional.put(LanguageCodes.Bangla.getLocale(), createUPIRegionalMap("bn"));
        upiAppNamesRegional.put(LanguageCodes.Oriya.getLocale(), createUPIRegionalMap("or"));
        upiAppNamesRegional.put(LanguageCodes.Marathi.getLocale(), createUPIRegionalMap("mr"));
        upiAppNamesRegional.put(LanguageCodes.Malayalam.getLocale(), createUPIRegionalMap("ml"));
        upiAppNamesRegional.put(LanguageCodes.Kannada.getLocale(), createUPIRegionalMap("kn"));
        upiAppNamesRegional.put(LanguageCodes.Tamil.getLocale(), createUPIRegionalMap("ta"));
        upiAppNamesRegional.put(LanguageCodes.Telugu.getLocale(), createUPIRegionalMap("te"));
        upiAppNamesRegional.put(LanguageCodes.Gujarati.getLocale(), createUPIRegionalMap("gu"));
        upiAppNamesRegional.put(LanguageCodes.Punjabi.getLocale(), createUPIRegionalMap("pa"));
    }

    public static Map<String, Map<String, String>> getUpiAppNamesRegional() {
        return upiAppNamesRegional;
    }

    public static Map<String, String> createUPIRegionalMap(String language) {
        Map<String, String> upiLocaleNameMap = new HashMap<>();
        try {
            Iterator<String> i1 = UPIAppNamesRegional.valueOf("en").getValues().iterator();
            Iterator<String> i2 = UPIAppNamesRegional.valueOf(language).getValues().iterator();
            while (i1.hasNext() && i2.hasNext()) {
                upiLocaleNameMap.put(i1.next(), i2.next());
            }
            return upiLocaleNameMap;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid locale passed in request - " + language);
        }
        return upiLocaleNameMap;
    }
}
