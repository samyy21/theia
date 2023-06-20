package com.paytm.pgplus.theia.nativ.model.vpa.details;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VpaDetailsServiceRequest {

    private String mid;
    private String ssoToken;
    private String paymentMode;
    private String txnToken;

}
