package com.paytm.pgplus.theia.nativ.model.payview.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.common.enums.MandateAuthMode;
import com.paytm.pgplus.common.enums.MandateMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class BankMandate extends Bank {

    private static final long serialVersionUID = -5167525347076508602L;

    private MandateMode mandateMode;

    private List<MandateAuthMode> mandateAuthMode;

    private String mandateBankCode;
}
