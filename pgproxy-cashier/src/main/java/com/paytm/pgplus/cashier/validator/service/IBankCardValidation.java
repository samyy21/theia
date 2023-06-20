/**
 *
 */
package com.paytm.pgplus.cashier.validator.service;

import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;

/**
 * @author amitdubey
 *
 */
public interface IBankCardValidation {
    void validateSavedCard(SavedCardVO savedCard) throws PaytmValidationException;

    void validateSubmitCacheCardRequest(CardRequest cardRequest) throws CashierCheckedException;

    void validateCard(CompleteCardRequest completeCardRequest, BinCardRequest binCardRequest)
            throws PaytmValidationException;
}
