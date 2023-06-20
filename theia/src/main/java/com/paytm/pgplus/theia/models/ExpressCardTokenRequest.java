package com.paytm.pgplus.theia.models;

import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.utils.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class ExpressCardTokenRequest implements Serializable {
    private static final long serialVersionUID = 4372560658770706544L;
    private static final char MASK_CHARACTER = '*';
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressCardTokenRequest.class);

    private String expiryMonth;
    private String expiryYear;
    private String cardNumber;
    private String userId;
    private String cvv;
    private String mid;
    private String savedCardId;
    private CardTokenInfo cardTokenInfo;

    // For internal processing only
    private String cardType;
    private String cardScheme;
    private String instId;
    private String loginUserId;

    public ExpressCardTokenRequest() {
    }

    public ExpressCardTokenRequest(final HttpServletRequest request) {
        this.expiryMonth = request.getParameter("CARD_EXPIRY_MONTH");
        this.expiryYear = request.getParameter("CARD_EXPIRY_YEAR");
        this.cvv = request.getParameter("CVV");
        this.cardNumber = request.getParameter("CARD_NUMBER");
        this.userId = request.getParameter("CUST_ID");
        this.mid = request.getParameter("MID");
        this.savedCardId = request.getParameter("SAVED_CARD_ID");
        try {
            if (StringUtils.isNotBlank(request.getParameter("CARD_TOKEN_INFO"))) {
                this.cardTokenInfo = JsonMapper.mapJsonToObject(request.getParameter("CARD_TOKEN_INFO"),
                        CardTokenInfo.class);
            }
        } catch (FacadeCheckedException e) {
            LOGGER.error("error occurred while parsing CARD_TOKEN_INFO");
        }

    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public void setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
    }

    private String getMaskedCardNumber(String cardNumber) {
        StringBuilder maskedCardNumber = new StringBuilder();
        if (StringUtils.isNotBlank(cardNumber)) {
            int cardNumberLength = cardNumber.length();
            maskedCardNumber.append(cardNumber.substring(0, 6)).append("******")
                    .append(cardNumber.substring(cardNumberLength - 4, cardNumberLength));
        }
        return maskedCardNumber.toString();
    }

    private String getMaskedValue(String plainText) {
        if (StringUtils.isNotBlank(plainText)) {
            return StringUtils.repeat(MASK_CHARACTER, plainText.length());
        }
        return null;
    }

    public String getLoginUserId() {
        return loginUserId;
    }

    public void setLoginUserId(String loginUserId) {
        this.loginUserId = loginUserId;
    }

    public CardTokenInfo getCardTokenInfo() {
        return cardTokenInfo;
    }

    public void setCardTokenInfo(CardTokenInfo cardTokenInfo) {
        this.cardTokenInfo = cardTokenInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExpressCardTokenRequest [maskedCardNumber=");
        builder.append(getMaskedCardNumber(cardNumber));
        builder.append(", maskedExpiryMonth=");
        builder.append(getMaskedValue(expiryMonth));
        builder.append(", maskedExpiryYear=");
        builder.append(getMaskedValue(expiryYear));
        builder.append(", maskedCvv=");
        builder.append(getMaskedValue(cvv));
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", mid=");
        builder.append(mid);
        builder.append(", savedCardId=");
        builder.append(savedCardId);
        builder.append(", cardType=");
        builder.append(cardType);
        builder.append(", cardScheme=");
        builder.append(cardScheme);
        builder.append(", instId=");
        builder.append(instId);
        builder.append(", cardTokenInfo=");
        builder.append(cardTokenInfo);
        builder.append("]");
        return builder.toString();
    }

}
