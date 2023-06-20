package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.user.models.response.GeneratePaymentOtpResponse;
import com.paytm.pgplus.facade.user.models.response.ValidatePaymentOtpResponse;
import com.paytm.pgplus.facade.user.services.IAuthentication;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.theia.cache.impl.ConfigurationDataServiceImpl;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.services.impl.TheiaSessionDataServiceImpl;
import com.paytm.pgplus.theia.sessiondata.ThemeInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PaymentOTPServiceTest extends AOAUtilsTest {

    @InjectMocks
    PaymentOTPService paymentOTPService;

    @Mock
    TransactionInfo transactionInfo;

    @Mock
    TheiaSessionDataServiceImpl theiaSessionDataService;

    @Mock
    PaymentRequestBean requestBean;

    @Mock
    ThemeInfo themeInfo;

    @Mock
    ConfigurationDataServiceImpl configurationDataService;

    @Mock
    WalletInfo walletInfo;

    @Mock
    MerchantExtendInfoUtils merchantExtendInfoUtils;

    @Mock
    IAuthentication authImpl;

    @Mock
    GeneratePaymentOtpResponse generateOTPResponse;

    @Mock
    ValidatePaymentOtpResponse validateOTPResponse;

    @Test
    public void testGenerateIfPaymentOTP() throws FacadeCheckedException, PaytmValidationException {
        when(requestBean.getMid()).thenReturn("test");
        when(requestBean.getTxnAmount()).thenReturn("10");
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(theiaSessionDataService.setAndGetThemeInfoInSesion(any())).thenReturn(themeInfo);
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(walletInfo.getWalletBalance()).thenReturn(100.00);
        when(themeInfo.getTheme()).thenReturn("test");
        when(transactionInfo.getSaveStateAgainstPaymentOTP()).thenReturn("test");
        when(transactionInfo.getSsoToken()).thenReturn("test");
        when(merchantExtendInfoUtils.getMerchantName(anyString())).thenReturn("test");
        when(authImpl.generatePaymentOtp(any())).thenReturn(generateOTPResponse);
        when(generateOTPResponse.isSuccessfullyProcessed()).thenReturn(true);
        paymentOTPService.generateIfPaymentOTP(requestBean);
        verify(theiaSessionDataService, atMost(2)).getTxnInfoFromSession(any());
    }

    @Test
    public void testGenerateIfPaymentOTPWhenIsOtpSentBeforeFalse() throws FacadeCheckedException,
            PaytmValidationException {
        when(requestBean.getMid()).thenReturn("test");
        when(requestBean.getTxnAmount()).thenReturn("10");
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(theiaSessionDataService.setAndGetThemeInfoInSesion(any())).thenReturn(themeInfo);
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(walletInfo.getWalletBalance()).thenReturn(100.00);
        when(themeInfo.getTheme()).thenReturn("test");
        when(transactionInfo.getSaveStateAgainstPaymentOTP()).thenReturn(null);
        when(transactionInfo.getSsoToken()).thenReturn("test");
        when(merchantExtendInfoUtils.getMerchantName(anyString())).thenReturn("test");
        when(authImpl.generatePaymentOtp(any())).thenReturn(generateOTPResponse);
        when(generateOTPResponse.isSuccessfullyProcessed()).thenReturn(true);
        paymentOTPService.generateIfPaymentOTP(requestBean);
        verify(theiaSessionDataService, atMost(2)).getTxnInfoFromSession(any());
    }

    @Test(expected = PaytmValidationException.class)
    public void testGenerateIfPaymentOTPWhenThrowsPaytmValidationException() throws FacadeCheckedException,
            PaytmValidationException {
        when(requestBean.getMid()).thenReturn("test");
        when(requestBean.getTxnAmount()).thenReturn("10");
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(theiaSessionDataService.setAndGetThemeInfoInSesion(any())).thenReturn(themeInfo);
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(walletInfo.getWalletBalance()).thenReturn(100.00);
        when(themeInfo.getTheme()).thenReturn("test");
        when(transactionInfo.getSaveStateAgainstPaymentOTP()).thenReturn(null);
        when(transactionInfo.getSsoToken()).thenReturn("test");
        when(merchantExtendInfoUtils.getMerchantName(anyString())).thenReturn("test");
        when(authImpl.generatePaymentOtp(any())).thenReturn(generateOTPResponse);
        when(generateOTPResponse.isSuccessfullyProcessed()).thenReturn(false);
        paymentOTPService.generateIfPaymentOTP(requestBean);
        verify(theiaSessionDataService, atMost(2)).getTxnInfoFromSession(any());
    }

    @Test
    public void testGenerateIfPaymentOTPWhenThrowsFacadeCheckedException() throws FacadeCheckedException,
            PaytmValidationException {
        when(requestBean.getMid()).thenReturn("test");
        when(requestBean.getTxnAmount()).thenReturn("10");
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(theiaSessionDataService.setAndGetThemeInfoInSesion(any())).thenReturn(themeInfo);
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(configurationDataService.getPaytmPropertyValue(anyString())).thenReturn("test");
        when(walletInfo.isWalletEnabled()).thenReturn(true);
        when(walletInfo.getWalletBalance()).thenReturn(100.00);
        when(themeInfo.getTheme()).thenReturn("test");
        when(transactionInfo.getSaveStateAgainstPaymentOTP()).thenReturn(null);
        when(transactionInfo.getSsoToken()).thenReturn("test");
        when(merchantExtendInfoUtils.getMerchantName(anyString())).thenReturn("test");

        when(authImpl.generatePaymentOtp(any())).thenThrow(FacadeCheckedException.class);
        when(generateOTPResponse.isSuccessfullyProcessed()).thenReturn(false);
        paymentOTPService.generateIfPaymentOTP(requestBean);
        verify(theiaSessionDataService, atMost(2)).getTxnInfoFromSession(any());
    }

    @Test(expected = PaytmValidationException.class)
    public void testValidateIfPaymentOTP() throws FacadeCheckedException, PaytmValidationException {
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(transactionInfo.getIsPaymentOTPRequired()).thenReturn(true);
        when(requestBean.getOtp()).thenReturn("123456");
        when(transactionInfo.getSaveStateAgainstPaymentOTP()).thenReturn("test");
        when(transactionInfo.getSsoToken()).thenReturn("test");
        when(transactionInfo.getMaxInvalidAttempts()).thenReturn(4);
        when(authImpl.validatePaymentOtp(any())).thenReturn(validateOTPResponse);
        when(validateOTPResponse.isOtpVerified()).thenReturn(false);
        paymentOTPService.validateIfPaymentOTP(requestBean);

    }

    @Test(expected = PaytmValidationException.class)
    public void testValidateIfPaymentOTPThrowsPaytmValidationException() throws FacadeCheckedException,
            PaytmValidationException {
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(theiaSessionDataService.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(transactionInfo.getIsPaymentOTPRequired()).thenReturn(true);
        when(requestBean.getOtp()).thenReturn("123456");
        when(transactionInfo.getSaveStateAgainstPaymentOTP()).thenReturn("test");
        when(transactionInfo.getSsoToken()).thenReturn("test");
        when(transactionInfo.getMaxInvalidAttempts()).thenReturn(1);
        when(authImpl.validatePaymentOtp(any())).thenReturn(validateOTPResponse);
        when(validateOTPResponse.isOtpVerified()).thenReturn(false);
        paymentOTPService.validateIfPaymentOTP(requestBean);

    }

    @Test
    public void testResendPaymentOTP() throws FacadeCheckedException {
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(requestBean.getSsoToken()).thenReturn("test");
        when(requestBean.getMid()).thenReturn("test");
        when(merchantExtendInfoUtils.getMerchantName(anyString())).thenReturn("test");
        when(authImpl.generatePaymentOtp(any())).thenReturn(generateOTPResponse);
        when(generateOTPResponse.isSuccessfullyProcessed()).thenReturn(true);
        paymentOTPService.resendPaymentOTP(requestBean);
        verify(theiaSessionDataService, atMost(2)).getTxnInfoFromSession(any());
    }

    @Test
    public void testResendPaymentOTPWhenIsSuccessfullyProcessedReturnFalse() throws FacadeCheckedException {
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(requestBean.getSsoToken()).thenReturn("test");
        when(requestBean.getMid()).thenReturn("test");
        when(merchantExtendInfoUtils.getMerchantName(anyString())).thenReturn("test");
        when(authImpl.generatePaymentOtp(any())).thenReturn(generateOTPResponse);
        when(generateOTPResponse.isSuccessfullyProcessed()).thenReturn(false);
        paymentOTPService.resendPaymentOTP(requestBean);
        verify(theiaSessionDataService, atMost(2)).getTxnInfoFromSession(any());
    }

    @Test
    public void testResendPaymentOTPWhenThrowsFacadeCheckedException() throws FacadeCheckedException {
        when(theiaSessionDataService.getTxnInfoFromSession(any())).thenReturn(transactionInfo);
        when(requestBean.getSsoToken()).thenReturn("test");
        when(requestBean.getMid()).thenReturn("test");
        when(merchantExtendInfoUtils.getMerchantName(anyString())).thenReturn("test");
        when(authImpl.generatePaymentOtp(any())).thenThrow(FacadeCheckedException.class);

        paymentOTPService.resendPaymentOTP(requestBean);
        verify(theiaSessionDataService, atMost(2)).getTxnInfoFromSession(any());
    }

}