package com.paytm.pgplus.theia.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.user.models.request.GeneratePaymentOtpRequest;
import com.paytm.pgplus.facade.user.models.request.ValidatePaymentOtpRequest;
import com.paytm.pgplus.facade.user.models.response.GeneratePaymentOtpResponse;
import com.paytm.pgplus.facade.user.models.response.ValidatePaymentOtpResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.theia.cache.IConfigurationDataService;
import com.paytm.pgplus.theia.constants.TheiaConstant.MerchantPreference.PreferenceValues;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.services.ITheiaSessionDataService;
import com.paytm.pgplus.theia.sessiondata.ThemeInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;

/**
 * @author Santosh
 */

@Service
public class PaymentOTPService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOTPService.class);

    @Autowired
    @Qualifier("authenticationImpl")
    private IAuthentication authImpl;

    @Autowired
    @Qualifier("theiaSessionDataService")
    private ITheiaSessionDataService sessionDataService;

    @Autowired
    private MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Autowired
    @Qualifier("configurationDataService")
    private IConfigurationDataService configurationDataService;

    public void generateIfPaymentOTP(PaymentRequestBean requestData) throws PaytmValidationException {
        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(requestData.getRequest());
        if (isPaymentOTPRequired(requestData)) {
            boolean isOtpSentBefore = false;
            if (txnInfo.getSaveStateAgainstPaymentOTP() != null) {
                isOtpSentBefore = true;
            }
            GeneratePaymentOtpRequest paymentOTPData = getOTPRequest(requestData);
            try {
                GeneratePaymentOtpResponse generateOTPResponse = (authImpl.generatePaymentOtp(paymentOTPData));
                if (generateOTPResponse.isSuccessfullyProcessed()) {
                    txnInfo.setIsPaymentOTPRequired(PreferenceValues.PAYMENT_OTP_ENABLED);
                    if (!isOtpSentBefore) {
                        txnInfo.setMaxInvalidAttempts(PreferenceValues.INITIALIZE_INVALID_ATTEMPTS);
                        txnInfo.setPaymentOtpSendCount(PreferenceValues.INITIALIZE_RESEND_OTP);
                    }
                    txnInfo.setSaveStateAgainstPaymentOTP(generateOTPResponse.getCode());
                } else {
                    final WalletInfo walletInfo = sessionDataService.getWalletInfoFromSession(requestData.getRequest());
                    walletInfo.setWalletEnabled(false);
                    walletInfo.setWalletFailed(true);
                    walletInfo.setWalletInactive(true);
                    walletInfo.setWalletFailedMsg("Payment OTP not generated");
                    LOGGER.error("Payment OTP generation message:: {}", generateOTPResponse.getResponseMessage());
                    throw new PaytmValidationException("Wallet is not available right now. please try after sometime");
                }
            } catch (FacadeUncheckedException | FacadeCheckedException e) {
                LOGGER.error("Exception occured during generatePaymentOTP", e);
            }
        }
    }

    public boolean isPaymentOTPRequired(PaymentRequestBean requestData) {
        final StringBuilder themePath = new StringBuilder("");
        ThemeInfo themeInfo = sessionDataService.setAndGetThemeInfoInSesion(requestData.getRequest());
        if (themeInfo != null && StringUtils.isNotBlank(themeInfo.getTheme())) {
            themePath.append(themeInfo.getTheme());
        }
        return (isPaymentOTPConfigured(themePath.toString(), requestData.getMid()) && isBalanceSufficient(requestData));
    }

    private boolean isBalanceSufficient(PaymentRequestBean requestData) {
        final WalletInfo walletInfo = sessionDataService.getWalletInfoFromSession(requestData.getRequest());
        boolean isBalanceSufficient = false;
        if (walletInfo != null && walletInfo.isWalletEnabled()) {
            isBalanceSufficient = Double.parseDouble(requestData.getTxnAmount()) <= walletInfo.getWalletBalance();
        }
        if (!isBalanceSufficient) {
            LOGGER.info("Wallet balance not sufficient for Payment OTP");
        }
        return isBalanceSufficient;
    }

    private boolean isPaymentOTPConfigured(String theme, String mid) {
        String otpEnabledTheme = configurationDataService.getPaytmPropertyValue("payment.otp.enabled.themes");
        String otpEnabledMid = configurationDataService.getPaytmPropertyValue("payment.otp.enabled.merchants");
        if (!StringUtils.isBlank(otpEnabledTheme) && !StringUtils.isBlank(otpEnabledMid)) {
            List<String> listOTPEnabledTheme = Arrays.asList(otpEnabledTheme.split(","));
            List<String> listOTPEnabledMid = Arrays.asList(otpEnabledMid.split(","));
            boolean isThemeEnabled = listOTPEnabledTheme.contains(theme) && listOTPEnabledMid.contains(mid);
            return isThemeEnabled;
        }
        return false;
    }

    private GeneratePaymentOtpRequest getOTPRequest(PaymentRequestBean requestData) {
        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(requestData.getRequest());
        return new GeneratePaymentOtpRequest(txnInfo.getSsoToken(), merchantExtendInfoUtils.getMerchantName(requestData
                .getMid()));
    }

    public void validateIfPaymentOTP(PaymentRequestBean requestData) throws PaytmValidationException {
        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(requestData.getRequest());
        if (txnInfo.getIsPaymentOTPRequired()) {
            validatePaymentOTP(requestData, requestData.getOtp());
        }
    }

    private void validatePaymentOTP(PaymentRequestBean requestData, String otpSubmitted)
            throws PaytmValidationException {
        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(requestData.getRequest());
        String code = txnInfo.getSaveStateAgainstPaymentOTP();
        String ssoToken = txnInfo.getSsoToken();
        int maxInvalidAttempts = txnInfo.getMaxInvalidAttempts();
        if (StringUtils.isNotBlank(code) && StringUtils.isNumeric(otpSubmitted)) {
            ValidatePaymentOtpResponse ValidateOTPResponse = validateOTP(code, ssoToken, otpSubmitted);
            if (!ValidateOTPResponse.isOtpVerified()) {
                txnInfo.setMaxInvalidAttempts(maxInvalidAttempts + PreferenceValues.INCREMENT);
                if (maxInvalidAttempts > PreferenceValues.MAX_OTP_ATTEMPTS) {
                    final WalletInfo walletInfo = sessionDataService.getWalletInfoFromSession(requestData.getRequest());
                    walletInfo.setWalletEnabled(false);
                    walletInfo.setWalletInactive(true);
                    walletInfo.setWalletFailed(true);
                    walletInfo.setWalletFailedMsg("Maximum OTP Attempts reached");
                    throw new PaytmValidationException("Wallet inactive maximum OTP Attempts reached");
                }
                throw new PaytmValidationException("Please enter a valid OTP");
            }
        }
    }

    private ValidatePaymentOtpResponse validateOTP(String code, String ssoToken, String otpSubmitted) {
        ValidatePaymentOtpRequest ValidateOTP = new ValidatePaymentOtpRequest(Long.parseLong(otpSubmitted), code,
                ssoToken);
        ValidatePaymentOtpResponse ValidateOTPResponse = null;
        try {
            ValidateOTPResponse = authImpl.validatePaymentOtp(ValidateOTP);
        } catch (FacadeUncheckedException e) {
            LOGGER.error("Exception occured during validatePaymentOTP", e);
        } catch (FacadeCheckedException e) {
            LOGGER.error("Exception occured during validatePaymentOTP", e);
        }
        return ValidateOTPResponse;
    }

    public void resendPaymentOTP(PaymentRequestBean requestData) {
        final TransactionInfo txnInfo = sessionDataService.getTxnInfoFromSession(requestData.getRequest());

        GeneratePaymentOtpRequest paymentOTPData = getOTPRequest(requestData);
        try {
            GeneratePaymentOtpResponse generateOTPResponse = authImpl.generatePaymentOtp(paymentOTPData);

            if (generateOTPResponse.isSuccessfullyProcessed()) {
                txnInfo.setSaveStateAgainstPaymentOTP(generateOTPResponse.getCode());
            } else {
                LOGGER.debug("Payment OTP generation message:: {}", generateOTPResponse.getResponseMessage());
            }
        } catch (FacadeUncheckedException | FacadeCheckedException e) {
            LOGGER.error("Exception occured during generatePaymentOTP", e);
        }
    }

}