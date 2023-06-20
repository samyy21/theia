package com.paytm.pgplus.biz.core.merchant.service;

import com.paytm.pgplus.cache.model.EMIDetailList;

public interface IMerchantCenterService {
    public EMIDetailList getEMIDetailsByMid(String mid) throws Exception;
}
