package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.facade.common.interfaces.Builder;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import org.apache.commons.lang3.StringUtils;

public class UserAgentInfo {

    public static final String deviceAndroid = "Android";
    public static final String deviceIos = "Ios";

    private String browser;

    private String browserType;

    private String browserMajorVersion;

    private String platform;

    private String platformVersion;

    private String deviceType;

    private UserAgentInfo(Builder builder) {
        super();
        this.browser = builder.browser;
        this.browserType = builder.browserType;
        this.browserMajorVersion = builder.browserMajorVersion;
        this.platform = builder.platform;
        this.platformVersion = builder.platformVersion;
        this.deviceType = builder.deviceType;
    }

    public boolean detectAndroid() {
        return StringUtils.containsIgnoreCase(platform, deviceAndroid);
    }

    public boolean detectIos() {
        return StringUtils.containsIgnoreCase(platform, deviceIos);
    }

    public static class Builder implements com.paytm.pgplus.facade.common.interfaces.Builder {
        private String browser;
        private String browserType;
        private String browserMajorVersion;
        private String platform;
        private String platformVersion;
        private String deviceType;

        public Builder browser(String browser) {
            this.browser = browser;
            return this;
        }

        public Builder browserType(String browserType) {
            this.browserType = browserType;
            return this;
        }

        public Builder browserMajorVersion(String browserMajorVersion) {
            this.browserMajorVersion = browserMajorVersion;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder platformVersion(String platformVersion) {
            this.browser = browser;
            return this;
        }

        public Builder deviceType(String deviceType) {
            this.browser = browser;
            return this;
        }

        @Override
        public UserAgentInfo build() throws FacadeCheckedException {
            return new UserAgentInfo(this);
        }
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserType() {
        return browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public String getBrowserMajorVersion() {
        return browserMajorVersion;
    }

    public void setBrowserMajorVersion(String browserMajorVersion) {
        this.browserMajorVersion = browserMajorVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
