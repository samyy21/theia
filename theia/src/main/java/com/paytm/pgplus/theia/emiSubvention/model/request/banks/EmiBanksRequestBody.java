package com.paytm.pgplus.theia.emiSubvention.model.request.banks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.emisubvention.models.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiBanksRequestBody implements Serializable {

    private static final long serialVersionUID = -8323067863538642356L;

    private List<Item> items;
    private String mid;
    private String customerId;
    private double subventionAmount;
    private double price;
    private String referenceId;

}
