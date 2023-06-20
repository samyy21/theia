/**
 *
 */
package com.paytm.pgplus.cashier.validator.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.cashier.cachecard.model.BinCardRequest;
import com.paytm.pgplus.cashier.cachecard.model.CompleteCardRequest;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.models.CardRequest;
import com.paytm.pgplus.cashier.validator.service.IBankCardValidation;
import com.paytm.pgplus.pgproxycommon.enums.CardType;
import com.paytm.pgplus.pgproxycommon.enums.PaytmValidationExceptionType;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.LuhnAlgoImpl;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;

/**
 * @author amitdubey
 *
 */
@Service
public class BankCardValidationImpl implements IBankCardValidation {

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    private static final int MAX_CARD_LENGTH = 19;

    @Override
    public void validateCard(final CompleteCardRequest completeCardRequest, final BinCardRequest binCardRequest)
            throws PaytmValidationException {

        if (StringUtils.isEmpty(completeCardRequest.getBankCardRequest().getCardNo())
                || completeCardRequest.getBankCardRequest().getCardNo().length() > MAX_CARD_LENGTH) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_CARD_LENGTH);
        }

        if (!LuhnAlgoImpl.validateCardNumber(completeCardRequest.getBankCardRequest().getCardNo())) {

            PaytmValidationExceptionType type = PaytmValidationExceptionType.INVALID_DEBIT_CARD_LUHN;

            if (CardType.CREDIT_CARD.equals(binCardRequest.getCardType())) {
                type = PaytmValidationExceptionType.INVALID_CREDIT_CARD_LUHN;
            }

            throw new PaytmValidationException(type);
        }

        if (!completeCardRequest.isDirectInstService()) {
            cardUtils.validateCVV(completeCardRequest.getBankCardRequest().getCvv(), StringUtils.EMPTY,
                    completeCardRequest.getBankCardRequest().getCardNo());
        }
    }

    @Override
    public void validateSavedCard(final SavedCardVO savedCard) throws PaytmValidationException {
        if (null == savedCard) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_SC);
        }

        if (StringUtils.isEmpty(String.valueOf(savedCard.getFirstSixDigit()))) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_SC);
        }

        if (StringUtils.isEmpty(savedCard.getCardNumber())) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_SC);
        }

        if (StringUtils.isEmpty(savedCard.getExpiryDate())) {
            throw new PaytmValidationException(PaytmValidationExceptionType.INVALID_PAYMODE_SC);
        }
    }

    /**
     * @param cardRequest
     * @throws CashierCheckedException
     */
    @Override
    public void validateSubmitCacheCardRequest(final CardRequest cardRequest) throws CashierCheckedException {
        if (null == cardRequest.getSavedCardRequest() && null == cardRequest.getBankCardRequest()
                && null == cardRequest.getImpsCardRequest() && null == cardRequest.getSavedImpsCardRequest()) {
            throw new CashierCheckedException(
                    "Process failed : savedCardRequest/ImpsCardRequest/BankCardRequest/savedImpsCardRequest can not be null");
        }
    }
}
