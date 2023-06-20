package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by rahulverma on 17/4/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalCreditCheckBalanceRequestBody extends RequestBody {

    private static final long serialVersionUID = -847559789028162005L;
    @NotBlank
    private String amount;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "DigitalCreditCheckBalanceRequestBody{" + "amount='" + amount + '\'' + '}';
    }
}
