package com.paytm.pgplus.cashier.validator.service.impl;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paytm.pgplus.cashier.constant.PGPlusConstants;
import com.paytm.pgplus.cashier.exception.CashierCheckedException;
import com.paytm.pgplus.cashier.facade.service.IFacadeService;
import com.paytm.pgplus.cashier.models.CommonParams;
import com.paytm.pgplus.cashier.validator.model.PaymentOTP;
import com.paytm.pgplus.cashier.validator.service.IPaymentOTPValidator;

/**
 * @author amit.dubey
 *
 */
@Component("paymentValidatorImpl")
public class PaymentOTPValidatorImpl implements IPaymentOTPValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOTPValidatorImpl.class);

    @Autowired
    IFacadeService facadeService;

    @Override
    public boolean validate(PaymentOTP paymentOTP) throws CashierCheckedException {
        if (isPaymentOTPRequired(paymentOTP) && PGPlusConstants.PPI.equals(paymentOTP.getTxnMode())) {
            if (!validatePayment(paymentOTP)) {
                LOGGER.warn("Payment OTP Verification Failed!");
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isPaymentOTPRequired(final PaymentOTP paymentOTP) {
        return isPaymentOTPConfigured(paymentOTP.getTheme(), paymentOTP.getMid())
                && isBalanceSufficient(paymentOTP.getWalletBalance(), paymentOTP.getTxnAmt());
    }

    private boolean isBalanceSufficient(final Double walletBalance, final BigDecimal txnAmt) {
        if (null != walletBalance) {
            return txnAmt.doubleValue() <= walletBalance;
        }
        return false;
    }

    private boolean isPaymentOTPConfigured(final String theme, final String mid) {
        return CommonParams.getInstance().isThemeApplicableForPaymentOTP(theme)
                && CommonParams.getInstance().isMerchantApplicableForPaymentOTP(mid);
    }

    public boolean validatePayment(final PaymentOTP paymentOTP) throws CashierCheckedException {
        if (StringUtils.isNotBlank(paymentOTP.getCode())) {
            return validatePaymentOTP(paymentOTP.getCode(), paymentOTP.getToken(), paymentOTP.getOtp());
        }
        return false;
    }

    private boolean validatePaymentOTP(final String code, final String token, final Long otp)
            throws CashierCheckedException {
        return facadeService.validatePaymentOTP(code, token, otp);
    }

}
