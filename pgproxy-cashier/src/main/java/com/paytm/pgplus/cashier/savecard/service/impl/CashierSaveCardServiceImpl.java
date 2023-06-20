/**
 *
 */
package com.paytm.pgplus.cashier.savecard.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.pay.service.model.CashierUserCard;
import com.paytm.pgplus.cashier.savecard.service.ICashierSaveCardService;
import com.paytm.pgplus.common.enums.CardTypeEnum;
import com.paytm.pgplus.common.enums.StatusEnum;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;

/**
 * @author amit.dubey
 *
 */
@Component
public class CashierSaveCardServiceImpl implements ICashierSaveCardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CashierSaveCardServiceImpl.class);

    @Autowired
    ISavedCardService savedCardService;

    @Override
    public boolean saveCard(CashierUserCard card) throws CashierCheckedException {

        SavedCardVO cardDetails = new SavedCardVO();

        cardDetails.setCardNumber(card.getCardNumber());
        cardDetails.setCardType(CardTypeEnum.getEnumByName(card.getCardTypeVal()));
        cardDetails.setExpiryDate(card.getExpiryDate());
        cardDetails.setFirstSixDigit(Long.valueOf(card.getFirstSixDigit()));
        cardDetails.setLastFourDigit(Long.valueOf(card.getLastFourDigit()));
        cardDetails.setStatus(StatusEnum.getEnumByName(card.getStatusVal()));
        cardDetails.setUserId(card.getUserId());

        SavedCardResponse<Long> response = savedCardService.saveCardDetails(cardDetails);
        if (null == response.getResponseData()) {
            LOGGER.error("No response received from save card service");
            return false;
        }

        return response.getStatus();
    }
}
