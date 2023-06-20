package com.paytm.pgplus.theia.nativ.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Created by mohit.gupta
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpiMetaData implements Serializable {

    private static final long serialVersionUID = 4017564347373822857L;

    String profileStatus;

    String upiLinkedMobileNumber;

    boolean isDeviceBinded;

    public String getProfileStatus() {
        return profileStatus;
    }

    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }

    public String getUpiLinkedMobileNumber() {
        return upiLinkedMobileNumber;
    }

    public void setUpiLinkedMobileNumber(String upiLinkedMobileNumber) {
        this.upiLinkedMobileNumber = upiLinkedMobileNumber;
    }

    public boolean isDeviceBinded() {
        return isDeviceBinded;
    }

    public void setDeviceBinded(boolean deviceBinded) {
        isDeviceBinded = deviceBinded;
    }
}
