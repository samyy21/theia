package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * Created by rahulverma on 1/9/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayMethodViews implements Serializable {

    private static final long serialVersionUID = 9176010381286236949L;
    @Valid
    private SavedInstruments merchantSavedInstruments;

    private SavedInstruments addMoneySavedInstruments;
    @Valid
    private List<PayMethod> merchantPayMethods;
    @Valid
    private List<PayMethod> addMoneyPayMethods;

    public PayMethodViews() {
    }

    public SavedInstruments getMerchantSavedInstruments() {
        return this.merchantSavedInstruments;
    }

    public SavedInstruments getAddMoneySavedInstruments() {
        return this.addMoneySavedInstruments;
    }

    public List<PayMethod> getMerchantPayMethods() {
        return this.merchantPayMethods;
    }

    public List<PayMethod> getAddMoneyPayMethods() {
        return this.addMoneyPayMethods;
    }

    public void setMerchantSavedInstruments(SavedInstruments merchantSavedInstruments) {
        this.merchantSavedInstruments = merchantSavedInstruments;
    }

    public void setAddMoneySavedInstruments(SavedInstruments addMoneySavedInstruments) {
        this.addMoneySavedInstruments = addMoneySavedInstruments;
    }

    public void setMerchantPayMethods(List<PayMethod> merchantPayMethods) {
        this.merchantPayMethods = merchantPayMethods;
    }

    public void setAddMoneyPayMethods(List<PayMethod> addMoneyPayMethods) {
        this.addMoneyPayMethods = addMoneyPayMethods;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayMethodViews{");
        sb.append("merchantSavedInstruments=").append(merchantSavedInstruments);
        sb.append(", addMoneySavedInstruments=").append(addMoneySavedInstruments);
        sb.append(", merchantPayMethods=").append(merchantPayMethods);
        sb.append(", addMoneyPayMethods=").append(addMoneyPayMethods);
        sb.append('}');
        return sb.toString();
    }
}
