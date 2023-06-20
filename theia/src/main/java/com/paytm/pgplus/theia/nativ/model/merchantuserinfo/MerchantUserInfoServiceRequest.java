package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MerchantUserInfoServiceRequest {

    private String mid;
    private String orderId;
    private String tokenType;
    private String txnToken;
    private String paytmSsoToken;

    public MerchantUserInfoServiceRequest(String mid, String orderId, String tokenType, String paytmSsoToken) {
        this.mid = mid;
        this.orderId = orderId;
        this.tokenType = tokenType;
        this.paytmSsoToken = paytmSsoToken;
    }

    public MerchantUserInfoServiceRequest(String mid, String orderId, String tokenType, String txnToken,
            String paytmSsoToken) {
        this.mid = mid;
        this.orderId = orderId;
        this.tokenType = tokenType;
        this.txnToken = txnToken;
        this.paytmSsoToken = paytmSsoToken;
    }
}
