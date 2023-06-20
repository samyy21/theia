package com.paytm.pgplus.cashier.models;

import java.io.Serializable;
import java.util.Map;

import com.paytm.pgplus.facade.enums.TerminalType;

/**
 * @author amit.dubey
 *
 */
public class CashierEnvInfo implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -3370408182974118121L;

    private String sessionId;
    private String tokenId;
    private String websiteLanguage;
    private String clientIp;
    private String osType;
    private String appVersion;
    private String sdkVersion;
    private TerminalType terminalType;
    private String clientKey;
    private String orderTerminalType;
    private String orderOsType;
    private String merchantAppVersion;
    private Map<String, String> extentInfo;

    protected CashierEnvInfo() {
    }

    public CashierEnvInfo(final CashierEnvInfoBuilder cashierEnvInfoBuilder) {
        sessionId = cashierEnvInfoBuilder.sessionId;
        tokenId = cashierEnvInfoBuilder.tokenId;
        websiteLanguage = cashierEnvInfoBuilder.websiteLanguage;
        clientIp = cashierEnvInfoBuilder.clientIp;
        osType = cashierEnvInfoBuilder.osType;
        appVersion = cashierEnvInfoBuilder.appVersion;
        sdkVersion = cashierEnvInfoBuilder.sdkVersion;
        terminalType = cashierEnvInfoBuilder.terminalType;
        clientKey = cashierEnvInfoBuilder.clientKey;
        orderTerminalType = cashierEnvInfoBuilder.orderTerminalType;
        orderOsType = cashierEnvInfoBuilder.orderOsType;
        merchantAppVersion = cashierEnvInfoBuilder.merchantAppVersion;
        extentInfo = cashierEnvInfoBuilder.extentInfo;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @return the tokenId
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * @return the websiteLanguage
     */
    public String getWebsiteLanguage() {
        return websiteLanguage;
    }

    /**
     * @return the clientIp
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * @return the osType
     */
    public String getOsType() {
        return osType;
    }

    /**
     * @return the appVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @return the sdkVersion
     */
    public String getSdkVersion() {
        return sdkVersion;
    }

    /**
     * @return the terminalType
     */
    public TerminalType getTerminalType() {
        return terminalType;
    }

    /**
     * @return the clientKey
     */
    public String getClientKey() {
        return clientKey;
    }

    /**
     * @return the orderTerminalType
     */
    public String getOrderTerminalType() {
        return orderTerminalType;
    }

    /**
     * @return the orderOsType
     */
    public String getOrderOsType() {
        return orderOsType;
    }

    /**
     * @return the merchantAppVersion
     */
    public String getMerchantAppVersion() {
        return merchantAppVersion;
    }

    public Map<String, String> getExtentInfo() {
        return extentInfo;
    }

    /**
     * This class is the Builder class for the EnvInfo class
     *
     * @author vaishakh
     * @version 1.0
     *
     */
    public static class CashierEnvInfoBuilder {
        private String sessionId;
        private String tokenId;
        private String websiteLanguage;
        private String clientIp;
        private String osType;
        private String appVersion;
        private String sdkVersion;
        private TerminalType terminalType;
        private String clientKey;
        private String orderTerminalType;
        private String orderOsType;
        private String merchantAppVersion;
        private Map<String, String> extentInfo;

        public CashierEnvInfoBuilder(final String clientIp, final TerminalType terminalType) {
            this.clientIp = clientIp;
            this.terminalType = terminalType;
        }

        public CashierEnvInfoBuilder sessionId(final String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public CashierEnvInfoBuilder tokenId(final String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public CashierEnvInfoBuilder websiteLanguage(final String websiteLanguage) {
            this.websiteLanguage = websiteLanguage;
            return this;
        }

        public CashierEnvInfoBuilder osType(final String osType) {
            this.osType = osType;
            return this;
        }

        public CashierEnvInfoBuilder appVersion(final String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public CashierEnvInfoBuilder sdkVersion(final String sdkVersion) {
            this.sdkVersion = sdkVersion;
            return this;
        }

        public CashierEnvInfoBuilder clientKey(final String clientKey) {
            this.clientKey = clientKey;
            return this;
        }

        public CashierEnvInfoBuilder orderTerminalType(final String orderTerminalType) {
            this.orderTerminalType = orderTerminalType;
            return this;
        }

        public CashierEnvInfoBuilder orderOsType(final String orderOsType) {
            this.orderOsType = orderOsType;
            return this;
        }

        public CashierEnvInfoBuilder merchantAppVersion(final String merchantAppVersion) {
            this.merchantAppVersion = merchantAppVersion;
            return this;
        }

        public CashierEnvInfoBuilder extendInfo(final Map<String, String> extentInfo) {
            this.extentInfo = extentInfo;
            return this;
        }

        public CashierEnvInfo build() {
            return new CashierEnvInfo(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CashierEnvInfo[");
        sb.append("sessionId='").append(sessionId).append('\'');
        sb.append(", tokenId='").append(tokenId).append('\'');
        sb.append(", websiteLanguage='").append(websiteLanguage).append('\'');
        sb.append(", clientIp='").append(clientIp).append('\'');
        sb.append(", osType='").append(osType).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", sdkVersion='").append(sdkVersion).append('\'');
        sb.append(", terminalType=").append(terminalType);
        sb.append(", clientKey='").append(clientKey).append('\'');
        sb.append(", orderTerminalType='").append(orderTerminalType).append('\'');
        sb.append(", orderOsType='").append(orderOsType).append('\'');
        sb.append(", merchantAppVersion='").append(merchantAppVersion).append('\'');
        sb.append(", extentInfo=").append(extentInfo);
        sb.append(']');
        return sb.toString();
    }
}
