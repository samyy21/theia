package com.paytm.pgplus.theia.services;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.payloadvault.theia.request.PayOptionsRequest;
import com.paytm.pgplus.payloadvault.theia.response.PayOptionsResponse;

/**
 * Created by ankitgupta on 10/8/17.
 */
public interface IMerchantPayOptionService {

    PayOptionsResponse processPayMethodsRequest(PayOptionsRequest payOptionsRequest) throws FacadeCheckedException;
}
