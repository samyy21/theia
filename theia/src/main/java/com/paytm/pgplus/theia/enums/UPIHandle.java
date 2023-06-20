package com.paytm.pgplus.theia.enums;

/**
 * Created by: satyamsinghrajput at 17/7/19
 */
public enum UPIHandle {
    PHONEPE("ybl", "PhonePe", "phonepe"), PAYTM("paytm", "Paytm", "paytm"), GOOGLE_PAY("ok", "Google Pay", "google"), GOOGLE_PAY_AXIS(
            "okaxis", "Google Pay", "google"), GOOGLE_PAY_HDFC("okhdfcbank", "Google Pay", "google"), GOOGLE_PAY_ICICI(
            "okicici", "Google Pay", "google"), GOOGLE_PAY_SBI("oksbi", "Google Pay", "google"), BHIM("upi", "BHIM",
            "bhim"), DEFAULT("defaut", "UPI Linked Bank/ UPI", "upi_default");

    private String handle;
    private String appName;
    private String upiImageName;

    UPIHandle(String handle, String appName, String upiImage) {
        this.handle = handle;
        this.appName = appName;
        this.upiImageName = upiImage;
    }

    public String getHandle() {
        return handle;
    }

    public String getAppName() {
        return appName;
    }

    public String getUpiImageName() {
        return upiImageName;
    }
}
