package com.paytm.pgplus.theia.offline.services;

import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.response.CashierInfoResponse;

/**
 * Created by rahulverma on 23/8/17.
 */
public interface ICashierService {
    public CashierInfoResponse fetchCashierInfo(CashierInfoRequest cashierInfoRequest);

}
