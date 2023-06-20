package com.paytm.pgplus.theia.dynamicWrapper.enums;

public enum PaymentCollectionTypes {

    Premium("Premium Payment", "01"), Loan("loan repayment/loan interest payment ", "02"), Advance("Advance Premium",
            "03"), Topup("TOP-UP Payment", "04"), Proposal("Proposal Deposit", "05"), Miscellaneous(
            "Miscellaneous Deposit", "06");

    private String name;
    private String value;

    PaymentCollectionTypes(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static String findValueByName(String name) {
        for (final PaymentCollectionTypes paymentCollectionType : PaymentCollectionTypes.values()) {
            if (name.equals(paymentCollectionType.name)) {
                return paymentCollectionType.value;
            }
        }
        return null;
    }

    public static String findNameByValue(String value) {
        for (final PaymentCollectionTypes paymentCollectionType : PaymentCollectionTypes.values()) {
            if (value.equals(paymentCollectionType.value)) {
                return paymentCollectionType.name;
            }
        }
        return null;
    }
}
