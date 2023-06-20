/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.request;

import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.customaspects.annotations.Mask;

import java.io.Serializable;

/**
 * @author manojpal
 *
 */
public class CacheCardRequestBean implements Serializable {

    private static final long serialVersionUID = -6281654029466892249L;

    private String cardNo;
    private String cardIndexNo;
    private String instNetworkType;
    private String instNetworkCode;
    private String cardType;
    private String holderName;
    private String holderMobileNo;
    private String instId;
    private String instBranchId;
    private String cvv2;
    private String otp;
    private String cardScheme;
    private Short expiryYear;
    private Short expiryMonth;
    @Mask
    private String unMaskedAccountNo;
    private boolean storeInCacheOnly;
    private String last4ref;
    private String globalPanIndex;
    private String par;
    private String uniqueCardReference;

    public CacheCardRequestBean(CacheCardRequestBeanBuilder builder) {
        this.cardNo = builder.cardNo;
        this.cardIndexNo = builder.cardIndexNo;
        this.instNetworkType = builder.instNetworkType;
        this.instNetworkCode = builder.instNetworkCode;
        this.cardType = builder.cardType;
        this.holderName = builder.holderName;
        this.holderMobileNo = builder.holderMobileNo;
        this.instId = builder.instId;
        this.instBranchId = builder.instBranchId;
        this.cvv2 = builder.cvv2;
        this.otp = builder.otp;
        this.cardScheme = builder.cardScheme;
        this.expiryYear = builder.expiryYear;
        this.expiryMonth = builder.expiryMonth;
        this.unMaskedAccountNo = builder.unMaskedAccountNo;
        this.last4ref = builder.last4ref;
        this.globalPanIndex = builder.globalPanIndex;
        this.par = builder.par;
        this.uniqueCardReference = builder.uniqueCardReference;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getCardIndexNo() {
        return cardIndexNo;
    }

    public String getInstNetworkType() {
        return instNetworkType;
    }

    public String getInstNetworkCode() {
        return instNetworkCode;
    }

    public String getCardType() {
        return cardType;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getHolderMobileNo() {
        return holderMobileNo;
    }

    public String getInstId() {
        return instId;
    }

    public String getInstBranchId() {
        return instBranchId;
    }

    public String getCvv2() {
        return cvv2;
    }

    public String getOtp() {
        return otp;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public Short getExpiryYear() {
        return expiryYear;
    }

    public Short getExpiryMonth() {
        return expiryMonth;
    }

    public String getUnMaskedAccountNo() {
        return unMaskedAccountNo;
    }

    public boolean isStoreInCacheOnly() {
        return storeInCacheOnly;
    }

    public void setStoreInCacheOnly(boolean storeInCacheOnly) {
        this.storeInCacheOnly = storeInCacheOnly;
    }

    public String getLast4ref() {
        return last4ref;
    }

    public void setLast4ref(String last4ref) {
        this.last4ref = last4ref;
    }

    public String getGlobalPanIndex() {
        return globalPanIndex;
    }

    public void setGlobalPanIndex(String globalPanIndex) {
        this.globalPanIndex = globalPanIndex;
    }

    public String getPar() {
        return par;
    }

    public void setPar(String par) {
        this.par = par;
    }

    public String getUniqueCardReference() {
        return uniqueCardReference;
    }

    public void setUniqueCardReference(String uniqueCardReference) {
        this.uniqueCardReference = uniqueCardReference;
    }

    public static class CacheCardRequestBeanBuilder {
        private String cardNo;
        private String cardIndexNo;
        private String instNetworkType;
        private String instNetworkCode;
        private String cardType;
        private String holderName;
        private String holderMobileNo;
        private String instId;
        private String instBranchId;
        private String cvv2;
        private String otp;
        private String cardScheme;
        private Short expiryYear;
        private Short expiryMonth;
        private String unMaskedAccountNo;
        private String last4ref;
        private String globalPanIndex;
        private String par;
        private String uniqueCardReference;

        // For Credit/Debit Card
        public CacheCardRequestBeanBuilder(final String cardNo, final String instNetworkType,
                final String instNetworkCode, String cardType, final String cvv2, final String cardScheme,
                final Short expiryYear, final Short expiryMonth) {
            this.cardNo = cardNo;
            this.instNetworkType = instNetworkType;
            this.instNetworkCode = instNetworkCode;
            this.cardType = cardType;
            this.cvv2 = cvv2;
            this.cardScheme = cardScheme;
            this.expiryYear = expiryYear;
            this.expiryMonth = expiryMonth;

        }

        public CacheCardRequestBeanBuilder(final String cardNo, final String instNetworkType,
                final String instNetworkCode, String cardType, final String cvv2, final String cardScheme,
                final Short expiryYear, final Short expiryMonth, final String globalPanIndex, final String par) {
            this.cardNo = cardNo;
            this.instNetworkType = instNetworkType;
            this.instNetworkCode = instNetworkCode;
            this.cardType = cardType;
            this.cvv2 = cvv2;
            this.cardScheme = cardScheme;
            this.expiryYear = expiryYear;
            this.expiryMonth = expiryMonth;
            this.globalPanIndex = globalPanIndex;
            this.par = par;
        }

        // For IMPS
        public CacheCardRequestBeanBuilder(final String cardNo, final String instNetworkType,
                final String instNetworkCode, final String otp, final String holderMobileNo) {
            this.cardNo = cardNo;
            this.instNetworkType = instNetworkType;
            this.instNetworkCode = instNetworkCode;
            this.otp = otp;
            this.holderMobileNo = holderMobileNo;
        }

        // For Stock Trading Flow
        public CacheCardRequestBeanBuilder(final String cardNo, final String instId, final String instNetworkType) {
            this.cardNo = cardNo;
            this.instId = instId;
            this.instNetworkType = instNetworkType;
        }

        // For Mandate Flow
        public CacheCardRequestBeanBuilder(final String accountNo, final String issuingBankCode, final String ifscCode,
                final String holderName) {
            this.cardNo = accountNo;
            this.instId = issuingBankCode;
            this.instNetworkCode = ifscCode;
            this.holderName = holderName;
        }

        public CacheCardRequestBean build() {
            return new CacheCardRequestBean(this);
        }

        public CacheCardRequestBeanBuilder cardNo(final String cardNo) {
            this.cardNo = cardNo;
            return this;

        }

        public CacheCardRequestBeanBuilder cardIndexNo(final String cardIndexNo) {
            this.cardIndexNo = cardIndexNo;
            return this;
        }

        public CacheCardRequestBeanBuilder instNetworkType(final String instNetworkType) {
            this.instNetworkType = instNetworkType;
            return this;
        }

        public CacheCardRequestBeanBuilder instNetworkCode(final String instNetworkCode) {
            this.instNetworkCode = instNetworkCode;
            return this;
        }

        public CacheCardRequestBeanBuilder cardType(final String cardType) {
            this.cardType = cardType;
            return this;
        }

        public CacheCardRequestBeanBuilder holderName(final String holderName) {
            this.holderName = holderName;
            return this;
        }

        public CacheCardRequestBeanBuilder holderMobileNo(final String holderMobileNo) {
            this.holderMobileNo = holderMobileNo;
            return this;
        }

        public CacheCardRequestBeanBuilder instId(final String instId) {
            this.instId = instId;
            return this;
        }

        public CacheCardRequestBeanBuilder instBranchId(final String instBranchId) {
            this.instBranchId = instBranchId;
            return this;
        }

        public CacheCardRequestBeanBuilder cvv2(final String cvv2) {
            this.cvv2 = cvv2;
            return this;
        }

        public CacheCardRequestBeanBuilder otp(final String otp) {
            this.otp = otp;
            return this;
        }

        public CacheCardRequestBeanBuilder cardScheme(final String cardScheme) {
            this.cardScheme = cardScheme;
            return this;
        }

        public CacheCardRequestBeanBuilder expiryYear(final Short expiryYear) {
            this.expiryYear = expiryYear;
            return this;
        }

        public CacheCardRequestBeanBuilder expiryMonth(final Short expiryMonth) {
            this.expiryMonth = expiryMonth;
            return this;
        }

        public CacheCardRequestBeanBuilder unMaskedAccountNo(final String unMaskedAccountNo) {
            this.unMaskedAccountNo = unMaskedAccountNo;
            return this;
        }

        public CacheCardRequestBeanBuilder last4ref(final String last4ref) {
            this.last4ref = last4ref;
            return this;
        }

        public CacheCardRequestBeanBuilder globalPanIndex(final String globalPanIndex) {
            this.globalPanIndex = globalPanIndex;
            return this;
        }

        public CacheCardRequestBeanBuilder par(final String par) {
            this.par = par;
            return this;
        }

        public CacheCardRequestBeanBuilder uniqueCardReference(final String uniqueCardReference) {
            this.uniqueCardReference = uniqueCardReference;
            return this;
        }

    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
