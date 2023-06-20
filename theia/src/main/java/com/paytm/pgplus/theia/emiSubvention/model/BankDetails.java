package com.paytm.pgplus.theia.emiSubvention.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankDetails implements Serializable {

    private static final long serialVersionUID = 4082617613493353721L;

    private String bankName;
    private String bankCode;
    private String bankLogoUrl;
}
