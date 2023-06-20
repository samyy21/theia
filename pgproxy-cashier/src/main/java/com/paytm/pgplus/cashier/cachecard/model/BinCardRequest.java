/**
 *
 */
package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;
import com.paytm.pgplus.pgproxycommon.enums.CardScheme;
import com.paytm.pgplus.pgproxycommon.enums.CardType;

/**
 * @author amit.dubey
 *
 */
public class BinCardRequest implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5246050091731001594L;

    private CardType cardType;
    private CardScheme cardScheme;
    private boolean active;
    private String instNetworkCode;
    private String instId;
    private String bankName;
    private Long binNumber;
    private boolean isIndian;

    /**
     * @param cardType
     * @param cardScheme
     * @param cardStatus
     * @throws CashierCheckedException
     */
    public BinCardRequest(CardType cardType, CardScheme cardScheme, boolean cardStatus, String instNetworkCode)
            throws CashierCheckedException {
        BeanParameterValidator.validateInputObjectParam(cardType, "cardType");
        this.cardType = cardType;

        BeanParameterValidator.validateInputObjectParam(cardScheme, "cardScheme");
        this.cardScheme = cardScheme;

        this.active = cardStatus;

        BeanParameterValidator.validateInputStringParam(instNetworkCode, "instNetworkCode");
        this.instNetworkCode = instNetworkCode;
    }

    public BinCardRequest(BinDetail binDetail) {
        this.cardType = CardType.valueOf(binDetail.getCardType());
        this.cardScheme = CardScheme.getCardSchemebyName(binDetail.getCardName());
        this.active = binDetail.isActive();
        this.instNetworkCode = binDetail.getBankCode();
        this.instId = binDetail.getBankCode();
        this.bankName = binDetail.getBank();
        this.isIndian = binDetail.getIsIndian() ? true : false;
        this.binNumber = binDetail.getBin();
    }

    public boolean isIndian() {
        return isIndian;
    }

    public void setIndian(boolean isIndian) {
        this.isIndian = isIndian;
    }

    public Long getBinNumber() {
        return binNumber;
    }

    /**
     * @return the instId
     */
    public String getInstId() {
        return instId;
    }

    /**
     * @return the cardType
     */
    public CardType getCardType() {
        return cardType;
    }

    /**
     * @return the cardScheme
     */
    public CardScheme getCardScheme() {
        return cardScheme;
    }

    /**
     * @return the cardStatus
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the instNetworkCode
     */
    public String getInstNetworkCode() {
        return instNetworkCode;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public void setCardScheme(CardScheme cardScheme) {
        this.cardScheme = cardScheme;
    }

    /**
     * @return the bankName
     */
    public String getBankName() {
        return bankName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BinCardRequest [cardType=");
        builder.append(cardType);
        builder.append(", cardScheme=");
        builder.append(cardScheme);
        builder.append(", active=");
        builder.append(active);
        builder.append(", instNetworkCode=");
        builder.append(instNetworkCode);
        builder.append(", instId=");
        builder.append(instId);
        builder.append(", bankName=");
        builder.append(bankName);
        builder.append(", binNumber=");
        builder.append(binNumber);
        builder.append(", isIndian=");
        builder.append(isIndian);
        builder.append("]");
        return builder.toString();
    }
}
