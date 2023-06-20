package com.paytm.pgplus.theia.nativ.model.merchantuserinfo;

import com.paytm.pgplus.theia.nativ.model.common.NativeInitiateRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MerchantInfoServiceRequest {

    private String mid;
    private String orderId;
    private String txnToken;
    private String ssoToken;
    private NativeInitiateRequest nativeInitiateRequest;
    private String callbackUrl;
    private boolean appSSOMatchWithOrderDetailSSO;

    public MerchantInfoServiceRequest(String mid, String orderId, String txnToken, String ssoToken,
            NativeInitiateRequest nativeInitiateRequest, boolean appSSOMatchWithOrderDetailSSO) {
        this.mid = mid;
        this.orderId = orderId;
        this.txnToken = txnToken;
        this.ssoToken = ssoToken;
        this.nativeInitiateRequest = nativeInitiateRequest;
        this.appSSOMatchWithOrderDetailSSO = appSSOMatchWithOrderDetailSSO;
    }

    public MerchantInfoServiceRequest(String mid) {
        this.mid = mid;
    }
}