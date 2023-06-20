package com.paytm.pgplus.theia.sessiondata;

import java.io.Serializable;

import com.dyuproject.protostuff.Tag;
import com.paytm.pgplus.facade.user.models.PaytmBanksVpaDefaultDebitCredit;

/**
 * @author vivek kumar updated by santosh chourasia
 * @date 19-Dec-2017
 */
public class SarvatraVPAInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Tag(value = 1)
    private String name;
    @Tag(value = 2)
    private PaytmBanksVpaDefaultDebitCredit defaultDebit;
    @Tag(value = 3)
    private boolean primary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PaytmBanksVpaDefaultDebitCredit getDefaultDebit() {
        return defaultDebit;
    }

    public void setDefaultDebit(PaytmBanksVpaDefaultDebitCredit defaultDebit) {
        this.defaultDebit = defaultDebit;
    }

    public boolean getPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SarvatraVPAInfo [name=").append(name).append(", defaultDebit=").append(defaultDebit)
                .append(", primary=").append(primary).append("]");
        return builder.toString();
    }

}
