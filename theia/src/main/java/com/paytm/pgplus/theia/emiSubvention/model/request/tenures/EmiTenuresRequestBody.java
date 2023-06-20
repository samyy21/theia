package com.paytm.pgplus.theia.emiSubvention.model.request.tenures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.facade.emisubvention.models.Filter;
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
public class EmiTenuresRequestBody implements Serializable {

    private static final long serialVersionUID = 1288437083622839711L;

    private List<Item> items;
    private String mid;
    private Filter filters;
    private String customerId;
    private double subventionAmount;
    private double price;
    private String referenceId;
    private double originalPrice;
    private List<String> applicableTenures;
    private boolean amountBasedSubvention;

}
