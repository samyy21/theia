/*
 * This File is the sole property of Paytm(One97 Communications Limited)
 */
package com.paytm.pgplus.biz.core.model.oauth;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.biz.core.model.CardBeanBiz;
import com.paytm.pgplus.cache.annotation.RedisEncrypt;
import com.paytm.pgplus.cache.annotation.RedisEncryptAttribute;
import com.paytm.pgplus.pgproxycommon.models.UserProfile;

@RedisEncrypt
public class UserDetailsBiz implements Serializable {

    private static final long serialVersionUID = 2L;

    @Mask(prefixNoMaskLen = 6, suffixNoMaskLen = 4)
    private String userToken;
    private String userId;
    @Mask(prefixNoMaskLen = 2, maskStr = "*", suffixNoMaskLen = 2)
    private String userName;
    private String email;
    @Mask(prefixNoMaskLen = 3, maskStr = "*", suffixNoMaskLen = 3)
    private String mobileNo;
    private String internalUserId;
    private String payerAccountNumber;
    @RedisEncryptAttribute
    private List<CardBeanBiz> merchantViewSavedCardsList;
    @RedisEncryptAttribute
    private List<CardBeanBiz> addAndPayViewSavedCardsList;
    private UserProfile userProfile;
    private boolean isKYC;
    private boolean paytmCCEnabled;
    private boolean savingsAccountRegistered;
    private String postpaidStatus;
    private List<String> userTypes;
    private String clientId;
    /**
     * This is done specifically for Corporate Advannce Deposit
     */
    private String childUserId;

    private Boolean consentForAutoDebitPref;
    private Boolean showConsentSheetAutoDebit;
    private Boolean capturePostpaidConsentForWalletTopUp;

    private Boolean isUserEligibileForPostPaidOnboarding;

    private String postpaidCreditLimit;

    private String postpaidOnboardingStageMsg;

    public Boolean getCapturePostpaidConsentForWalletTopUp() {
        return capturePostpaidConsentForWalletTopUp;
    }

    public void setCapturePostpaidConsentForWalletTopUp(Boolean capturePostpaidConsentForWalletTopUp) {
        this.capturePostpaidConsentForWalletTopUp = capturePostpaidConsentForWalletTopUp;
    }

    public Boolean getConsentForAutoDebitPref() {
        return consentForAutoDebitPref;
    }

    public void setConsentForAutoDebitPref(Boolean consentForAutoDebitPref) {
        this.consentForAutoDebitPref = consentForAutoDebitPref;
    }

    public Boolean getShowConsentSheetAutoDebit() {
        return showConsentSheetAutoDebit;
    }

    public void setShowConsentSheetAutoDebit(Boolean showConsentSheetAutoDebit) {
        this.showConsentSheetAutoDebit = showConsentSheetAutoDebit;
    }

    public String getPayerAccountNumber() {
        return payerAccountNumber;
    }

    public void setPayerAccountNumber(String payerAccountNumber) {
        this.payerAccountNumber = payerAccountNumber;
    }

    public List<CardBeanBiz> getMerchantViewSavedCardsList() {
        return merchantViewSavedCardsList == null ? Collections.emptyList() : merchantViewSavedCardsList;
    }

    public void setMerchantViewSavedCardsList(List<CardBeanBiz> merchantViewSavedCardsList) {
        this.merchantViewSavedCardsList = merchantViewSavedCardsList;
    }

    public List<CardBeanBiz> getAddAndPayViewSavedCardsList() {
        return addAndPayViewSavedCardsList == null ? Collections.emptyList() : addAndPayViewSavedCardsList;
    }

    public void setAddAndPayViewSavedCardsList(List<CardBeanBiz> addAndPayViewSavedCardsList) {
        this.addAndPayViewSavedCardsList = addAndPayViewSavedCardsList;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(final String mobileNo) {
        this.mobileNo = mobileNo;
    }

    /**
     * @return the internalUserId
     */
    public String getInternalUserId() {
        return internalUserId;
    }

    /**
     * @param internalUserId
     *            the internalUserId to set
     */
    public void setInternalUserId(final String internalUserId) {
        this.internalUserId = internalUserId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(final String userToken) {
        this.userToken = userToken;
    }

    /**
     * @return the userProfile
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    /**
     * @param userProfile
     *            the userProfile to set
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * @return the isKYC
     */
    public boolean isKYC() {
        return isKYC;
    }

    /**
     * @param isKYC
     *            the isKYC to set
     */
    public void setKYC(boolean isKYC) {
        this.isKYC = isKYC;
    }

    public boolean isPaytmCCEnabled() {
        return paytmCCEnabled;
    }

    public void setPaytmCCEnabled(boolean paytmCCEnabled) {
        this.paytmCCEnabled = paytmCCEnabled;
    }

    public boolean isSavingsAccountRegistered() {
        return savingsAccountRegistered;
    }

    public void setSavingsAccountRegistered(boolean savingsAccountRegistered) {
        this.savingsAccountRegistered = savingsAccountRegistered;
    }

    public String getPostpaidStatus() {
        return postpaidStatus;
    }

    public void setPostpaidStatus(String postpaidStatus) {
        this.postpaidStatus = postpaidStatus;
    }

    public List<String> getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(List<String> userTypes) {
        this.userTypes = userTypes;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getChildUserId() {
        return childUserId;
    }

    public void setChildUserId(String childUserId) {
        this.childUserId = childUserId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

    public Boolean getUserEligibileForPostPaidOnboarding() {
        return isUserEligibileForPostPaidOnboarding;
    }

    public void setUserEligibileForPostPaidOnboarding(Boolean userEligibileForPostPaidOnboarding) {
        isUserEligibileForPostPaidOnboarding = userEligibileForPostPaidOnboarding;
    }

    public String getPostpaidCreditLimit() {
        return postpaidCreditLimit;
    }

    public void setPostpaidCreditLimit(String postpaidCreditLimit) {
        this.postpaidCreditLimit = postpaidCreditLimit;
    }

    public String getPostpaidOnboardingStageMsg() {
        return postpaidOnboardingStageMsg;
    }

    public void setPostpaidOnboardingStageMsg(String postpaidOnboardingStageMsg) {
        this.postpaidOnboardingStageMsg = postpaidOnboardingStageMsg;
    }
}
