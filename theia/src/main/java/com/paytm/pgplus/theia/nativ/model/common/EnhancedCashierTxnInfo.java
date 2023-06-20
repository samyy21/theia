package com.paytm.pgplus.theia.nativ.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedCashierTxnInfo implements Serializable {

    private static final long serialVersionUID = 4170010563751002406L;

    private String txnAmount;
    private String type;
    private boolean insufficientBalance;
    private String id;
    private boolean addMoney;
    // private PCFDetail pcfDetail;
    private boolean pcfEnabled;
    private String redirectFlow;
    private String addMoneyDestination;
    private String gvConsentMsg;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean isAddMoneyPcfEnabled;

    public boolean isPcfEnabled() {
        return pcfEnabled;
    }

    public void setPcfEnabled(boolean pcfEnabled) {
        this.pcfEnabled = pcfEnabled;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInsufficientBalance() {
        return insufficientBalance;
    }

    public void setInsufficientBalance(boolean insufficientBalance) {
        this.insufficientBalance = insufficientBalance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAddMoney() {
        return addMoney;
    }

    public void setAddMoney(boolean addMoney) {
        this.addMoney = addMoney;
    }

    public String getRedirectFlow() {
        return redirectFlow;
    }

    public void setRedirectFlow(String redirectFlow) {
        this.redirectFlow = redirectFlow;
    }

    public String getAddMoneyDestination() {
        return addMoneyDestination;
    }

    public void setAddMoneyDestination(String addMoneyDestination) {
        this.addMoneyDestination = addMoneyDestination;
    }

    public String getGvConsentMsg() {
        return gvConsentMsg;
    }

    public void setGvConsentMsg(String gvConsentMsg) {
        this.gvConsentMsg = gvConsentMsg;
    }

    @JsonProperty(value = "isAddMoneyPcfEnabled")
    public boolean getAddMoneyPcfEnabled() {
        return isAddMoneyPcfEnabled;
    }

    public void setAddMoneyPcfEnabled(boolean addMoneyPcfEnabled) {
        isAddMoneyPcfEnabled = addMoneyPcfEnabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnhancedCashierTxnInfo [txnAmount=").append(txnAmount).append(", type=").append(type)
                .append(", insufficientBalance=").append(insufficientBalance).append(", id=").append(id)
                .append(", pcfEnabled=").append(pcfEnabled).append(", addMoney=").append(addMoney)
                .append(", isAddMoneyPcfEnabled=").append(isAddMoneyPcfEnabled).append("]");
        return builder.toString();
    }

}
