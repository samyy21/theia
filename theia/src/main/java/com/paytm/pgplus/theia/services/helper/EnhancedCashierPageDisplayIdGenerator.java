package com.paytm.pgplus.theia.services.helper;

public class EnhancedCashierPageDisplayIdGenerator {

    private int displayId;

    public int generateDisplayId() {
        displayId++;
        return displayId;
    }
}
