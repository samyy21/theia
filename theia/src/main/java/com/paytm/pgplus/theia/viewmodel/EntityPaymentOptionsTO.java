package com.paytm.pgplus.theia.viewmodel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author amitdubey
 *
 */
public class EntityPaymentOptionsTO implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 2602260140655279620L;

    private List<BankInfo> completeDcList;
    private List<BankInfo> completeCcList;
    private List<BankInfo> completeNbList;
    private List<BankInfo> completeATMList;
    private List<BankInfo> completeIMPSList;
    private List<BankInfo> completeEMIInfoList;
    private List<BankInfo> hybridEMIInfoList;

    private String errorCode;
    private String cardStoreOption;

    private boolean dcEnabled;
    private boolean ccEnabled;
    private boolean netBankingEnabled;
    private boolean atmEnabled;
    private boolean impsEnabled;
    private boolean emiEnabled;
    private boolean codEnabled;

    /* These all are addAndPay Modes */
    private boolean addDcEnabled;
    private boolean addCcEnabled;
    private boolean addNetBankingEnabled;
    private boolean addAtmEnabled;
    private boolean addImpsEnabled;
    private boolean addEmiEnabled;
    private boolean addCodEnabled;

    private List<BankInfo> addCompleteCcList;
    private List<BankInfo> addCompleteDcList;
    private List<BankInfo> addCompleteNbList;
    private List<BankInfo> addCompleteATMList;
    private List<BankInfo> addCompleteIMPSList;
    private List<BankInfo> addCompleteEMIInfoList;

    private boolean addUpiEnabled;
    private boolean upiEnabled;
    private List<BankInfo> completeUPIInfoList;
    private List<BankInfo> addCompleteUpiInfoList;
    private Set<String> directServiceInsts = new HashSet<>();
    private Set<String> supportAtmPins = new HashSet<>();
    private boolean iDebitEnabled;
    private String lowPercentageMessage;
    private String maintenanceMessage;
    private String maintainenceMessage;

    private boolean paymentsBankEnabled;
    private boolean addPaymentsBankEnabled;
    private boolean upiPushEnabled;
    private boolean addUpiPushEnabled;
    private boolean upiPushExpressEnabled;
    private boolean addUpiPushExpressEnabled;
    private boolean reseller;

    public boolean isiDebitEnabled() {
        return iDebitEnabled;
    }

    public void setiDebitEnabled(boolean iDebitEnabled) {
        this.iDebitEnabled = iDebitEnabled;
    }

    /**
     * @return the directServiceInsts
     */
    public Set<String> getDirectServiceInsts() {
        return directServiceInsts;
    }

    /**
     * @param directServiceInsts
     *            the directServiceInsts to set
     */
    public void setDirectServiceInsts(Set<String> directServiceInsts) {
        this.directServiceInsts = directServiceInsts;
    }

    public Set<String> getSupportAtmPins() {
        return supportAtmPins;
    }

    public void setSupportAtmPins(Set<String> supportAtmPins) {
        this.supportAtmPins = supportAtmPins;
    }

    public boolean isAddDcEnabled() {
        return addDcEnabled;
    }

    public void setAddDcEnabled(boolean addDcEnabled) {
        this.addDcEnabled = addDcEnabled;
    }

    public boolean isAddCcEnabled() {
        return addCcEnabled;
    }

    public void setAddCcEnabled(boolean addCcEnabled) {
        this.addCcEnabled = addCcEnabled;
    }

    public boolean isAddNetBankingEnabled() {
        return addNetBankingEnabled;
    }

    public void setAddNetBankingEnabled(boolean addNetBankingEnabled) {
        this.addNetBankingEnabled = addNetBankingEnabled;
    }

    public boolean isAddAtmEnabled() {
        return addAtmEnabled;
    }

    public void setAddAtmEnabled(boolean addAtmEnabled) {
        this.addAtmEnabled = addAtmEnabled;
    }

    public boolean isAddImpsEnabled() {
        return addImpsEnabled;
    }

    public void setAddImpsEnabled(boolean addImpsEnabled) {
        this.addImpsEnabled = addImpsEnabled;
    }

    public boolean isAddEmiEnabled() {
        return addEmiEnabled;
    }

    public void setAddEmiEnabled(boolean addEmiEnabled) {
        this.addEmiEnabled = addEmiEnabled;
    }

    public boolean isAddCodEnabled() {
        return addCodEnabled;
    }

    public void setAddCodEnabled(boolean addCodEnabled) {
        this.addCodEnabled = addCodEnabled;
    }

    public List<BankInfo> getAddCompleteDcList() {
        return addCompleteDcList;
    }

    public void setAddCompleteDcList(List<BankInfo> addCompleteDcList) {
        this.addCompleteDcList = addCompleteDcList;
    }

    public List<BankInfo> getAddCompleteCcList() {
        return addCompleteCcList;
    }

    public void setAddCompleteCcList(List<BankInfo> addCompleteCcList) {
        this.addCompleteCcList = addCompleteCcList;
    }

    public List<BankInfo> getAddCompleteNbList() {
        return addCompleteNbList;
    }

    public void setAddCompleteNbList(List<BankInfo> addCompleteNbList) {
        this.addCompleteNbList = addCompleteNbList;
    }

    public List<BankInfo> getAddCompleteATMList() {
        return addCompleteATMList;
    }

    public void setAddCompleteATMList(List<BankInfo> addCompleteATMList) {
        this.addCompleteATMList = addCompleteATMList;
    }

    public List<BankInfo> getAddCompleteIMPSList() {
        return addCompleteIMPSList;
    }

    public void setAddCompleteIMPSList(List<BankInfo> addCompleteIMPSList) {
        this.addCompleteIMPSList = addCompleteIMPSList;
    }

    public List<BankInfo> getAddCompleteEMIInfoList() {
        return addCompleteEMIInfoList;
    }

    public void setAddCompleteEMIInfoList(List<BankInfo> addCompleteEMIInfoList) {
        this.addCompleteEMIInfoList = addCompleteEMIInfoList;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public List<BankInfo> getCompleteEMIInfoList() {
        return completeEMIInfoList;
    }

    public void setCompleteEMIInfoList(List<BankInfo> completeEMIInfoList) {
        this.completeEMIInfoList = completeEMIInfoList;
    }

    public List<BankInfo> getCompleteIMPSList() {
        return completeIMPSList;
    }

    public void setCompleteIMPSList(List<BankInfo> completeIMPSList) {
        this.completeIMPSList = completeIMPSList;
    }

    public boolean isDcEnabled() {
        return dcEnabled;
    }

    public void setDcEnabled(boolean dcEnabled) {
        this.dcEnabled = dcEnabled;
    }

    public boolean isCcEnabled() {
        return ccEnabled;
    }

    public void setCcEnabled(boolean ccEnabled) {
        this.ccEnabled = ccEnabled;
    }

    public boolean isNetBankingEnabled() {
        return netBankingEnabled;
    }

    public void setNetBankingEnabled(boolean netBankingEnabled) {
        this.netBankingEnabled = netBankingEnabled;
    }

    public boolean isAtmEnabled() {
        return atmEnabled;
    }

    public void setAtmEnabled(boolean atmEnabled) {
        this.atmEnabled = atmEnabled;
    }

    public boolean isImpsEnabled() {
        return impsEnabled;
    }

    public void setImpsEnabled(boolean impsEnabled) {
        this.impsEnabled = impsEnabled;
    }

    public boolean isEmiEnabled() {
        return emiEnabled;
    }

    public void setEmiEnabled(boolean emiEnabled) {
        this.emiEnabled = emiEnabled;
    }

    public boolean isCodEnabled() {
        return codEnabled;
    }

    public void setCodEnabled(boolean codEnabled) {
        this.codEnabled = codEnabled;
    }

    public List<BankInfo> getCompleteATMList() {
        return completeATMList;
    }

    public void setCompleteATMList(List<BankInfo> completeATMList) {
        this.completeATMList = completeATMList;
    }

    public String getCardStoreOption() {
        return cardStoreOption;
    }

    public void setCardStoreOption(String cardStoreOption) {
        this.cardStoreOption = cardStoreOption;
    }

    public List<BankInfo> getCompleteNbList() {
        return completeNbList;
    }

    public void setCompleteNbList(List<BankInfo> completeNbList) {
        this.completeNbList = completeNbList;
    }

    public List<BankInfo> getCompleteDcList() {
        return completeDcList;
    }

    public void setCompleteDcList(List<BankInfo> completeDcList) {
        this.completeDcList = completeDcList;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public List<BankInfo> getCompleteCcList() {
        return completeCcList;
    }

    public void setCompleteCcList(List<BankInfo> completeCcList) {
        this.completeCcList = completeCcList;
    }

    public List<BankInfo> getHybridEMIInfoList() {
        return hybridEMIInfoList;
    }

    public void setHybridEMIInfoList(List<BankInfo> hybridEMIInfoList) {
        this.hybridEMIInfoList = hybridEMIInfoList;
    }

    public List<BankInfo> getAddCompleteUpiInfoList() {
        return addCompleteUpiInfoList;
    }

    public void setAddCompleteUpiInfoList(List<BankInfo> addCompleteUpiInfoList) {
        this.addCompleteUpiInfoList = addCompleteUpiInfoList;
    }

    public boolean isAddUpiEnabled() {
        return addUpiEnabled;
    }

    public void setAddUpiEnabled(boolean addUpiEnabled) {
        this.addUpiEnabled = addUpiEnabled;
    }

    public List<BankInfo> getCompleteUPIInfoList() {
        return completeUPIInfoList;
    }

    public void setCompleteUPIInfoList(List<BankInfo> completeUPIInfoList) {
        this.completeUPIInfoList = completeUPIInfoList;
    }

    public boolean isUpiEnabled() {
        return upiEnabled;
    }

    public void setUpiEnabled(boolean upiEnabled) {
        this.upiEnabled = upiEnabled;
    }

    public String getLowPercentageMessage() {
        return lowPercentageMessage;
    }

    public void setLowPercentageMessage(String lowPercentageMessage) {
        this.lowPercentageMessage = lowPercentageMessage;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public boolean isPaymentsBankEnabled() {
        return paymentsBankEnabled;
    }

    public void setPaymentsBankEnabled(boolean paymentsBankEnabled) {
        this.paymentsBankEnabled = paymentsBankEnabled;
    }

    public boolean isAddPaymentsBankEnabled() {
        return addPaymentsBankEnabled;
    }

    public void setAddPaymentsBankEnabled(boolean addPaymentsBankEnabled) {
        this.addPaymentsBankEnabled = addPaymentsBankEnabled;
    }

    public boolean isUpiPushEnabled() {
        return upiPushEnabled;
    }

    public void setUpiPushEnabled(boolean upiPushEnabled) {
        this.upiPushEnabled = upiPushEnabled;
    }

    public boolean isAddUpiPushEnabled() {
        return addUpiPushEnabled;
    }

    public void setAddUpiPushEnabled(boolean addupiPushEnabled) {
        this.addUpiPushEnabled = addupiPushEnabled;
    }

    public boolean isUpiPushExpressEnabled() {
        return upiPushExpressEnabled;
    }

    public void setUpiPushExpressEnabled(boolean upiPushExpressEnabled) {
        this.upiPushExpressEnabled = upiPushExpressEnabled;
    }

    public boolean isAddUpiPushExpressEnabled() {
        return addUpiPushExpressEnabled;
    }

    public void setAddUpiPushExpressEnabled(boolean addUpiPushExpressEnabled) {
        this.addUpiPushExpressEnabled = addUpiPushExpressEnabled;
    }

    public boolean isReseller() {
        return reseller;
    }

    public void setReseller(boolean reseller) {
        this.reseller = reseller;
    }

    public void resetAllPaymentOptionsEnabled() {
        ccEnabled = false;
        dcEnabled = false;
        netBankingEnabled = false;
        atmEnabled = false;
        emiEnabled = false;
        codEnabled = false;
        impsEnabled = false;
        upiEnabled = false;
        paymentsBankEnabled = false;
        upiPushEnabled = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityPaymentOptionsTO [completeDcList=");
        builder.append(completeDcList);
        builder.append(", completeCcList=");
        builder.append(completeCcList);
        builder.append(", completeNbList=");
        builder.append(completeNbList);
        builder.append(", completeATMList=");
        builder.append(completeATMList);
        builder.append(", completeIMPSList=");
        builder.append(completeIMPSList);
        builder.append(", completeEMIInfoList=");
        builder.append(completeEMIInfoList);
        builder.append(", hybridEMIInfoList=");
        builder.append(hybridEMIInfoList);
        builder.append(", errorCode=");
        builder.append(errorCode);
        builder.append(", cardStoreOption=");
        builder.append(cardStoreOption);
        builder.append(", dcEnabled=");
        builder.append(dcEnabled);
        builder.append(", ccEnabled=");
        builder.append(ccEnabled);
        builder.append(", netBankingEnabled=");
        builder.append(netBankingEnabled);
        builder.append(", atmEnabled=");
        builder.append(atmEnabled);
        builder.append(", impsEnabled=");
        builder.append(impsEnabled);
        builder.append(", emiEnabled=");
        builder.append(emiEnabled);
        builder.append(", codEnabled=");
        builder.append(codEnabled);
        builder.append(", addDcEnabled=");
        builder.append(addDcEnabled);
        builder.append(", addCcEnabled=");
        builder.append(addCcEnabled);
        builder.append(", addNetBankingEnabled=");
        builder.append(addNetBankingEnabled);
        builder.append(", addAtmEnabled=");
        builder.append(addAtmEnabled);
        builder.append(", addImpsEnabled=");
        builder.append(addImpsEnabled);
        builder.append(", addEmiEnabled=");
        builder.append(addEmiEnabled);
        builder.append(", addCodEnabled=");
        builder.append(addCodEnabled);
        builder.append(", addCompleteCcList=");
        builder.append(addCompleteCcList);
        builder.append(", addCompleteDcList=");
        builder.append(addCompleteDcList);
        builder.append(", addCompleteNbList=");
        builder.append(addCompleteNbList);
        builder.append(", addCompleteATMList=");
        builder.append(addCompleteATMList);
        builder.append(", addCompleteIMPSList=");
        builder.append(addCompleteIMPSList);
        builder.append(", addCompleteEMIInfoList=");
        builder.append(addCompleteEMIInfoList);
        builder.append(", addUpiEnabled=");
        builder.append(addUpiEnabled);
        builder.append(", upiEnabled=");
        builder.append(upiEnabled);
        builder.append(", completeUPIInfoList=");
        builder.append(completeUPIInfoList);
        builder.append(", addCompleteUpiInfoList=");
        builder.append(addCompleteUpiInfoList);
        builder.append(", directServiceInsts=");
        builder.append(directServiceInsts);
        builder.append(", iDebitEnabled=");
        builder.append(iDebitEnabled);
        builder.append(", lowPercentageMessage=");
        builder.append(lowPercentageMessage);
        builder.append(", maintenanceMessage=");
        builder.append(maintenanceMessage);
        builder.append(", paymentsBankEnabled=");
        builder.append(paymentsBankEnabled);
        builder.append(", addPaymentsBankEnabled=");
        builder.append(addPaymentsBankEnabled);
        builder.append(", upiPushEnabled=");
        builder.append(upiPushEnabled);
        builder.append(", addUpiPushEnabled=");
        builder.append(addUpiPushEnabled);
        builder.append(", upiPushExpressEnabled=");
        builder.append(upiPushExpressEnabled);
        builder.append(", addUpiPushExpressEnabled=");
        builder.append(addUpiPushExpressEnabled);
        builder.append(", reseller");
        builder.append(reseller);
        builder.append(", supportAtmPins=");
        builder.append(supportAtmPins);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the maintainenceMessage
     */
    public String getMaintainenceMessage() {
        return maintainenceMessage;
    }

    /**
     * @param maintainenceMessage
     *            the maintainenceMessage to set
     */
    public void setMaintainenceMessage(String maintainenceMessage) {
        this.maintainenceMessage = maintainenceMessage;
    }

}
