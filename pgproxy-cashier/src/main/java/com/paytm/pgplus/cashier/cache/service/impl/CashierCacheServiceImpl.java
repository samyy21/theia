/**
 *
 */
package com.paytm.pgplus.cashier.cache.service.impl;

import com.paytm.pgplus.cashier.redis.IPgProxyCashierTransactionalRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.cache.service.ICashierCacheService;
import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.cashier.models.CashierRequest;

/**
 * @author amitdubey
 * @date Nov 24, 2016
 */
@Component("cashierCacheServiceImpl")
public class CashierCacheServiceImpl implements ICashierCacheService {

    @Autowired
    private IPgProxyCashierTransactionalRedisUtil pgProxyCashierTransactionalRedisUtil;

    @Override
    public void cacheCashierRequest(String cashierRequestId, CashierRequest cashierRequest) {
        StringBuilder sb = new StringBuilder(CashierConstant.PAYMENT_RETRY_KEY).append(cashierRequestId);
        pgProxyCashierTransactionalRedisUtil.set(sb.toString(), cashierRequest, 1 * 60 * 60);
    }

    @Override
    public CashierRequest fetchCashierRequest(String cashierRequestId) {
        StringBuilder sb = new StringBuilder(CashierConstant.PAYMENT_RETRY_KEY).append(cashierRequestId);
        return (CashierRequest) pgProxyCashierTransactionalRedisUtil.get(sb.toString());
    }

    @Override
    public void cacheCardRequest(String transId, CardRequest cardRequest) {
        StringBuilder sb = new StringBuilder(CashierConstant.ISOCARD_KEY).append(transId);
        pgProxyCashierTransactionalRedisUtil.set(sb.toString(), cardRequest, 1 * 60 * 60);
    }

    @Override
    public void cachePaymentRetryCount(String transId, int retryCount) {
        StringBuilder sb = new StringBuilder(CashierConstant.RETRY_COUNT).append(transId);
        pgProxyCashierTransactionalRedisUtil.set(sb.toString(), retryCount, 1 * 60 * 60);
    }

    @Override
    public Integer getPaymentRetryCountFromCache(String transId) {
        StringBuilder sb = new StringBuilder(CashierConstant.RETRY_COUNT).append(transId);
        return (Integer) pgProxyCashierTransactionalRedisUtil.get(sb.toString());
    }

}