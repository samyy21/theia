package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.facade.acquiring.models.response.QueryByMerchantTransIdResponse;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringOrder;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.impl.MerchantMappingServiceImpl;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class AcquiringUtilTest extends AOAUtilsTest {

    @InjectMocks
    AcquiringUtil acquiringUtil;

    @Mock
    MerchantMappingServiceImpl merchantMappingService;

    @Mock
    GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse;

    @Mock
    MappingMerchantData mappingMerchantData;

    @Mock
    IAcquiringOrder acquiringOrder;

    @Test
    public void testQueryByMerchantTransIdResponse() throws FacadeCheckedException {
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(merchantMappingResponse);
        when(merchantMappingResponse.getResponse()).thenReturn(new MappingMerchantData());
        when(acquiringOrder.queryByMerchantTransId(any())).thenReturn(new QueryByMerchantTransIdResponse());
        when(mappingMerchantData.getAlipayId()).thenReturn("test");
        Assert.assertNotNull(acquiringUtil.queryByMerchantTransId("test", "test"));
    }

    @Test(expected = PaymentRequestValidationException.class)
    public void testQueryByMerchantTransIdResponseWhenThrowsException() throws FacadeCheckedException {
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(null);
        acquiringUtil.queryByMerchantTransId("test", "test");
    }
}