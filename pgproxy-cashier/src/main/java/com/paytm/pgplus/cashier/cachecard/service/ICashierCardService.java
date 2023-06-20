/**
 *
 */
package com.paytm.pgplus.cashier.cachecard.service;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CashierRequest;
import com.paytm.pgplus.facade.enums.InstNetworkType;
import com.paytm.pgplus.facade.user.models.response.CacheCardResponseBody;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amit.dubey
 *
 */
public interface ICashierCardService {
    CacheCardResponseBody submitCacheCard(CashierRequest cashierRequest, InstNetworkType instNetworkType)
            throws CashierCheckedException, PaytmValidationException;
}