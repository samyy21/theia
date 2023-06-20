package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

/**
 * Created by charuaggarwal on 4/7/17.
 */
public class SavedVPACardRequest implements Serializable {

    private static final long serialVersionUID = 8173112877989780081L;

    private String savedCardId;

    private String vpa;

    public SavedVPACardRequest(String savedCardId, String vpa) {
        this.savedCardId = savedCardId;
        this.vpa = vpa;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SavedVpaCardRequest [savedCardId").append(savedCardId);
        builder.append(",vpa").append(vpa).append("]");
        return builder.toString();
    }
}
