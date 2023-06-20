package com.paytm.pgplus.theia.taglibs;

import com.paytm.pgplus.theia.utils.ConfigurationUtil;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.Random;

public class PaytmTLD {

    private static String domainPrefix;
    private static String domainSuffix;
    private static String version;
    private static Integer staticDomainsInt;
    private static final SecureRandom random;

    static {
        loadStaticProperties();
        random = new SecureRandom();
    }

    public static void loadStaticProperties() {
        domainPrefix = ConfigurationUtil.getProperty("static.domain.prefix");
        domainSuffix = ConfigurationUtil.getProperty("static.domain.suffix", ".paytm.in/");
        version = ConfigurationUtil.getProperty("static.version", "1");
        String staticDomains = ConfigurationUtil.getProperty("static.domain.count", "4");
        staticDomainsInt = Integer.parseInt(staticDomains);
    }

    public static String getStaticRandomUrlPrefix() {
        if (StringUtils.isBlank(domainPrefix)) {
            // return StringUtils.EMPTY;
            return "resources/";
        }
        StringBuilder strBuilder = new StringBuilder(domainPrefix);
        Integer randomStaticDomainNo = random.nextInt(staticDomainsInt) + 1;
        strBuilder.append(randomStaticDomainNo).append(domainSuffix).append("/");
        strBuilder.append(ConfigurationUtil.getProperty("static.version", "1")).append("/");
        return strBuilder.toString();
    }

    public static String getStaticUrlPrefix() {
        if (StringUtils.isBlank(domainPrefix)) {
            // return StringUtils.EMPTY;
            return "resources/";
        }
        StringBuilder strBuilder = new StringBuilder(domainPrefix);
        Integer randomStaticDomainNo = random.nextInt(staticDomainsInt) + 1;
        strBuilder.append(randomStaticDomainNo).append(domainSuffix).append("/");
        // strBuilder.append(ConfigurationUtil.getProperty("static.version",
        // "1")).append("/");
        return strBuilder.toString();
    }

    public static String getStaticUrlPrefixV2() {
        if (StringUtils.isBlank(domainPrefix)) {
            return "resources/";
        }
        StringBuilder strBuilder = new StringBuilder(domainPrefix);
        strBuilder.append(domainSuffix).append("/");
        return strBuilder.toString();
    }

    public static String getScanPayUrlPrefix() {

        return ConfigurationUtil.getProperty("scannpay.socket.url");
    }
}