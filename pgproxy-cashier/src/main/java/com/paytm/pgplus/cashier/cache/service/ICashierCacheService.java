/**
 * 
 */
package com.paytm.pgplus.cashier.cache.service;

import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.cashier.models.CashierRequest;

/**
 * @author amit.dubey
 *
 */
public interface ICashierCacheService {
    void cacheCashierRequest(String cashierRequestId, CashierRequest cashierRequest);

    CashierRequest fetchCashierRequest(String cashierRequestId);

    void cacheCardRequest(String transId, CardRequest cardRequest);

    public void cachePaymentRetryCount(String transId, int retryCount);

    public Integer getPaymentRetryCountFromCache(String transId);
}
