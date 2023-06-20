package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cashier.util.CashierUtilService;
import com.paytm.pgplus.pgproxycommon.exception.PaytmValidationException;
import com.paytm.pgplus.promo.service.client.model.PromoCodeData;
import com.paytm.pgplus.promo.service.client.model.PromoCodeResponse;
import com.paytm.pgplus.theia.models.TheiaPaymentRequest;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.sessiondata.LoginInfo;
import com.paytm.pgplus.theia.sessiondata.MerchantInfo;
import com.paytm.pgplus.theia.sessiondata.OAuthUserInfo;
import com.paytm.pgplus.theia.sessiondata.TransactionInfo;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TheiaPromoUtilTest extends AOAUtilsTest {

    @InjectMocks
    TheiaPromoUtil theiaPromoUtil;

    @Mock
    TheiaPaymentRequest theiaPaymentRequest;

    @Mock
    MerchantInfo merchantInfo;

    @Mock
    TransactionInfo txnInfo;

    @Mock
    LoginInfo loginInfo;

    @Mock
    PromoCodeResponse promoCodeResponse;

    @Mock
    OAuthUserInfo userInfo;

    @Mock
    CashierUtilService cashierUtilService;

    @Mock
    PromoCodeData promoCodeData;

    @Test
    public void testApplyPromocode() throws PaytmValidationException {
        when(txnInfo.getPromoCodeResponse()).thenReturn(promoCodeResponse);
        when(promoCodeResponse.getPromoResponseCode()).thenReturn("01");
        when(merchantInfo.getInternalMid()).thenReturn("test");
        when(txnInfo.getTxnAmount()).thenReturn("100");
        when(theiaPaymentRequest.getTxnMde()).thenReturn("test");
        when(txnInfo.getTxnId()).thenReturn("test");
        when(theiaPaymentRequest.getCardNo()).thenReturn("1234567890");
        when(loginInfo.getUser()).thenReturn(userInfo);
        when(userInfo.getUserID()).thenReturn("test");
        when(theiaPaymentRequest.getSavedCardId()).thenReturn("123456");
        when(cashierUtilService.getCardNumer(anyLong(), anyString())).thenReturn("1234567890");
        when(promoCodeResponse.getPromoCodeDetail()).thenReturn(promoCodeData);
        when(promoCodeData.getPromoCode()).thenReturn("HAPPY_TESTING");
        when(theiaPaymentRequest.getBankCode()).thenReturn("test");
        when(theiaPaymentRequest.getStoreCardFlag()).thenReturn("Y");
    }

}