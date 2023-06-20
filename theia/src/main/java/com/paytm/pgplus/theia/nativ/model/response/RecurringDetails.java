package com.paytm.pgplus.theia.nativ.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecurringDetails implements Serializable {

    private static final long serialVersionUID = -3647471041280001902L;
    private Boolean pspSupportedRecurring;
    private Boolean bankSupportedRecurring;

    public Boolean getPspSupportedRecurring() {
        return pspSupportedRecurring;
    }

    public void setPspSupportedRecurring(Boolean pspSupportedRecurring) {
        this.pspSupportedRecurring = pspSupportedRecurring;
    }

    public Boolean getBankSupportedRecurring() {
        return bankSupportedRecurring;
    }

    public void setBankSupportedRecurring(Boolean bankSupportedRecurring) {
        this.bankSupportedRecurring = bankSupportedRecurring;
    }

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }
}
