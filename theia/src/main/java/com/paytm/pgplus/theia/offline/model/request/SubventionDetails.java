package com.paytm.pgplus.theia.offline.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.models.Item;
import com.paytm.pgplus.theia.offline.enums.EMISubventionStrategy;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubventionDetails implements Serializable {

    private static final long serialVersionUID = 3737049312274238712L;
    private List<Item> items;
    private String customerId;
    private EMISubventionStrategy strategy;
    private Double subventionAmount;
}
