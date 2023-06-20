package com.paytm.pgplus.theia.utils;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.models.UserAgentInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.theia.utils.EnvInfoUtil.httpServletRequest;

@Component
public class BrowserUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserUtil.class);

    private static Map<String, List<String>> appInvokeSupportedBrowsers;
    private static Map<String, String> appLinkBrowserMap;
    private static UserAgentParser parser;

    static {
        try {
            loadAppLinkBrowserMap();
            loadAppInvokeCompatibleBrowserList();
            parser = new UserAgentService().loadParser();
        } catch (Exception e) {
            LOGGER.error("Exception in loading browser mappings {} ", e);
        }
    }

    private static void loadAppLinkBrowserMap() {
        appLinkBrowserMap = new HashMap<>();
        String browserMappingConfiguration = ConfigurationUtil.getProperty("theia.appInvoke.appLinkBrowserMap");
        // sample format : browserMappingConfiguration =
        // "Chrome:googlechrome;Edge:edge;Safari:safari";
        if (StringUtils.isNotBlank(browserMappingConfiguration)) {
            List<String> browserMappings = Arrays.asList(browserMappingConfiguration.split(";"));
            for (String browsers : browserMappings) {
                List<String> browserMap = Arrays.asList(StringUtils.split(browsers, ":"));
                if (browserMap.size() == 2) {
                    String browserName = browserMap.get(0);
                    String appLinkBrowserName = browserMap.get(1);
                    appLinkBrowserMap.put(browserName, appLinkBrowserName);
                }
            }
        }
    }

    private static void loadAppInvokeCompatibleBrowserList() {
        appInvokeSupportedBrowsers = new HashMap<>();
        String appInvokeSupportedBrowserConfiguration = ConfigurationUtil
                .getProperty("theia.appInvoke.supportedBrowsers");
        // sample format : appInvokeSupportedBrowserConfiguration =
        // "Android:Chrome,Edge;Ios:Chrome,Edge,FireFox;";
        if (StringUtils.isNotBlank(appInvokeSupportedBrowserConfiguration)) {
            List<String> browserMappings = Arrays.asList(appInvokeSupportedBrowserConfiguration.split(";"));
            for (String browsers : browserMappings) {
                List<String> browserMap = Arrays.asList(StringUtils.split(browsers, ":"));
                if (browserMap.size() == 2) {
                    String os = browserMap.get(0);
                    List<String> supportedBrowsers = Arrays.asList(browserMap.get(1).split(","));
                    appInvokeSupportedBrowsers.put(os, supportedBrowsers);
                }
            }
        }
    }

    public static boolean isBrowserSupported(UserAgentInfo userAgentInfo) {
        if (userAgentInfo == null) {
            return false;
        }
        String browser = userAgentInfo.getBrowser();
        List<String> supportedBrowsers = null;
        if (userAgentInfo.detectAndroid()) {
            supportedBrowsers = appInvokeSupportedBrowsers.get("Android");
        } else if (userAgentInfo.detectIos()) {
            supportedBrowsers = appInvokeSupportedBrowsers.get("Ios");
        }
        if (CollectionUtils.isNotEmpty(supportedBrowsers)) {
            for (String supportedBrowser : supportedBrowsers) {
                if (StringUtils.isNotBlank(browser) && StringUtils.isNotBlank(supportedBrowser)
                        && browser.contains(supportedBrowser)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getBrowserNameForAppLink(UserAgentInfo userAgentInfo) {
        if (userAgentInfo == null) {
            return null;
        }
        return appLinkBrowserMap.get(userAgentInfo.getBrowser());
    }

    public static UserAgentInfo getUserAgentInfo() throws FacadeCheckedException {
        HttpServletRequest request = httpServletRequest();
        String browserUserAgent = request.getHeader(TheiaConstant.RequestHeaders.USER_AGENT);
        if (browserUserAgent == null) {
            LOGGER.error("browserUserAgent is null");
            return null;
        }
        final Capabilities capabilities = parser.parse(browserUserAgent);
        if (capabilities == null) {
            LOGGER.error("Error occured in parsing user agent string");
            return null;
        } else {
            UserAgentInfo.Builder userAgentBuilder = new UserAgentInfo.Builder();
            userAgentBuilder.browser(capabilities.getBrowser());
            userAgentBuilder.browserMajorVersion(capabilities.getBrowserMajorVersion());
            userAgentBuilder.browserType(capabilities.getBrowserType());
            userAgentBuilder.deviceType(capabilities.getDeviceType());
            userAgentBuilder.platform(capabilities.getPlatform());
            userAgentBuilder.platformVersion(capabilities.getPlatformVersion());
            return userAgentBuilder.build();
        }
    }
}
