package com.paytm.pgplus.theia.nativ.model.consent;

import java.io.Serializable;

/**
 * Created by: satyamsinghrajput at 24/10/19
 */
public class NativeConsentResponseHead implements Serializable {

    private static final long serialVersionUID = -3941472856594617962L;
    private String version;
    private String timeStamp;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "NativeConsentResponseHead{" + "version='" + version + '\'' + ", timeStamp='" + timeStamp + '\'' + '}';
    }
}
