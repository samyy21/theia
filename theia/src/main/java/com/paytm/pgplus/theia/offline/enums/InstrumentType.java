package com.paytm.pgplus.theia.offline.enums;

import com.paytm.pgplus.common.enums.EPayMethod;

/**
 * Created by rahulverma on 1/9/17.
 */
public enum InstrumentType {

    CC("CC", EPayMethod.CREDIT_CARD.getMethod()), DC("DC", EPayMethod.DEBIT_CARD.getMethod()), NB("NB",
            EPayMethod.NET_BANKING.getMethod()), NB_TOP5("NB_TOP5", EPayMethod.NET_BANKING.getMethod()), UPI("UPI",
            EPayMethod.UPI.getMethod()), WALLET("WALLET", EPayMethod.BALANCE.getMethod()), DIGITAL_CREDIT(
            "DIGITAL_CREDIT", EPayMethod.PAYTM_DIGITAL_CREDIT.getMethod()), IMPS("IMPS", EPayMethod.IMPS.getMethod()), ATM(
            "ATM", EPayMethod.ATM.getMethod()), EMI("EMI", EPayMethod.EMI.getMethod()), COD("COD", EPayMethod.COD
            .getMethod()), MP_COD("MP_COD", EPayMethod.MP_COD.getMethod()), PPBL("PPBL", EPayMethod.PPBL.getMethod()), BANK_MANDATE(
            "BANK_MANDATE", EPayMethod.BANK_MANDATE.getMethod()), ALL("ALL", "ALL"), BANK_TRANSFER("BANK_TRANSFER",
            EPayMethod.BANK_TRANSFER.getMethod());

    private String type;
    private String payMethod;

    InstrumentType(String type, String payMethod) {
        this.type = type;
        this.payMethod = payMethod;
    }

    public EPayMethod getEPayMethod() {
        return EPayMethod.getPayMethodByMethod(payMethod);
    }

    public static InstrumentType instrumentTypeByEPayMethod(EPayMethod payMethod) {
        for (InstrumentType instrumentType : values()) {
            if (payMethod != null && instrumentType.getEPayMethod() == payMethod)
                return instrumentType;
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getPayMethod() {
        return payMethod;
    }
}
