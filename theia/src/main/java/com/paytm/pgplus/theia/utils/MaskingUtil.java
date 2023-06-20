package com.paytm.pgplus.theia.utils;

import static com.paytm.pgplus.facade.constants.FacadeConstants.REGEX_FOR_FLOAT;

/**
 * Created by Anjali on 05/05/21.
 */
public final class MaskingUtil {

    public static final String maskPayload(Object payload) {
        return filter(payload.toString());
    }

    private static String filter(String payload) {
        return payload
                .replaceAll("\\\\\"userEmail\\\\\":\\\\\"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\\\\\"",
                        "\\\\\"userEmail\\\\\":\\\\\"AAA@BBB.CCC\\\\\"")
                .replaceAll("merchantContactNo:([0-9]*)([0-9]{4})", "merchantContactNo:" + "XXXXXX" + "$2")
                .replaceAll("MOBILE_NO:([0-9]*)([0-9]{4})", "MOBILE_NO:" + "XXXXXX" + "$2")
                .replaceAll("\\\\\"email\\\\\":\\\\\"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\\\\\"",
                        "\\\\\"email\\\\\":\\\\\"XXX@YYY.ZZZ\\\\\"")
                .replaceAll("\\\\\"userMobile\\\\\":\\\\\"[0-9]*\\\\\"",
                        "\\\\\"userMobile\\\\\":\\\\\"XXXXXXXXXX\\\\\"")
                .replaceAll("\"custParam2\":\"([0-9]{2})([0-9]*)([0-9]{4})\"",
                        "\"custParam2\":\"" + "$1" + "*******" + "$2\"")
                .replaceAll("\"beneficiaryAccount\":\"([0-9]{4})([0-9]*)([0-9]{4})\"",
                        "\"beneficiaryAccount\":\"" + "$1" + "*********" + "$2\"")
                .replaceAll("\"subwalletGuid\":\"([0-9A-Za-z]{4})([A-Za-z0-9]+)([A-Za-z0-9]{4})\"",
                        "\"subwalletGuid\":\"" + "$1" + "*********" + "$2\"")
                .replaceAll("\\\\\"phoneNo\\\\\":\\\\\"[0-9]*\\\\\"", "\\\\\"phoneNo\\\\\":\\\\\"YYYYYYYYYY\\\\\"")
                .replaceAll("\\\\\"phoneNumber\\\\\":\\\\\"[0-9]*\\\\\"", "\\\\\"phoneNo\\\\\":\\\\\"YYYYYYYYYY\\\\\"")
                .replaceAll("\"BANK_ACC_NO\":\"[0-9]*\"", "\"BANK_ACC_NO\":\"*********\"")
                .replaceAll("\"bankAccountNumber\":\"[0-9]*\"", "\"bankAccountNumber\":\"*********\"")
                .replaceAll("\\\"effectiveBalance\\\":" + REGEX_FOR_FLOAT, "\\\"effectiveBalance\\\":\\\"YYYY\\\"")
                .replaceAll("\\\"slfdBalance\\\":" + REGEX_FOR_FLOAT, "\\\"slfdBalance\\\":\\\"YYYY\\\"")
                .replaceAll("SSO_TOKEN.[a-zA-Z0-9-]+", "SSO_TOKEN|******")
                .replaceAll("cardCacheToken(?<a>(...\\d{6}))(?<b>\\d*)(?<c>\\d{4})\\|..........",
                        "cardCacheToken${a}\\*\\*\\*\\*\\*\\*${c}|\\*\\*\\*|\\*\\*\\*\\*\\*\\*")
                .replaceAll("account(?<a>(...))\\d*(?<b>(\\d{4}\"))", "account${a}\\*\\*\\*\\*\\*\\*${b}")
                .replaceAll("accRefNumber(?<a>(...))\\d*(?<b>(\\d{4}\"))", "accRefNumber${a}\\*\\*\\*\\*\\*\\*${b}");
    }

}
