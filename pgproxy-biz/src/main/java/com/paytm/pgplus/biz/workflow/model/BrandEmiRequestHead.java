package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

/**
 * @author harshwardhan
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrandEmiRequestHead implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7707870314747806074L;

    private String clientId;
    private String clientIp;
    private String dataKsn;
    private String mac;
    private String macKsn;
    private String firmwareVersion;
    private String osVersion;
    private String version;
    @Mask(prefixNoMaskLen = 2, suffixNoMaskLen = 2)
    private String clientSecret;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setDataKsn(String dataKsn) {
        this.dataKsn = dataKsn;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setMacKsn(String macKsn) {
        this.macKsn = macKsn;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getDataKsn() {
        return dataKsn;
    }

    public String getMac() {
        return mac;
    }

    public String getMacKsn() {
        return macKsn;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
