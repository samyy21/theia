/**
 *
 */
package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.enums.CardType;

/**
 * @author amit.dubey
 *
 */
public class CardPayOption implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    private CardType cardType;
    private CardScheme cardSchema;

    /**
     * @param cardType
     * @param cardSchema
     * @throws CashierCheckedException
     */
    public CardPayOption(CardType cardType, CardScheme cardSchema) throws CashierCheckedException {
        BeanParameterValidator.validateInputObjectParam(cardType, "cardType");
        this.cardType = cardType;

        BeanParameterValidator.validateInputObjectParam(cardSchema, "cardSchema");
        this.cardSchema = cardSchema;
    }

    public String getPayOption() {
        StringBuilder sb = new StringBuilder();
        sb.append(cardType.getValue()).append("_").append(cardSchema.getValue());

        return sb.toString();
    }

}
