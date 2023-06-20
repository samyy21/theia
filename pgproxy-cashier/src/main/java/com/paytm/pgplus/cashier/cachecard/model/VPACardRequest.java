package com.paytm.pgplus.cashier.cachecard.model;

import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by charuaggarwal on 3/7/17.
 */
public class VPACardRequest implements Serializable {

    private static final long serialVersionUID = 3327753279760805690L;

    @NotBlank
    private String vpa;

    public VPACardRequest(String vpa) {
        this.vpa = vpa;
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
        builder.append("SavedVpaCardRequest [vpa").append(vpa).append("]");
        return builder.toString();
    }

}
