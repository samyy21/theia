package com.paytm.pgplus.theia.services;

import javax.servlet.http.HttpServletRequest;

import com.paytm.pgplus.cashier.cachecard.model.SavedCardRequest;
import com.paytm.pgplus.theia.exceptions.TheiaServiceException;

public interface IRetryPaymentService {

    boolean processPaymentRequest(HttpServletRequest requestData, SavedCardRequest savedCardRequest)
            throws TheiaServiceException;

}
