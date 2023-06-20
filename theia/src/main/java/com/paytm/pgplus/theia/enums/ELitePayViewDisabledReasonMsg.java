package com.paytm.pgplus.theia.enums;

import com.paytm.pgplus.biz.utils.ConfigurationUtil;
import com.paytm.pgplus.theia.nativ.model.payview.response.StatusInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ELitePayViewDisabledReasonMsg {

    CHANNEL_NOT_AVAILABLE("CHANNEL_NOT_AVAILABLE",
            "We are currently having difficulty in processing your payment. Please try with some other paymode/card.",
            "lpv.channel.not.available.msg");

    private String disabledReason;
    private String defaultMsg;
    private String propertyKey;

    public String getMessage() {
        return ConfigurationUtil.getProperty(this.propertyKey, this.defaultMsg);
    }

    public String getDisabledReason() {
        return this.disabledReason;
    }

    public String getPropertyKey() {
        return this.propertyKey;
    }

    public static void updateDisplayMessage(StatusInfo statusInfo) {
        for (ELitePayViewDisabledReasonMsg eLitePayViewDisabledReasonMsg : values()) {
            if (eLitePayViewDisabledReasonMsg.disabledReason.equals(statusInfo.getMsg())) {
                statusInfo.setShowDisabled(Boolean.TRUE);
                statusInfo.setDisplayMsg(eLitePayViewDisabledReasonMsg.getMessage());
                break;
            }
        }
    }

}
