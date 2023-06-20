package com.paytm.pgplus.biz.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardIndex {
    private boolean emiExists;
    private String emiType;
    private String subventionType;
    private int lowestEmiAvailable;
}
