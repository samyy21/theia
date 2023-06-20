package com.paytm.pgplus.theia.offline.enums;

import com.paytm.pgplus.common.enums.EPayMethod;

/**
 * Created by rahulverma on 13/10/17.
 */
public enum PassCodeType {
    PPBL(EPayMethod.PPBL), POSTPAID(EPayMethod.PAYTM_DIGITAL_CREDIT);

    private EPayMethod type;

    PassCodeType(EPayMethod type) {
        this.type = type;
    }
}
