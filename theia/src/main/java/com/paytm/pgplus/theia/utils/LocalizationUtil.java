package com.paytm.pgplus.theia.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.enums.LanguageCodes;
import com.paytm.pgplus.theia.nativ.model.common.EnhancedCashierPage;
import com.paytm.pgplus.theia.sessiondata.UPITransactionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.paytm.pgplus.logging.ExtendedLogger;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalizationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalizationUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(LocalizationUtil.class);
    private static final String JSON_FILE = ".json";
    private static final String PROPERTY_LOCATION = "/etc/appconf/project/locale/";
    private static final String LOCALE_PARAM = "locale";
    @Autowired
    @Qualifier(value = "merchantPreferenceService")
    private IMerchantPreferenceService merchantPreferenceService;

    @Cacheable(value = TheiaConstant.CacheConstant.LOCALIZATION_KEYS_CACHE, cacheManager = "localizationCacheManager", key = "\"UI_LOCALE_\"+#locale")
    public Map getLocaleAppData(String locale) {
        if (locale != null) {
            String filePath = PROPERTY_LOCATION + locale + JSON_FILE;
            try {
                return new ObjectMapper().readValue(new File(filePath), Map.class);
            } catch (Exception e) {
                LOGGER.error("Error while reading file " + filePath, e);
            }
        }
        return new HashMap();
    }

    public void addLocaleAppData(EnhancedCashierPage enhancedCashierPage) {
        if (isLocaleEnabled()) {
            String languageCode = getLanguageCodeFromRequest();
            enhancedCashierPage.setLanguageCode(languageCode);
            enhancedCashierPage.setLocalePushAppData(getLocaleAppData(languageCode));
        }
    }

    public void addLocaleAppData(UPITransactionInfo upiTransactionInfo) {
        if (isLocaleEnabled()) {
            String languageCode = getLanguageCodeFromRequest();
            upiTransactionInfo.setLocalePushAppData(getLocaleAppData(languageCode));
        }
    }

    public boolean isLocaleEnabled() {
        String localeInRequest = getLanguageCodeFromRequest();
        String mid = getMIDFromHeader();
        if (localeInRequest == null) {
            EXT_LOGGER.customInfo("Locale found as null in request with MID " + mid);
            return false;
        } else if (LanguageCodes.getEnumByLocale(localeInRequest) == null) {
            LOGGER.info(String.format("Unsupported locale %s found in request with MID %s", localeInRequest, mid));
            return false;
        } else if (!merchantPreferenceService.isLocalePrefEnabled(getMIDFromHeader())) {
            LOGGER.info("Locale preference not enabled for MID " + mid);
            return false;
        }
        return true;
    }

    public HttpServletRequest getHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public String getLanguageCodeFromRequest() {
        String languageCode = getHttpRequest().getHeader("X-accept-language");
        return languageCode != null ? languageCode : getHttpRequest().getParameter(LOCALE_PARAM);
    }

    public String getMIDFromHeader() {
        String mid = getHttpRequest().getParameter(TheiaConstant.RequestParams.Native.MID);
        if (mid == null) {
            return getHttpRequest().getParameter(TheiaConstant.RequestParams.MID);
        }
        return mid;
    }
}
