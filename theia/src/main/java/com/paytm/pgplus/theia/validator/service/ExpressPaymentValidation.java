package com.paytm.pgplus.theia.validator.service;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.LuhnAlgoImpl;
import com.paytm.pgplus.theia.models.ExpressCardTokenRequest;
import com.paytm.pgplus.theia.services.helper.ExpressPaymentServiceHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("expressPaymentValidation")
public class ExpressPaymentValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressPaymentValidation.class);

    @Autowired
    ExpressPaymentServiceHelper expressPaymentServiceHelper;

    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;

    public boolean validateCardNumber(String cardNumber) {
        if (StringUtils.isBlank(cardNumber)) {
            return false;
        }
        return LuhnAlgoImpl.validateCardNumber(cardNumber);
    }

    public boolean validateExpiryDate(String expiryMonth, String expiryYear, String cardScheme) {

        try {
            cardUtils.validateExpiryDate(expiryMonth, expiryYear, cardScheme, StringUtils.EMPTY);
        } catch (PaytmValidationException e) {
            return false;
        }

        return true;

    }

    public boolean validateCvv(ExpressCardTokenRequest requestData) {
        // Fetched BIN details first to identify card scheme

        BinDetail binDetails = null;
        String binNumber = requestData.getCardNumber().substring(0, 6);

        try {

            binDetails = expressPaymentServiceHelper.fetchBinRelatedDetails(requestData.getCardNumber());

            if (binDetails == null) {
                return false;
            }

        } catch (PaytmValidationException exception) {
            LOGGER.error("Error occurred while fetching bin details for bin {}, reason {}", binNumber, exception);
            return false;
        }

        requestData.setCardType(binDetails.getCardType());
        requestData.setCardScheme(binDetails.getCardName());
        requestData.setInstId(binDetails.getBankCode());

        String cvv = requestData.getCvv();
        LOGGER.debug("Bin details fetched:{}", binDetails);
        return validatingExpireByCardscheme(cvv, binDetails.getCardName());

    }

    public boolean validatingExpireByCardscheme(String cvv, String cardScheme) {
        switch (CardScheme.getCardSchemebyName(cardScheme)) {
        // CVV not mandatory for MAESTRO cards
        case MAESTRO:
            break;
        case AMEX: {
            if (cvv.length() != 4) {
                return false;
            }
            break;
        }
        default: {
            if (cvv.length() != 3) {
                return false;
            }
            break;
        }
        }
        return true;

    }
}
