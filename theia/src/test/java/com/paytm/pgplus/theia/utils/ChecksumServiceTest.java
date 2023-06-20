package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.API;
import com.paytm.pgplus.payloadvault.dynamicwrapper.enums.PayloadType;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ChecksumServiceTest extends AOAUtilsTest {

    @InjectMocks
    ChecksumService checksumService;

    @Mock
    DynamicWrapperUtil dynamicWrapperUtil;

    @Mock
    MerchantPreferenceProvider merchantPreferenceProvider;

    @Mock
    PaymentRequestBean paymentRequestBean;

    @Test
    public void testValidateChecksum() {
        when(dynamicWrapperUtil.isDynamicWrapperEnabled()).thenReturn(true);
        when(dynamicWrapperUtil.isDynamicWrapperConfigPresent("test", API.REFUND, PayloadType.REQUEST))
                .thenReturn(true);
        when(paymentRequestBean.isMerchantVerifiedChecksum()).thenReturn(true);
        when(paymentRequestBean.isChecksumVerificationResult()).thenReturn(true);
        Assert.assertTrue(checksumService.validateChecksum(paymentRequestBean));

    }

}