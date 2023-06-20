package com.paytm.pgplus.theia.nativ.utils;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.sessiondata.WalletInfo;
import org.junit.Assert;
import org.mockito.Mock;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class NativeCODValidationServiceTest extends AOAUtilsTest {

    @InjectMocks
    NativeCODValidationService nativeCODValidationService;

    @Mock
    TheiaSessionDataServiceAdapterNative theiaSessionDataServiceAdapterNative;

    @Mock
    NativeCODUtils nativeCODUtils;

    @Mock
    WalletInfo walletInfo;

    @Test
    public void testIsValidPaymentFLowAndMode() {
        when(theiaSessionDataServiceAdapterNative.getWalletInfoFromSession(any())).thenReturn(walletInfo);
        when(nativeCODUtils.getMinimumCodAmount()).thenReturn("100");
        when(walletInfo.getWalletBalance()).thenReturn(150.0);
        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setIsAddMoney("0");
        paymentRequestBean.setTxnAmount("200");
        Assert.assertFalse(nativeCODValidationService.isValidPaymentFlowAndMode(paymentRequestBean));

    }

    @Test
    public void testIsValidPaymentFLowAndModeWhenReturnTrue() {

        PaymentRequestBean paymentRequestBean = new PaymentRequestBean();
        paymentRequestBean.setIsAddMoney("a");

        Assert.assertTrue(nativeCODValidationService.isValidPaymentFlowAndMode(paymentRequestBean));

    }

}