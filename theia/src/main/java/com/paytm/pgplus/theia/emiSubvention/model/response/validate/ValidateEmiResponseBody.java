package com.paytm.pgplus.theia.emiSubvention.model.response.validate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.facade.emisubvention.models.Gratification;
import com.paytm.pgplus.facade.emisubvention.models.ItemBreakUp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateEmiResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = -8456974407295236856L;

    private String bankId;
    private String bankName;
    private String bankCode;
    private String cardType;
    private String bankLogoUrl;
    private String planId;
    private String pgPlanId;
    private String rate;
    private String interval;
    private String emi;
    private String interest;
    private String emiType;
    private String emiLabel;
    private List<Gratification> gratifications;
    private List<ItemBreakUp> itemBreakUpList;
    private String emiSubventionToken;
    private String finalTransactionAmount;
    private Boolean merchantOnlyContribution;
}
