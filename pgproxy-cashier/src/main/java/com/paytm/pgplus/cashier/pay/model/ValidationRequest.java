/**
 * 
 */
package com.paytm.pgplus.cashier.pay.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lalit Mehra
 *
 */
public class ValidationRequest implements Serializable {

    private static final long serialVersionUID = 2600017473179789061L;

    private String txnMode;
    private List<String> bankList;
    private String selectedBank;
    private boolean addMoney;
    private String entityId;
    private String bankId;
    private PSULimit psuLimitInfo;

    public ValidationRequest(String txnMode, List<String> bankList, String selectedBank, boolean addMoney,
            String entityId, String bankId, PSULimit psuLimitInfo) {
        this.txnMode = txnMode;
        this.bankList = bankList;
        this.selectedBank = selectedBank;
        this.addMoney = addMoney;
        this.entityId = entityId;
        this.bankId = bankId;
        this.psuLimitInfo = psuLimitInfo;
    }

    public String getTxnMode() {
        return txnMode;
    }

    public void setTxnMode(String txnMode) {
        this.txnMode = txnMode;
    }

    public List<String> getBankList() {
        return bankList;
    }

    public void setBankList(List<String> bankList) {
        this.bankList = bankList;
    }

    public String getSelectedBank() {
        return selectedBank;
    }

    public void setSelectedBank(String selectedBank) {
        this.selectedBank = selectedBank;
    }

    public boolean isAddMoney() {
        return addMoney;
    }

    public void setAddMoney(boolean addMoney) {
        this.addMoney = addMoney;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public PSULimit getPsuLimitInfo() {
        return psuLimitInfo;
    }

    public void setPsuLimitInfo(PSULimit psuLimitInfo) {
        this.psuLimitInfo = psuLimitInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ValidationRequest [txnMode=");
        builder.append(txnMode);
        builder.append(", bankList=");
        builder.append(bankList);
        builder.append(", selectedBank=");
        builder.append(selectedBank);
        builder.append(", addMoney=");
        builder.append(addMoney);
        builder.append(", entityId=");
        builder.append(entityId);
        builder.append(", bankId=");
        builder.append(bankId);
        builder.append(", psuLimitInfo=");
        builder.append(psuLimitInfo);
        builder.append("]");
        return builder.toString();
    }

}
