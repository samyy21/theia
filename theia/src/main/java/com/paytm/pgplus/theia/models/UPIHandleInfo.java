package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.localisationProcessor.annotation.LocaleField;

import java.io.Serializable;

/**
 * Created by: satyamsinghrajput at 30/7/19
 */
public class UPIHandleInfo implements Serializable {
    private static final long serialVersionUID = -760376517419753880L;

    @LocaleField
    private String upiAppName;
    private String upiImageName;

    public String getUpiAppName() {
        return upiAppName;
    }

    public void setUpiAppName(String upiAppName) {
        this.upiAppName = upiAppName;
    }

    public String getUpiImageName() {
        return upiImageName;
    }

    public void setUpiImageName(String upiImageName) {
        this.upiImageName = upiImageName;
    }

    public UPIHandleInfo(String upiAppName, String upiImageName) {
        this.upiAppName = upiAppName;
        this.upiImageName = upiImageName;
    }

    @Override
    public String toString() {
        return "UPIHandleInfo{" + "upiAppName='" + upiAppName + '\'' + ", upiImageName='" + upiImageName + '\'' + '}';
    }
}
