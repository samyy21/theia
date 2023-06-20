package com.paytm.pgplus.theia.sessiondata;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.dyuproject.protostuff.Tag;
import com.paytm.pgplus.biz.enums.SubsTypes;
import com.paytm.pgplus.common.model.ConsultDetails;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;

/**
 * @createdOn 28-Mar-2016
 * @author kesari
 */
public class TransactionConfig implements Serializable {

    private static final long serialVersionUID = -8307965049652467727L;

    @Tag(value = 1)
    private boolean codHybridAllowed;
    @Tag(value = 2)
    private boolean chequeDDNeftEnabled;
    @Tag(value = 3)
    private boolean addMoneyFlag;

    // TODO Not used anywhwre
    @Tag(value = 4)
    private boolean subscriptionTxn;

    @Tag(value = 5)
    private boolean offlineEnabled;
    @Tag(value = 6)
    private boolean saveCardMandatory;
    @Tag(value = 7)
    private List<ChannelInfo> configuredChannels;
    @Tag(value = 8)
    private boolean uiEventLogToConsole;
    @Tag(value = 9)
    private boolean uiEventLogToServer;

    // For Cashier
    @Tag(value = 10)
    private boolean chargePayer;

    // private List<ChannelInfo> filteredConfiguredChannels;
    @Tag(value = 11)
    private int retryCount;
    // TODO Remove as hybridTxnAllowed is present
    @Tag(value = 12)
    private boolean hybridAllowed;
    @Tag(value = 13)
    private boolean addAndPayAllowed;

    // Aquiring or AddMoney -- MayBe for Cashier
    @Tag(value = 14)
    private String txnType;

    @Tag(value = 15)
    private String commMode;

    @Tag(value = 16)
    private EnvInfoRequestBean envInfo;

    /*
     * This is computed from ConvenienceFee from Alipay+
     */
    @Tag(value = 17)
    private Map<String, ConsultDetails> paymentCharges;
    @Tag(value = 18)
    private Map<String, String> paymentChargesInfo;

    /*
     * When only BALANCE comes from Consult from biz
     */
    @Tag(value = 19)
    private boolean walletOnly;

    @Tag(value = 20)
    private String paymentModeOnly;
    @Tag(value = 21)
    private List<String> allowedPayModes;
    @Tag(value = 22)
    private List<String> disallowedPayModes;
    @Tag(value = 23)
    private String productCode;
    @Tag(value = 24)
    private SubsTypes subsTypes;
    @Tag(value = 25)
    private String minAmountForAddMoneyVoucher;

    @Tag(value = 26)
    private boolean onTheFlyKYCRequired;

    @Tag(value = 27)
    private boolean dynamicQR2FA;

    public EnvInfoRequestBean getEnvInfo() {
        return envInfo;
    }

