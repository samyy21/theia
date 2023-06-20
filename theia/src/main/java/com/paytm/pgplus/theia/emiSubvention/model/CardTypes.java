package com.paytm.pgplus.theia.emiSubvention.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardTypes implements Serializable {

    private static final long serialVersionUID = -1875767962408224356L;

    private String cardLabel;
    private String cardType;
    private List<BankDetails> bankDetails;

}
