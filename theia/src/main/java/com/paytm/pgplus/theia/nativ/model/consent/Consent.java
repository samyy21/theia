package com.paytm.pgplus.theia.nativ.model.consent;

import java.io.Serializable;

/**
 * Created by: satyamsinghrajput at 23/10/19
 */
public class Consent implements Serializable {

    private static final long serialVersionUID = 3403529473206336485L;

    private String bankCode;
    private String linkAccount;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getLinkAccount() {
        return linkAccount;
    }

    public void setLinkAccount(String linkAccount) {
        this.linkAccount = linkAccount;
    }

    @Override
    public String toString() {
        return "Consent{" + "bankCode='" + bankCode + '\'' + ", linkAccount='" + linkAccount + '\'' + '}';
    }
}