    public void setEnvInfo(EnvInfoRequestBean envInfo) {
        this.envInfo = envInfo;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public boolean isAddAndPayAllowed() {
        return addAndPayAllowed;
    }

    public void setAddAndPayAllowed(boolean addAndPayAllowed) {
        this.addAndPayAllowed = addAndPayAllowed;
    }

    /*
     * public List<ChannelInfo> getFilteredConfiguredChannels() { return
     * filteredConfiguredChannels; }
     * 
     * public void setFilteredConfiguredChannels(List<ChannelInfo>
     * filteredConfiguredChannels) { this.filteredConfiguredChannels =
     * filteredConfiguredChannels; }
     */

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isHybridAllowed() {
        return hybridAllowed;
    }

    public void setHybridAllowed(boolean hybridAllowed) {
        this.hybridAllowed = hybridAllowed;
    }

    public String getCommMode() {
        return commMode;
    }

    public void setCommMode(String commMode) {
        this.commMode = commMode;
    }

    public Map<String, ConsultDetails> getPaymentCharges() {
        return paymentCharges;
    }

    public void setPaymentCharges(Map<String, ConsultDetails> paymentCharges) {
        this.paymentCharges = paymentCharges;
    }

    public boolean isWalletOnly() {
        return walletOnly;
    }

    public void setWalletOnly(boolean walletOnly) {
        this.walletOnly = walletOnly;
    }

    public boolean isChargePayer() {
        return chargePayer;
    }

    public void setChargePayer(boolean chargePayer) {
        this.chargePayer = chargePayer;
    }

    /*
     * public boolean isItzEnabled() { return itzEnabled; }
     * 
     * public void setItzEnabled(boolean itzEnabled) { this.itzEnabled =
     * itzEnabled; }
     * 
     * public boolean isOfflineEnabled() { return offlineEnabled; }
     * 
     * public void setOfflineEnabled(boolean offlineEnabled) {
     * this.offlineEnabled = offlineEnabled; }
     */

    public boolean isCodHybridAllowed() {
        return codHybridAllowed;
    }

    public void setCodHybridAllowed(boolean codHybridAllowed) {
        this.codHybridAllowed = codHybridAllowed;
    }

    public boolean isChequeDDNeftEnabled() {
        return chequeDDNeftEnabled;
    }

    public void setChequeDDNeftEnabled(boolean chequeDDNeftEnabled) {
        this.chequeDDNeftEnabled = chequeDDNeftEnabled;
    }

    public boolean isAddMoneyFlag() {
        return addMoneyFlag;
    }

    public void setAddMoneyFlag(boolean addMoneyFlag) {
        this.addMoneyFlag = addMoneyFlag;
    }

    /*
     * public String getMerchantImage() { return merchantImage; }
     * 
     * public void setMerchantImage(String merchantImage) { this.merchantImage =
     * merchantImage; }
     */

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public boolean isSubscriptionTxn() {
        return subscriptionTxn;
    }

    public void setSubscriptionTxn(boolean isSubscriptionTxn) {
        this.subscriptionTxn = isSubscriptionTxn;
    }

    public List<ChannelInfo> getConfiguredChannels() {
        return configuredChannels;
    }

    public void setConfiguredChannels(List<ChannelInfo> configuredChannels) {
        this.configuredChannels = configuredChannels;
    }

    public boolean isSaveCardMandatory() {
        return saveCardMandatory;
    }

    public void setSaveCardMandatory(boolean saveCardMandatory) {
        this.saveCardMandatory = saveCardMandatory;
    }

    /**
     * @return the offlineEnabled
     */
    public boolean isOfflineEnabled() {
        return offlineEnabled;
    }

    /**
     * @param offlineEnabled
     *            the offlineEnabled to set
     */
    public void setOfflineEnabled(boolean offlineEnabled) {
        this.offlineEnabled = offlineEnabled;
    }

    /**
     * @return the paymentModeOnly
     */
    public String getPaymentModeOnly() {
        return paymentModeOnly;
    }

    /**
     * @param paymentModeOnly
     *            the paymentModeOnly to set
     */
    public void setPaymentModeOnly(String paymentModeOnly) {
        this.paymentModeOnly = paymentModeOnly;
    }

    /**
     * @return the allowedPayModes
     */
    public List<String> getAllowedPayModes() {
        return allowedPayModes != null ? allowedPayModes : Collections.emptyList();
    }

    /**
     * @param allowedPayModes
     *            the allowedPayModes to set
     */
    public void setAllowedPayModes(List<String> allowedPayModes) {
        this.allowedPayModes = allowedPayModes;
    }

    /**
     * @return the disallowedPayModes
     */
    public List<String> getDisallowedPayModes() {
        return disallowedPayModes != null ? disallowedPayModes : Collections.emptyList();
    }

    /**
     * @param disallowedPayModes
     *            the disallowedPayModes to set
     */
    public void setDisallowedPayModes(List<String> disallowedPayModes) {
        this.disallowedPayModes = disallowedPayModes;
    }

    /**
     * @return the productCode
     */
    public String getProductCode() {
        return productCode;
    }

    /**
     * @param productCode
     *            the productCode to set
     */
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    /**
     * @return the paymentChargesInfo
     */
    public Map<String, String> getPaymentChargesInfo() {
        return paymentChargesInfo;
    }

    /**
     * @param paymentChargesInfo
     *            the paymentChargesInfo to set
     */
    public void setPaymentChargesInfo(Map<String, String> paymentChargesInfo) {
        this.paymentChargesInfo = paymentChargesInfo;
    }

    /**
     * @return the uiEventLogToConsole
     */
    public boolean isUiEventLogToConsole() {
        return uiEventLogToConsole;
    }

    /**
     * @param uiEventLogToConsole
     *            the uiEventLogToConsole to set
     */
    public void setUiEventLogToConsole(boolean uiEventLogToConsole) {
        this.uiEventLogToConsole = uiEventLogToConsole;
    }

    /**
     * @return the uiEventLogToServer
     */
    public boolean isUiEventLogToServer() {
        return uiEventLogToServer;
    }

    /**
     * @param uiEventLogToServer
     *            the uiEventLogToServer to set
     */
    public void setUiEventLogToServer(boolean uiEventLogToServer) {
        this.uiEventLogToServer = uiEventLogToServer;
    }

    /**
     * @return the subsTypes
     */
    public SubsTypes getSubsTypes() {
        return subsTypes;
    }

    /**
     * @param subsTypes
     *            the subsTypes to set
     */
    public void setSubsTypes(SubsTypes subsTypes) {
        this.subsTypes = subsTypes;
    }

    public boolean isOnTheFlyKYCRequired() {
        return onTheFlyKYCRequired;
    }

    public void setOnTheFlyKYCRequired(boolean onTheFlyKYCRequired) {
        this.onTheFlyKYCRequired = onTheFlyKYCRequired;
    }

    /**
     * @return the minAmountForAddMoneyVoucher
     */
    public String getMinAmountForAddMoneyVoucher() {
        return minAmountForAddMoneyVoucher;
    }

    /**
     * @param minAmountForAddMoneyVoucher
     *            the minAmountForAddMoneyVoucher to set
     */
    public void setMinAmountForAddMoneyVoucher(String minAmountForAddMoneyVoucher) {
        this.minAmountForAddMoneyVoucher = minAmountForAddMoneyVoucher;
    }

    public boolean isDynamicQR2FA() {
        return dynamicQR2FA;
    }

    public void setDynamicQR2FA(boolean dynamicQR2FA) {
        this.dynamicQR2FA = dynamicQR2FA;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionConfig{");
        sb.append("addAndPayAllowed=").append(addAndPayAllowed);
        sb.append(", addMoneyFlag=").append(addMoneyFlag);
        sb.append(", allowedPayModes=").append(allowedPayModes);
        sb.append(", chargePayer=").append(chargePayer);
        sb.append(", chequeDDNeftEnabled=").append(chequeDDNeftEnabled);
        sb.append(", codHybridAllowed=").append(codHybridAllowed);
        sb.append(", commMode='").append(commMode).append('\'');
        sb.append(", configuredChannels=").append(configuredChannels);
        sb.append(", disallowedPayModes=").append(disallowedPayModes);
        sb.append(", envInfo=").append(envInfo);
        sb.append(", hybridAllowed=").append(hybridAllowed);
        sb.append(", minAmountForAddMoneyVoucher='").append(minAmountForAddMoneyVoucher).append('\'');
        sb.append(", offlineEnabled=").append(offlineEnabled);
        sb.append(", onTheFlyKYCRequired=").append(onTheFlyKYCRequired);
        sb.append(", paymentCharges=").append(paymentCharges);
        sb.append(", paymentChargesInfo=").append(paymentChargesInfo);
        sb.append(", paymentModeOnly='").append(paymentModeOnly).append('\'');
        sb.append(", productCode='").append(productCode).append('\'');
        sb.append(", retryCount=").append(retryCount);
        sb.append(", saveCardMandatory=").append(saveCardMandatory);
        sb.append(", subscriptionTxn=").append(subscriptionTxn);
        sb.append(", subsTypes=").append(subsTypes);
        sb.append(", txnType='").append(txnType).append('\'');
        sb.append(", uiEventLogToConsole=").append(uiEventLogToConsole);
        sb.append(", uiEventLogToServer=").append(uiEventLogToServer);
        sb.append(", walletOnly=").append(walletOnly);
        sb.append(", dynamicQR2FA=").append(dynamicQR2FA);
        sb.append('}');
        return sb.toString();
    }
}
