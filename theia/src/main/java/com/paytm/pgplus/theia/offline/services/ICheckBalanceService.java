package com.paytm.pgplus.theia.offline.services;

import com.paytm.pgplus.theia.offline.model.request.DigitalCreditCheckBalanceRequest;
import com.paytm.pgplus.theia.offline.model.response.DigitalCreditCheckBalanceResponse;

/**
 * Created by rahulverma on 17/4/18.
 */
public interface ICheckBalanceService {

    DigitalCreditCheckBalanceResponse checkDigitalCreditBalance(DigitalCreditCheckBalanceRequest request);

}
