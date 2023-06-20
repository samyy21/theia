/**
 *
 */
package com.paytm.pgplus.theia.utils;

import com.google.common.base.Strings;
import com.paytm.pgplus.biz.core.validator.BizParamValidator;
import com.paytm.pgplus.common.enums.ETerminalType;
import com.paytm.pgplus.common.enums.TerminalType;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.common.model.EnvInfo;
import com.paytm.pgplus.facade.exception.FacadeInvalidParameterException;
import com.paytm.pgplus.request.BaseHeader;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.constants.TheiaConstant.ExtraConstants;
import com.paytm.pgplus.theia.constants.TheiaConstant.RequestHeaders;
import net.sf.uadetector.ReadableDeviceCategory.Category;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.pgplus.theia.enums.DeviceSource.getDeviceSourceByValue;

/**
 * @author amit.dubey
 */
public class EnvInfoUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvInfoUtil.class);
    private static final String DUMMY_USER_AGENT = "DummyUserAgent";
    private static final String DEFAULT_CLIENT_IP = "127.0.0.1";

    private static UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();

    public static EnvInfoRequestBean fetchEnvInfo(HttpServletRequest request) {
        LOGGER.debug("Request received for fetching EnvInfo");

        EnvInfoRequestBean envInfo = new EnvInfoRequestBean();
        ReadableUserAgent userAgent = null;

        // Need to set dummy user-agent if it is not available
        String browserUserAgent = request.getHeader(RequestHeaders.USER_AGENT);
        if (StringUtils.isBlank(browserUserAgent)) {
            browserUserAgent = DUMMY_USER_AGENT;
        }
        userAgent = getUserAgent(browserUserAgent);
        // End

        if (userAgent != null) {
            envInfo.setOsType(userAgent.getOperatingSystem().getName());
            Map<String, String> extendedInfo = envInfo.getExtendInfo();
            if (extendedInfo == null)
                extendedInfo = new HashMap<>();
            extendedInfo.put("browserName", userAgent.getFamily().getName() + " "
                    + userAgent.getVersionNumber().toVersionString());
            extendedInfo.put("deviceCategory", userAgent.getDeviceCategory().getName());
            envInfo.setExtendInfo(extendedInfo);
        }
        // PGP-31672 added support for ipv6
        String clientIp = getClientIP(request);
        envInfo.setClientIp(clientIp);

        String network_type = request.getHeader(RequestHeaders.NETWORK_TYPE);
        String isp = request.getHeader(RequestHeaders.ISP);
        String zipCode = request.getHeader(RequestHeaders.ZIPCODE);
        String city = request.getHeader(RequestHeaders.CITY);
        String state = request.getHeader(RequestHeaders.STATE);
        String country = request.getHeader(RequestHeaders.COUNTRY);
        Map<String, String> extendedInfo = envInfo.getExtendInfo();
        if (extendedInfo == null)
            extendedInfo = new HashMap<>();
        extendedInfo.put("networkType", network_type);
        extendedInfo.put("isp", isp);
        extendedInfo.put("zipCode", zipCode);
        extendedInfo.put("city", city);
        extendedInfo.put("state", state);
        extendedInfo.put("country", country);
        envInfo.setExtendInfo(extendedInfo);

        if (StringUtils.isNotBlank(request.getParameter(TheiaConstant.RequestParams.CHANNEL_ID))) {
            envInfo.setTerminalType(ETerminalType.getTerminalTypeByTerminal(request
                    .getParameter(TheiaConstant.RequestParams.CHANNEL_ID)));
        } else if (StringUtils.isNotBlank((String) request.getAttribute(TheiaConstant.RequestParams.CHANNEL_ID))) {
            envInfo.setTerminalType(ETerminalType.getTerminalTypeByTerminal((String) request
                    .getAttribute(TheiaConstant.RequestParams.CHANNEL_ID)));
        } else {
            envInfo.setTerminalType(getTerminalTypeFromUserAgent(userAgent));
        }
        if (TerminalType.APP.equals(envInfo.getTerminalType())) {
            envInfo.setAppVersion(userAgent.getVersionNumber().toVersionString());
        }
        envInfo.setTokenId(request.getParameter(TheiaConstant.RequestParams.DEVICE_ID));
        envInfo.setBrowserUserAgent(browserUserAgent);

        String riskSessionId = getDeviceIdFromCookie(request);
        if (!Strings.isNullOrEmpty(riskSessionId)) {
            envInfo.setSessionId(riskSessionId);
        }

        setRiskParameters(request, envInfo, riskSessionId);

        LOGGER.debug("Env Info request bean as : {} ", envInfo);
        return envInfo;
    }

    private static ReadableUserAgent getUserAgent(String userAgent) {
        return parser.parse(userAgent);
    }

    public static String getClientIP(HttpServletRequest request) {
        // PGP-36172 pass through ipv6 it it comes
        if (Boolean.parseBoolean(ConfigurationUtil.getProperty(ExtraConstants.ENABLE_IPV6, "true"))) {
            return getClientIPV2(request);
        }
        String clientIp = getClientIp(request.getHeader(RequestHeaders.X_FORWARDED_FOR));
        return validateClientIp(clientIp) ? clientIp : DEFAULT_CLIENT_IP;
    }

    private static String getClientIp(String clientIp) {
        if (StringUtils.isNotBlank(clientIp)) {
            String clientIps[] = clientIp.split(",");
            clientIp = clientIps[0].trim();
        }
        return clientIp;
    }

    public static ETerminalType getTerminalType(HttpServletRequest request) {

        return getTerminalTypeFromUserAgent(getUserAgent(request.getHeader(RequestHeaders.USER_AGENT)));
    }

    /**
     * @param userAgent
     * @return
     */
    public static ETerminalType getTerminalTypeFromUserAgent(ReadableUserAgent userAgent) {
        ETerminalType terminalType;

        if (userAgent == null) {
            LOGGER.info("User agent not available, setting this as SYSTEM");
            return ETerminalType.SYSTEM;
        }

        Category category = userAgent.getDeviceCategory().getCategory();
        switch (category) {
        case GAME_CONSOLE:
        case PDA:
        case SMARTPHONE:
            terminalType = ETerminalType.WAP;
            break;
        case OTHER:
        case PERSONAL_COMPUTER:
        case SMART_TV:
        case TABLET:
        case UNKNOWN:
        case WEARABLE_COMPUTER:
        default:
            terminalType = ETerminalType.WEB;
            break;
        }
        return terminalType;
    }

    public static HttpServletRequest httpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static void setChannelDFromUserAgent(BaseHeader requestHeader) {

        if (requestHeader != null && requestHeader.getChannelId() == null) {
            HttpServletRequest httpServletRequest = httpServletRequest();
            if (httpServletRequest != null) {
                EChannelId eChannelId = EnvInfoUtil.getChannelID(
                        httpServletRequest.getHeader(TheiaConstant.RequestHeaders.USER_AGENT),
                        httpServletRequest.getHeader(TheiaConstant.RequestHeaders.ACCEPT));

                requestHeader.setChannelId(eChannelId);
            }
        }
    }

    /**
     *
     * @param userAgent
     * @param accept
     * @return
     */

    public static EChannelId getChannelID(String userAgent, String accept) {

        if (StringUtils.isBlank(userAgent)) {
            LOGGER.info("User agent not available, setting this as SYSTEM");
            return EChannelId.SYSTEM;
        }

        UserAgentInfo userAgentInfo = new UserAgentInfo(userAgent, accept);
        if (userAgentInfo.detectMobileLong()) {

            return EChannelId.WAP;

        } else if (userAgentInfo.isDesktopBrowser()) {

            return EChannelId.WEB;
        }
        return EChannelId.WEB;
    }

    private static void setRiskParameters(HttpServletRequest request, final EnvInfoRequestBean envInfo,
            String deviceIdFromCookies) {
        String deviceSource = request.getParameter(TheiaConstant.RequestParams.DEVICE_SOURCE);
        String deviceId = request.getParameter(TheiaConstant.RequestParams.DEVICE_ID);
        if (Strings.isNullOrEmpty(deviceId)) {
            LOGGER.debug(" Unable to find deviceId in Request trying to set value from cookie.");
            deviceId = deviceIdFromCookies;
        }

        switch (getDeviceSourceByValue(deviceSource)) {
        case PG:
        case BLANK:
        case PGPLUS:
            Map<String, String> extendInfo = envInfo.getExtendInfo();
            if (extendInfo == null)
                extendInfo = new HashMap<>();
            extendInfo.put(TheiaConstant.ExtraConstants.DEVICE_ID, deviceId);
            envInfo.setExtendInfo(extendInfo);
            if (Strings.isNullOrEmpty(envInfo.getTokenId())) {
                envInfo.setTokenId(deviceId);
            }
            break;
        }
    }

    private static String getDeviceIdFromCookie(HttpServletRequest request) {
        String value = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ExtraConstants.RISK_SESSION_COOKIE.equals(cookie.getName())) {
                    LOGGER.info("Found Risk Cookie from Session. ");
                    return cookie.getValue();
                }
            }
        }
        return value;
    }

    public static EnvInfo createEnvInfo(final EnvInfoRequestBean envInfoRequestBean)
            throws FacadeInvalidParameterException {

        final String clientIp = envInfoRequestBean.getClientIp();
        final TerminalType terminalType = TerminalType.getTerminalTypeByTerminal(envInfoRequestBean.getTerminalType()
                .toString());
        final EnvInfo.EnvInfoBuilder envBuilder = new EnvInfo.EnvInfoBuilder(clientIp, terminalType.getTerminal());

        envBuilder.sessionId(envInfoRequestBean.getSessionId());
        envBuilder.tokenId(envInfoRequestBean.getTokenId());
        envBuilder.websiteLanguage(envInfoRequestBean.getWebsiteLanguage());
        envBuilder.osType(envInfoRequestBean.getOsType());
        envBuilder.appVersion(envInfoRequestBean.getAppVersion());
        envBuilder.sdkVersion(envInfoRequestBean.getSdkVersion());
        envBuilder.clientKey(envInfoRequestBean.getClientKey());
        envBuilder.orderTerminalType(envInfoRequestBean.getOrderTerminalType());
        envBuilder.orderOsType(envInfoRequestBean.getOrderOsType());
        envBuilder.merchantAppVersion(envInfoRequestBean.getMerchantAppVersion());
        Map<String, String> extendedInfo = envInfoRequestBean.getExtendInfo();
        if (!TerminalType.WEB.equals(terminalType)) {
            if (extendedInfo == null)
                extendedInfo = new HashMap<>();
            extendedInfo.put("deviceId", envInfoRequestBean.getTokenId());
        }
        envBuilder.extendInfo(extendedInfo);
        final EnvInfo envInfo = envBuilder.build();
        return envInfo;

    }

    private static boolean validateClientIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        if (ip.length() > 15) {
            return false;
        }
        return true;
    }

    public static void populateDataEnrichmentFields(Map<String, String> riskExtendedInfoMap, EnvInfoRequestBean envInfo) {
        try {
            if (riskExtendedInfoMap == null || envInfo == null)
                return;
            Map<String, String> extendInfo = envInfo.getExtendInfo();
            if (StringUtils.isBlank(envInfo.getAppVersion())) {
                String appVersion = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.APP_VERSION_KEY);
                if (StringUtils.isNotBlank(appVersion)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.APP_VERSION_KEY, appVersion,
                                TheiaConstant.DataEnrichmentKeys.APP_VERSION_VAL_LENGTH)) {
                    envInfo.setAppVersion(appVersion);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.APP_VERSION_KEY, appVersion);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.APP_VERSION_KEY, envInfo.getAppVersion());
            }
            if (StringUtils.isBlank(envInfo.getBrowserType())) {
                String browserType = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.BROWSER_TYPE);
                if (StringUtils.isNotBlank(browserType)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.BROWSER_TYPE,
                                browserType, TheiaConstant.DataEnrichmentKeys.BROWSER_TYPE_VAL_LENGTH)) {
                    envInfo.setBrowserType(browserType);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.BROWSER_TYPE, browserType);
                }
            }
            if (StringUtils.isBlank(envInfo.getUserAgent())) {
                String browserUserAgent = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.USER_AGENT_KEY);
                if (StringUtils.isNotBlank(browserUserAgent)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.USER_AGENT_KEY,
                                browserUserAgent, TheiaConstant.DataEnrichmentKeys.USER_AGENT_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.USER_AGENT_KEY, browserUserAgent);
                    envInfo.setUserAgent(browserUserAgent);
                }
            }
            if (StringUtils.isNotBlank(envInfo.getClientIp())
                    && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.CLIENTIP_KEY,
                            envInfo.getClientIp(), TheiaConstant.DataEnrichmentKeys.CLIENTIP_VAL_LENGTH)) {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.CLIENTIP_KEY, envInfo.getClientIp());
                LOGGER.info("client Ip received from Client : {}",
                        riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.CLIENTIP_KEY));
            }
            if (StringUtils.isBlank(envInfo.getClientKey())) {
                String clientKey = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.CLIENTKEY_KEY);
                if (StringUtils.isNotBlank(clientKey)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.CLIENTKEY_KEY,
                                clientKey, TheiaConstant.DataEnrichmentKeys.CLIENTKEY_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.CLIENTKEY_KEY, clientKey);
                    envInfo.setClientKey(clientKey);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.CLIENTKEY_KEY, envInfo.getClientKey());
            }
            if (StringUtils.isBlank(envInfo.getBrowserVersion())) {
                String browserVersion = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.BROWSER_VERSION_KEY);
                if (StringUtils.isNotBlank(browserVersion)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.BROWSER_VERSION_KEY, browserVersion,
                                TheiaConstant.DataEnrichmentKeys.BROWSER_VERSION_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.BROWSER_VERSION_KEY, browserVersion);
                    envInfo.setBrowserVersion(browserVersion);
                }
            }
            if (StringUtils.isBlank(envInfo.getDeviceId())) {
                String deviceId = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.DEVICE_ID_KEY);
                if (StringUtils.isNotBlank(deviceId)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.DEVICE_ID_KEY,
                                deviceId, TheiaConstant.DataEnrichmentKeys.DEVICE_ID_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.DEVICE_ID_KEY, deviceId);
                    envInfo.setDeviceId(deviceId);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.DEVICE_ID_KEY, envInfo.getDeviceId());
            }
            if (StringUtils.isBlank(envInfo.getDeviceIMEI())) {
                String deviceIMEI = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.DEVICE_IMEI_KEY);
                if (StringUtils.isNotBlank(deviceIMEI)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.DEVICE_IMEI_KEY, deviceIMEI,
                                TheiaConstant.DataEnrichmentKeys.DEVICE_IMEI_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.DEVICE_IMEI_KEY, deviceIMEI);
                    envInfo.setDeviceIMEI(deviceIMEI);
                }
            }
            if (StringUtils.isBlank(envInfo.getDeviceManufacturer())) {
                String deviceManufacturer = riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.DEVICE_MANUFACTURER_KEY);
                if (StringUtils.isNotBlank(deviceManufacturer)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.DEVICE_MANUFACTURER_KEY, deviceManufacturer,
                                TheiaConstant.DataEnrichmentKeys.DEVICE_MANUFACTURER_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.DEVICE_MANUFACTURER_KEY, deviceManufacturer);
                    envInfo.setDeviceManufacturer(deviceManufacturer);
                }
            }
            if (StringUtils.isBlank(envInfo.getDeviceModel())) {
                String deviceModel = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.DEVICE_MODEL_KEY);
                if (StringUtils.isNotBlank(deviceModel)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.DEVICE_MODEL_KEY, deviceModel,
                                TheiaConstant.DataEnrichmentKeys.DEVICE_MODEL_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.DEVICE_MODEL_KEY, deviceModel);
                    envInfo.setDeviceModel(deviceModel);
                }
            }
            if (StringUtils.isBlank(envInfo.getDeviceType())) {
                String deviceType = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.DEVICE_TYPE_KEY);
                if (StringUtils.isNotBlank(deviceType)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.DEVICE_TYPE_KEY, deviceType,
                                TheiaConstant.DataEnrichmentKeys.DEVICE_TYPE_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.DEVICE_TYPE_KEY, deviceType);
                    envInfo.setDeviceType(deviceType);
                }
            }
            if (StringUtils.isBlank(envInfo.getGender())) {
                String gender = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.GENDER_KEY);
                if (StringUtils.isNotBlank(gender)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.GENDER_KEY,
                                gender, TheiaConstant.DataEnrichmentKeys.GENDER_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.GENDER_KEY, gender);
                    envInfo.setGender(gender);
                }
            }
            if (StringUtils.isBlank(envInfo.getHybridPlatform())) {
                String hybridPlatform = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_KEY);
                if (StringUtils.isNotBlank(hybridPlatform)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_KEY, hybridPlatform,
                                TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_KEY, hybridPlatform);
                    envInfo.setHybridPlatform(hybridPlatform);
                }
            }
            if (StringUtils.isBlank(envInfo.getHybridPlatformVersion())) {
                String hybridPlatformVersion = riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_VERSION_KEY);
                if (StringUtils.isNotBlank(hybridPlatformVersion)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_VERSION_KEY, hybridPlatformVersion,
                                TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_VERSION_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_VERSION_KEY, hybridPlatformVersion);
                    envInfo.setHybridPlatformVersion(hybridPlatformVersion);
                }
            }
            if (StringUtils.isBlank(envInfo.getICCIDNumber())) {
                String ICCIDNumber = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.ICCIDNUMBER_KEY);
                if (StringUtils.isNotBlank(ICCIDNumber)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.ICCIDNUMBER_KEY, ICCIDNumber,
                                TheiaConstant.DataEnrichmentKeys.ICCIDNUMBER_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.ICCIDNUMBER_KEY, ICCIDNumber);
                    envInfo.setICCIDNumber(ICCIDNumber);
                }
            }
            if (StringUtils.isBlank(envInfo.getLanguage())) {
                String language = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.LANGUAGE_KEY);
                if (StringUtils.isNotBlank(language)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.LANGUAGE_KEY,
                                language, TheiaConstant.DataEnrichmentKeys.LANGUAGE_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.LANGUAGE_KEY, language);
                    envInfo.setLanguage(language);
                }
            }
            if (StringUtils.isBlank(envInfo.getMerchantAppVersion())) {
                String merchantAppVersion = riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.MERCHANT_APP_VERSION_KEY);
                if (StringUtils.isNotBlank(merchantAppVersion)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.MERCHANT_APP_VERSION_KEY, merchantAppVersion,
                                TheiaConstant.DataEnrichmentKeys.MERCHANT_APP_VERSION_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.MERCHANT_APP_VERSION_KEY, merchantAppVersion);
                    envInfo.setMerchantAppVersion(merchantAppVersion);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.MERCHANT_APP_VERSION_KEY,
                        envInfo.getMerchantAppVersion());
            }
            if (StringUtils.isBlank(envInfo.getOrderOsType())) {
                String orderOsType = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.ORDER_OS_TYPE_KEY);
                if (StringUtils.isNotBlank(orderOsType)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.ORDER_OS_TYPE_KEY, orderOsType,
                                TheiaConstant.DataEnrichmentKeys.ORDER_OS_TYPE_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.ORDER_OS_TYPE_KEY, orderOsType);
                    envInfo.setOrderOsType(orderOsType);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.ORDER_OS_TYPE_KEY, envInfo.getOrderOsType());
            }
            if (StringUtils.isBlank(envInfo.getOrderTerminalId())) {
                String orderTerminalId = riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_ID_KEY);
                if (StringUtils.isNotBlank(orderTerminalId)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_ID_KEY, orderTerminalId,
                                TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_ID_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_ID_KEY, orderTerminalId);
                    envInfo.setOrderTerminalId(orderTerminalId);
                }
            }
            if (StringUtils.isBlank(envInfo.getOrderTerminalType())) {
                String orderTerminalType = riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_TYPE_KEY);
                if (StringUtils.isNotBlank(orderTerminalType)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_TYPE_KEY, orderTerminalType,
                                TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_TYPE_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_TYPE_KEY, orderTerminalType);
                    envInfo.setOrderTerminalType(orderTerminalType);
                }
            } else {
                extendInfo
                        .put(TheiaConstant.DataEnrichmentKeys.ORDER_TERMINAL_TYPE_KEY, envInfo.getOrderTerminalType());
            }
            if (StringUtils.isBlank(envInfo.getOsVersion())) {
                String osVersion = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.OS_VERSION_TYPE_KEY);
                if (StringUtils.isNotBlank(osVersion)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.OS_VERSION_TYPE_KEY, osVersion,
                                TheiaConstant.DataEnrichmentKeys.OS_VERSION_TYPE_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.OS_VERSION_TYPE_KEY, osVersion);
                    envInfo.setOsVersion(osVersion);
                }
            }
            if (StringUtils.isBlank(envInfo.getPlatform())) {
                String platform = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.PLATFORM_KEY);
                if (StringUtils.isNotBlank(platform)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.PLATFORM_KEY,
                                platform, TheiaConstant.DataEnrichmentKeys.PLATFORM_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.PLATFORM_KEY, platform);
                    envInfo.setPlatform(platform);
                }

            }
            String osType = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.OS_TYPE_KEY);
            if (StringUtils.isNotBlank(osType)
                    && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.OS_TYPE_KEY,
                            osType, TheiaConstant.DataEnrichmentKeys.OS_TYPE_VAL_LENGTH)) {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.OS_TYPE_KEY, osType);
                envInfo.setOsType(osType);
            }
            if (StringUtils.isBlank(envInfo.getRouterMac())) {
                String routerMac = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.ROUTER_MAC_KEY);
                if (StringUtils.isNotBlank(routerMac)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.ROUTER_MAC_KEY,
                                routerMac, TheiaConstant.DataEnrichmentKeys.ROUTER_MAC_VAL_LENGTH)) {
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.ROUTER_MAC_KEY, routerMac);
                    envInfo.setRouterMac(routerMac);
                }
            }
            if (StringUtils.isBlank(envInfo.getScreenResolution())) {
                String screenResolution = riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.SCREEN_RESOLUTION_KEY);
                if (StringUtils.isNotBlank(screenResolution)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.SCREEN_RESOLUTION_KEY, screenResolution,
                                TheiaConstant.DataEnrichmentKeys.SCREEN_RESOLUTION_VALUE_LENGTH)) {
                    envInfo.setScreenResolution(screenResolution);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.SCREEN_RESOLUTION_KEY, screenResolution);
                }
            }
            if (StringUtils.isBlank(envInfo.getSdkVersion())) {
                String sdkVersion = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.SDK_VERSION_KEY);
                if (StringUtils.isNotBlank(sdkVersion)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.SDK_VERSION_KEY, sdkVersion,
                                TheiaConstant.DataEnrichmentKeys.SDK_VERSION_VAL_LENGTH)) {
                    envInfo.setSdkVersion(sdkVersion);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.SDK_VERSION_KEY, sdkVersion);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.SDK_VERSION_KEY, envInfo.getSdkVersion());
            }
            if (StringUtils.isBlank(envInfo.getSessionId())) {
                String sessionId = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.SESSIONID_KEY);
                if (StringUtils.isNotBlank(sessionId)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.SESSIONID_KEY,
                                sessionId, TheiaConstant.DataEnrichmentKeys.SESSIONID_VAL_LENGTH)) {
                    envInfo.setSessionId(sessionId);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.SESSIONID_KEY, sessionId);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.SESSIONID_KEY, envInfo.getSessionId());
            }
            if (StringUtils.isBlank(envInfo.getTimeZone())) {
                String timeZone = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.TIMEZONE_KEY);
                if (StringUtils.isNotBlank(timeZone)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.TIMEZONE_KEY,
                                timeZone, TheiaConstant.DataEnrichmentKeys.TIMEZONE_VAL_LENGTH)) {
                    envInfo.setTimeZone(timeZone);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.TIMEZONE_KEY, timeZone);
                }
            }
            if (StringUtils.isBlank(envInfo.getTokenId())) {
                String tokenId = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.TOKENID_KEY);
                if (StringUtils.isNotBlank(tokenId)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.TOKENID_KEY,
                                tokenId, TheiaConstant.DataEnrichmentKeys.TOKENID_VAL_LENGTH)) {
                    envInfo.setTokenId(tokenId);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.TOKENID_KEY, tokenId);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.TOKENID_KEY, envInfo.getTokenId());
            }
            if (StringUtils.isBlank(envInfo.getUserAgent())) {
                String userAgent = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.USER_AGENT_KEY);
                if (StringUtils.isNotBlank(userAgent)
                        && BizParamValidator.validateInputStringLength(TheiaConstant.DataEnrichmentKeys.USER_AGENT_KEY,
                                userAgent, TheiaConstant.DataEnrichmentKeys.USER_AGENT_VAL_LENGTH)) {
                    envInfo.setUserAgent(userAgent);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.USER_AGENT_KEY, userAgent);
                }
            }
            if (StringUtils.isBlank(envInfo.getLatitude())) {
                String latitude = String.valueOf(riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.USER_LATITUDE_KEY));
                if (StringUtils.isNotBlank(latitude)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.USER_LATITUDE_KEY, latitude,
                                TheiaConstant.DataEnrichmentKeys.USER_LATITUDE_VAL_LENGTH)) {
                    envInfo.setLatitude(latitude);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.LATITUDE, latitude);
                }
            }
            if (StringUtils.isBlank(envInfo.getLongitude())) {
                String longitude = String.valueOf(riskExtendedInfoMap
                        .get(TheiaConstant.DataEnrichmentKeys.USER_LONGITUDE_KEY));
                if (StringUtils.isNotBlank(longitude)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.USER_LONGITUDE_KEY, longitude,
                                TheiaConstant.DataEnrichmentKeys.USER_LONGITUDE_VAL_LENGTH)) {
                    envInfo.setLongitude(longitude);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.LONGITUDE, longitude);
                }
            }
            if (StringUtils.isBlank(envInfo.getWebsiteLanguage())) {
                String websiteLanguage = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.WEBSITE_LANGUAGE_KEY);
                if (StringUtils.isNotBlank(websiteLanguage)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.WEBSITE_LANGUAGE_KEY, websiteLanguage,
                                TheiaConstant.DataEnrichmentKeys.WEBSITE_LANGUAGE_VAL_LENGTH)) {
                    envInfo.setWebsiteLanguage(websiteLanguage);
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.WEBSITE_LANGUAGE_KEY, websiteLanguage);
                }
            } else {
                extendInfo.put(TheiaConstant.DataEnrichmentKeys.WEBSITE_LANGUAGE_KEY, envInfo.getWebsiteLanguage());
            }
            if (StringUtils.isBlank(envInfo.getMerchantType())) {
                String merchantType = riskExtendedInfoMap.get(TheiaConstant.DataEnrichmentKeys.MERCHANT_TYPE_KEY);
                if (StringUtils.isNotBlank(merchantType)
                        && BizParamValidator.validateInputStringLength(
                                TheiaConstant.DataEnrichmentKeys.MERCHANT_TYPE_KEY, merchantType,
                                TheiaConstant.DataEnrichmentKeys.MERCHANT_TYPE_VAL_LENGTH)) {
                    envInfo.setMerchantType(merchantType.toUpperCase());
                    extendInfo.put(TheiaConstant.DataEnrichmentKeys.MERCHANT_TYPE_KEY, merchantType.toUpperCase());
                }
            }
            removeExternalRiskExtendedFields(riskExtendedInfoMap);

        } catch (Exception e) {
            LOGGER.error(
                    "DATA_ENRICHMENT : In EnvInfoUtil.setDataEnrichmentFields() , Error While Setting DataEnrichment Fields - {}",
                    e.getMessage());
        }
    }

    private static void removeExternalRiskExtendedFields(Map<String, String> riskExtendedInfoMap) {
        try {
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.BROWSER_TYPE);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.BROWSER_VERSION_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.DEVICE_MODEL_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.USER_AGENT_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.CLIENTKEY_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.DEVICE_ID_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.DEVICE_IMEI_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.DEVICE_MANUFACTURER_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.DEVICE_MODEL_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.DEVICE_TYPE_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.GENDER_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.HYBRID_PLATFORM_VERSION_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.ICCIDNUMBER_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.LANGUAGE_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.OS_VERSION_TYPE_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.SCREEN_RESOLUTION_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.TIMEZONE_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.LATITUDE);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.LONGITUDE);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.MERCHANT_TYPE_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.ROUTER_MAC_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.USER_LONGITUDE_KEY);
            riskExtendedInfoMap.remove(TheiaConstant.DataEnrichmentKeys.USER_LATITUDE_KEY);
        } catch (Exception e) {
            LOGGER.error("DATA_ENRICHMENT : error in removing fields from risk extend info :{}", e.getMessage());
        }
    }

    public static String getClientIPV2(HttpServletRequest request) {
        String ip = getIpAddressFromAkamai(request);
        if (StringUtils.isNotBlank(ip) && validateIpAddress(ip)) {
            return ip;
        }
        String forwardedFor = request.getHeader(TheiaConstant.ExtraConstants.X_FORWARDED_FOR);
        if (StringUtils.isNotBlank(forwardedFor)) {
            String ips[] = forwardedFor.split(",");
            forwardedFor = StringUtils.trim(ips[0]);
            if (StringUtils.isNotBlank(forwardedFor) && validateIpAddress(forwardedFor)) {
                return forwardedFor;
            }
        }
        if (StringUtils.isNotBlank(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return DEFAULT_CLIENT_IP;
    }

    private static String getIpAddressFromAkamai(HttpServletRequest request) {
        String ip = null;
        String ipHeader = request.getHeader(TheiaConstant.ExtraConstants.AKAMAI_IP_ADDRESS_HEADER);
        if (StringUtils.isNotEmpty(ipHeader)) {
            ip = StringUtils.trim(ipHeader);
        }
        return ip;

    }

    private static boolean validateIpAddress(String clientIp) {
        try {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            if (validator.isValidInet4Address(clientIp)) {
                return true;
            } else if (validator.isValidInet6Address(clientIp)) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("error while validating ip address :{}", e.getMessage());
        }
        return false;
    }
}
