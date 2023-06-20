package com.paytm.pgplus.biz.workflow.aoatimeoutcenter.service;

import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderRequest;
import com.paytm.pgplus.biz.workflow.model.AoaTimeoutCenterOrderResponse;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;

import java.util.Map;

public interface IAoaTimeoutCenterService {
    AoaTimeoutCenterOrderResponse persistOrderInfoAtAoaTimeoutCenter(AoaTimeoutCenterOrderRequest request,
            Map<String, String> queryParams) throws FacadeCheckedException;
}
