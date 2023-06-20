/**
 * 
 */
package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * @createdOn 27-Mar-2016
 * @author kesari
 */
public final class TheiaRequestValidator {

    public static boolean isValidString(String data, int maxLength) {
        return isValidString(data, 0, maxLength);
    }

    public static boolean isValidString(String data, int minLength, int maxLength) {
        if (StringUtils.isBlank(data) || data.length() < minLength || data.length() > maxLength) {
            return false;
        }
        return true;
    }

    public static void validateAmountIfNotBlank(String txnAmountReq) throws TheiaDataMappingException {
        validateAmountIfNotBlank(txnAmountReq, false);
    }

    public static void validateAmountIfNotBlank(String txnAmountReq, boolean isSubscriptionRequestForWallet)
            throws TheiaDataMappingException {
        if (StringUtils.isBlank(txnAmountReq)) {
            return;
        }
        validateAmount(txnAmountReq, isSubscriptionRequestForWallet);
    }

    public static void validateAmountIfNotBlankForSubscription(String txnAmountReq) throws TheiaDataMappingException {
        if (StringUtils.isBlank(txnAmountReq)) {
            return;
        }
        validateAmountForSubscription(txnAmountReq);
    }

    public static void validateAmount(String txnAmountReq) throws TheiaDataMappingException {
        validateAmount(txnAmountReq, false);
    }

    public static void validateAmount(String txnAmountReq, boolean isSubscriptionRequestForWallet)
            throws TheiaDataMappingException {
        if (NumberUtils.isParsable(txnAmountReq)) {
            String[] amountSplit = txnAmountReq.split("\\.");
            if (amountSplit.length == 1) {
                validateMantissa(txnAmountReq, amountSplit, false);
                return;
            } else if (amountSplit.length == 2) {
                validateMantissa(txnAmountReq, amountSplit, isSubscriptionRequestForWallet);
                validateExponent(txnAmountReq, amountSplit);
                return;
            }
        }
        throwExceptionForAmount(txnAmountReq);
    }

    public static void validateAmountForSubscription(String txnAmountReq) throws TheiaDataMappingException {
        if (NumberUtils.isParsable(txnAmountReq)) {
            String[] amountSplit = txnAmountReq.split("\\.");
            if (amountSplit.length == 1) {
                validateMantissaForSubscription(txnAmountReq, amountSplit);
                return;
            } else if (amountSplit.length == 2) {
                validateMantissaForSubscription(txnAmountReq, amountSplit);
                validateExponent(txnAmountReq, amountSplit);
                return;
            }
        }
        throwExceptionForAmount(txnAmountReq);
    }

    private static void validateExponent(String txnAmountReq, String[] amountSplit) throws TheiaDataMappingException {
        if (amountSplit[1].length() > 2) {
            throwExceptionForAmount(txnAmountReq);
        }
    }

    private static void validateMantissa(String txnAmountReq, String[] amountSplit,
            boolean isSubscriptionRequestForWallet) throws TheiaDataMappingException {
        int mantissa = Integer.parseInt(amountSplit[0]);
        // To support 0 rs transaction in subscription | Subscription Validation
        // is being done in
        // "validateMantissaForSubscription(String txnAmountReq, String[] amountSplit)"
        // Now .
        /*
         * if (amountSplit.length == 1 && mantissa == 0) { return; }
         */
        if (mantissa < 1) {
            if (isSubscriptionRequestForWallet && amountSplit.length == 2) {
                int remainingNum = Integer.parseInt(amountSplit[1]);
                if (remainingNum > 0) {
                    return;
                }
            }
            throwExceptionForAmount(txnAmountReq);
        }
    }

    private static void validateMantissaForSubscription(String txnAmountReq, String[] amountSplit)
            throws TheiaDataMappingException {
        int mantissa = Integer.parseInt(amountSplit[0]);
        // To support 0 rs transaction in subscription and here wwe have added
        if (amountSplit.length == 1 && mantissa == 0) {
            return;
        }
        if (amountSplit.length == 2 && mantissa == 0 && Integer.parseInt(amountSplit[1]) == 0) {
            return;
        }
        if (mantissa < 1) {
            throwExceptionForAmount(txnAmountReq);
        }
    }

    public static void validateAmountIfNotBlankForLoyaltyPoint(String txnAmountReq) throws TheiaDataMappingException {
        if (StringUtils.isBlank(txnAmountReq)) {
            return;
        }
        validateAmountForLoyaltyPoint(txnAmountReq);
    }

    public static void validateAmountForLoyaltyPoint(String txnAmountReq) throws TheiaDataMappingException {
        if (NumberUtils.isParsable(txnAmountReq)) {
            String[] amountSplit = txnAmountReq.split("\\.");
            if (amountSplit.length == 1) {
                validateMantissaForLoyaltyPoint(txnAmountReq, amountSplit);
                return;
            } else if (amountSplit.length == 2) {
                validateMantissaForLoyaltyPoint(txnAmountReq, amountSplit);
                validateExponent(txnAmountReq, amountSplit);
                return;
            }
        }
        throwExceptionForAmount(txnAmountReq);
    }

    private static void validateMantissaForLoyaltyPoint(String txnAmountReq, String[] amountSplit)
            throws TheiaDataMappingException {
        int mantissa = Integer.parseInt(amountSplit[0]);
        if (amountSplit.length == 1 && mantissa > 0) {
            return;
        }
        if (amountSplit.length == 2 && mantissa >= 0) {
            if (mantissa < 1) {
                int remainingNum = Integer.parseInt(amountSplit[1]);
                if (remainingNum > 0) {
                    return;
                }
            } else {
                return;
            }
        }
        throwExceptionForAmount(txnAmountReq);
    }

    private static void throwExceptionForAmount(String txnAmountReq) throws TheiaDataMappingException {
        throw new TheiaDataMappingException(new StringBuilder(txnAmountReq).append(
                " is not a valid value for TxnAmount").toString(), ResponseConstants.INVALID_TXN_AMOUNT);
    }
}
