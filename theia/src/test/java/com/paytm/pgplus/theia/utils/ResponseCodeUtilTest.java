package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.cache.model.MerchantBussinessLogoInfo;
import com.paytm.pgplus.cache.model.MessageAndRetryDetails;
import com.paytm.pgplus.cache.model.ResponseCodeDetails;
import com.paytm.pgplus.common.responsecode.enums.SystemResponseCode;
import com.paytm.pgplus.payloadvault.theia.enums.ButtonAction;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.responsecode.utils.MerchantResponseUtil;
import com.paytm.pgplus.theia.nativ.service.MerchantInfoService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.services.helper.RetryServiceHelper;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ResponseCodeUtilTest extends AOAUtilsTest {
    @InjectMocks
    ResponseCodeUtil responseCodeUtil;

    @Mock
    TransactionResponse transactionResponse;

    @Mock
    MerchantResponseUtil merchantResponseUtil;

    @Mock
    ResponseCodeDetails responseCodeDetails;

    @Mock
    MessageAndRetryDetails messageAndRetryDetails;

    @Mock
    RetryServiceHelper retryServiceHelper;

    @Mock
    MerchantInfoService merchantInfoService;

    @Mock
    MerchantBussinessLogoInfo merchantDetailsResponse;

    @Mock
    ICommonFacade commonFacade;

    @Test
    public void testSetRespMsgeAndCode() {
        when(transactionResponse.getTransactionStatus()).thenReturn("test");
        when(transactionResponse.getMid()).thenReturn("test");
        when(merchantResponseUtil.getMerchantRespCodeDetails(any())).thenReturn(responseCodeDetails);
        when(responseCodeDetails.getResponseCode()).thenReturn("test");
        when(responseCodeDetails.getDisplayMessage()).thenReturn("");
        when(responseCodeDetails.getRemark()).thenReturn("test");
        when(responseCodeDetails.getMessageAndRetryDetails()).thenReturn(messageAndRetryDetails);
        when(retryServiceHelper.checkNativePaymentRetry(anyString(), anyString())).thenReturn(true);
        when(merchantInfoService.getMerchantInfo(any())).thenReturn(merchantDetailsResponse);
        when(commonFacade.getLogoName(anyString())).thenReturn("test");
        when(merchantDetailsResponse.getMerchantDisplayName()).thenReturn("test");
        when(merchantDetailsResponse.getMerchantImageName()).thenReturn("test");
        when(messageAndRetryDetails.getHeaderMessage()).thenReturn("test");
        when(messageAndRetryDetails.getBlockerMessage()).thenReturn("test");
        when(messageAndRetryDetails.getBodyMessage()).thenReturn("test");
        when(messageAndRetryDetails.isPopupEnable()).thenReturn(true);
        when(messageAndRetryDetails.getFailureType()).thenReturn("BLOCK");
        when(messageAndRetryDetails.getProceedButtonAction()).thenReturn("PROCEED_TO_PAY");
        when(messageAndRetryDetails.getProceedButtonText()).thenReturn("test");
        when(messageAndRetryDetails.getBackButtonAction()).thenReturn("BACK_TO_CASHIER");
        when(messageAndRetryDetails.getBackButtonText()).thenReturn("test");
        responseCodeUtil.setRespMsgeAndCode(transactionResponse, "test", SystemResponseCode.SUCCESS_RESPONSE_CODE);
        verify(commonFacade, atMost(1)).getLogoName(anyString());
    }

    @Test
    public void testGetResponseCodeDetails() {
        when(merchantResponseUtil.getMerchantRespCodeDetails(any())).thenReturn(responseCodeDetails);
        Assert.assertNotNull(responseCodeUtil.getResponseCodeDetails(SystemResponseCode.SUCCESS_RESPONSE_CODE));
    }

}