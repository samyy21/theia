package com.paytm.pgplus.theia.nativ.model.balanceinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceInfoServiceRequest {

    private String mid;
    private String ssoToken;
    private String paymentMode;
    private String txnToken;

}
