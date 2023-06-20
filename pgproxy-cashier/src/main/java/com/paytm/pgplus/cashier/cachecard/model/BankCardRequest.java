/**
 *
 */
package com.paytm.pgplus.cashier.cachecard.model;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.paytm.pgplus.cashier.constant.CashierConstant;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.validator.BeanParameterValidator;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;

/**
 * @author amit.dubey
 *
 */
public class BankCardRequest implements Serializable {
    /**
     * serial version UID
     */
    private static final long serialVersionUID = 3927807306586165127L;

    @NotBlank(message = "{notblank}")
    @Length(max = 32, message = "{lengthlimit}")
    private final String cardNo;

    @NotBlank(message = "{notblank}")
    @Length(max = 32, message = "{lengthlimit}")
    private String cvv;

    @NotBlank(message = "{notblank}")
    @Pattern(regexp = "[\\d]{4}")
    private String expiryYear;

    @NotBlank(message = "{notblank}")
    @Pattern(regexp = "[\\d]{2}")
    private String expiryMonth;

    private String otp;

    @NotBlank(message = "{notblank}")
    private String cardType;

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    /**
     * @param cardNo
     * @param cvv
     * @param expiryYear
     * @param expiryMonth
     * @throws CashierCheckedException
     */
    public BankCardRequest(String cardNo, String cvv, String expiryYear, String expiryMonth, String cardScheme)
            throws CashierCheckedException, PaytmValidationException {
        BeanParameterValidator.validateInputStringParam(cardNo, "cardNo");
        this.cardNo = cardNo;

        if (cardNo.length() < 6 || !StringUtils.isNumeric(cardNo)) {
            throw new PaytmValidationException("Invalid Card Number");
        }

        switch (cardScheme) {
        case CashierConstant.MAESTRO_CARD:
            this.expiryYear = CashierConstant.MAESTRO_CARD_EXPIRY_YEAR;
            this.expiryMonth = CashierConstant.MAESTRO_CARD_EXPIRY_MONTH;
            this.cvv = CashierConstant.MAESTRO_CARD_CVV;
            break;

        case CashierConstant.BAJAJ_CARD:
            this.expiryYear = CashierConstant.MAESTRO_CARD_EXPIRY_YEAR;
            this.expiryMonth = CashierConstant.MAESTRO_CARD_EXPIRY_MONTH;
            this.cvv = CashierConstant.MAESTRO_CARD_CVV;
            break;

        case CashierConstant.BAJAJFN:
            BeanParameterValidator.validateInputStringParam(expiryYear, "expiryYear");
            this.expiryYear = expiryYear;
            BeanParameterValidator.validateInputStringParam(expiryMonth, "expiryMonth");
            this.expiryMonth = expiryMonth;
            break;

        default:
            this.cvv = cvv;
            BeanParameterValidator.validateInputStringParam(expiryYear, "expiryYear");
            this.expiryYear = expiryYear;

            BeanParameterValidator.validateInputStringParam(expiryMonth, "expiryMonth");
            this.expiryMonth = expiryMonth;
        }
    }

    public BankCardRequest(String cardNo, String cvv, String expiryYear, String expiryMonth, String cardType,
            String cardScheme) throws CashierCheckedException, PaytmValidationException {
        this(cardNo, cvv, expiryYear, expiryMonth, cardScheme);

        BeanParameterValidator.validateInputStringParam(cardType, "cardType");
        this.cardType = cardType;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(final String cvv) {
        this.cvv = cvv;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public String getOtp() {
        return otp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BankCardRequest [cardNo=");
        builder.append("NA");
        builder.append(", cvv=");
        builder.append("NA");
        builder.append(", expiryYear=");
        builder.append(expiryYear);
        builder.append(", expiryMonth=");
        builder.append(expiryMonth);
        builder.append(", otp=");
        builder.append(otp);
        builder.append(", cardType=");
        builder.append(cardType);
        builder.append("]");
        return builder.toString();
    }
}