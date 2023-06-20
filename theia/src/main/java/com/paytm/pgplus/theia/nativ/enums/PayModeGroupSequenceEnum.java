package com.paytm.pgplus.theia.nativ.enums;

public enum PayModeGroupSequenceEnum {
    DEFAULT("PAYMODE_GROUP_PRIORITY_LIST"), SUBSCRIPTION("SUBSCRIPTION_PAYMODE_GROUP_PRIORITY_LIST"), NATIVE(
            "NATIVE_PAYMODE_GROUP_PRIORITY_LIST"), OFFLINE("OFFLINE_PAYMODE_GROUP_PRIORITY_LIST"), ENHANCE(
            "ENHANCE_PAYMODE_GROUP_PRIORITY_LIST");

    String preferenceName;

    PayModeGroupSequenceEnum(String preferenceName) {
        this.preferenceName = preferenceName;
    }

    public String getPreferenceName() {
        return this.preferenceName;
    }
}
