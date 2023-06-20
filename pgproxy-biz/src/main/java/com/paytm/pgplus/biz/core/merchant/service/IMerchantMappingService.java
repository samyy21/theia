package com.paytm.pgplus.biz.core.merchant.service;

import com.paytm.pgplus.cache.model.MerchantExtendedInfoResponse;

public interface IMerchantMappingService {

    public MerchantExtendedInfoResponse getMerchantInfoResponse(String merchantId);
}
