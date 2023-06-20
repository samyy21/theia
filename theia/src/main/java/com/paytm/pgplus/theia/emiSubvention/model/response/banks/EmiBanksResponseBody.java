package com.paytm.pgplus.theia.emiSubvention.model.response.banks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.response.BaseResponseBody;
import com.paytm.pgplus.facade.emisubvention.models.BankDetailsByEmiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmiBanksResponseBody extends BaseResponseBody {

    private static final long serialVersionUID = 3226779636087240284L;

    private List<BankDetailsByEmiType> emiTypes;

}
