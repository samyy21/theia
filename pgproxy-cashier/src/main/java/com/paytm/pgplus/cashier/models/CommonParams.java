package com.paytm.pgplus.cashier.models;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class CommonParams {

    private static CommonParams commonParams;

    private CommonParams() {
    }

    private Set<Long> merchantList = new HashSet<Long>();
    private Set<Long> nbBankList = new HashSet<Long>();
    private Set<String> issuingBankList = new HashSet<String>();
    private Double nbMaxAmount = 0d;
    private Double cardMaxAmount = 0d;
    private Set<String> otpEnabledMerchants = new HashSet<String>();
    private Set<String> otpEnabledThemes = new HashSet<String>();
    private Set<String> commonLoginEnabledMerchants = new HashSet<String>();

    public static CommonParams getInstance() {
        if (commonParams == null) {
            synchronized (CommonParams.class) {
                if (commonParams == null) {
                    commonParams = new CommonParams();
                }
            }
        }
        return commonParams;
    }

    public void addMerchantToList(Long entityId) {
        merchantList.add(entityId);
    }

    public void addNBBankToList(Long bankId) {
        nbBankList.add(bankId);
    }

    public void clearMerchantList() {
        merchantList.clear();
    }

    public void clearOtpEnabledMerchantsList() {
        otpEnabledMerchants.clear();
    }

    public void clearOtpEnabledThemesList() {
        otpEnabledThemes.clear();
    }

    public void clearNBBankList() {
        nbBankList.clear();
    }

    public void clearCommonLoginMerchants() {
        commonLoginEnabledMerchants.clear();
    }

    public void addIssuingBankToList(String bankName) {
        issuingBankList.add(bankName);
    }

    public void addOTPEnabledMerchantToList(String mid) {
        otpEnabledMerchants.add(mid);
    }

    public void addOTPEnabledThemeToList(String mid) {
        otpEnabledThemes.add(mid);
    }

    public void addMerchantToCommonLoginMerchantList(String mid) {
        commonLoginEnabledMerchants.add(mid);
    }

    public void clearIssuingBankList() {
        issuingBankList.clear();
    }

    public boolean isThemeApplicableForPaymentOTP(String theme) {
        try {
            if (StringUtils.isNotBlank(theme)) {
                if (otpEnabledThemes.contains(theme)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isMerchantApplicableForCappedAmount(Long entityId) {
        try {
            if (entityId != null) {
                if (merchantList.contains(entityId)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isMerchantApplicableForPaymentOTP(String mid) {
        try {
            if (StringUtils.isNotBlank(mid)) {
                if (otpEnabledMerchants.contains(mid)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isBankApplicableForCappedNBAmount(Long bankId) {
        try {
            if (bankId != null) {
                if (nbBankList.contains(bankId)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isIssuingBankApplicableForCappedAmount(String bankName) {
        try {
            if (StringUtils.isNotBlank(bankName)) {
                if (issuingBankList.contains(bankName)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isMerchantEligibleForCommonLoginCheck(String mid) {
        try {
            if (StringUtils.isNotBlank(mid)) {
                if (commonLoginEnabledMerchants.contains(mid)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * @return the maxAmount
     */
    public Double getNBMaxAmount() {
        return nbMaxAmount;
    }

    /**
     * @param maxAmount
     *            the maxAmount to set
     */
    public void setNBMaxAmount(Double maxAmount) {
        this.nbMaxAmount = maxAmount;
    }

    /**
     *
     * @return
     */
    public Double getCardMaxAmount() {
        return cardMaxAmount;
    }

    /**
     *
     * @param cardMaxAmount
     */
    public void setCardMaxAmount(Double cardMaxAmount) {
        this.cardMaxAmount = cardMaxAmount;
    }

}
