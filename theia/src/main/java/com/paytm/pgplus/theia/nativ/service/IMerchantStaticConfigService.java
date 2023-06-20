package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigResponse;
import com.paytm.pgplus.theia.nativ.model.merchantuserinfo.MerchantStaticConfigServiceRequest;

public interface IMerchantStaticConfigService {
    MerchantStaticConfigResponse getMerchantStaticConfig(
            MerchantStaticConfigServiceRequest merchantStaticConfigServiceRequest);
}
