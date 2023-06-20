package com.paytm.pgplus.theia.emiSubvention.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.pgplus.Annotations.Mask;
import com.paytm.pgplus.Annotations.MaskToStringBuilder;
import com.paytm.pgplus.common.model.CardTokenInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDetails implements Serializable {

    private static final long serialVersionUID = -5703864219044589173L;

    @Mask
    private String cardNumber;
    private Double totalTransactionAmount;
    @JsonProperty("cardBin")
    private String cardBin6;
    private String savedInstrumentId;
    private CardTokenInfo cardTokenInfo;

    @Override
    public String toString() {
        return new MaskToStringBuilder(this).toString();
    }

}
