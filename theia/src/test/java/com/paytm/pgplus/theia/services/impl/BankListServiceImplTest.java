package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.core.model.request.LitePayviewConsultResponseBizBean;
import com.paytm.pgplus.biz.core.model.request.PayChannelOptionViewBiz;
import com.paytm.pgplus.biz.core.model.request.PayMethodViewsBiz;
import com.paytm.pgplus.biz.core.payment.service.IBizPaymentService;
import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.common.model.EnvInfoRequestBean;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.exceptions.PaymentRequestValidationException;
import com.paytm.pgplus.theia.models.BankListRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class BankListServiceImplTest {

    // @InjectMocks
    // BankListServiceImpl bankListService;

    @Mock
    BankListRequest bankListRequest;

    @Mock
    GenericCoreResponseBean<MappingMerchantData> merchantMappingResponse;

    @Mock
    IMerchantMappingService merchantMappingService;

    @Mock
    MappingMerchantData mappingMerchantData;

    @Mock
    EnvInfoRequestBean envInfoRequestBean;

    @Mock
    IBizPaymentService bizPaymentService;

    @Mock
    GenericCoreResponseBean<LitePayviewConsultResponseBizBean> responseBean;

    @Mock
    LitePayviewConsultResponseBizBean payviewConsultResponseBizBean;

    @Mock
    PayMethodViewsBiz payMethodViewsBiz;

    @Mock
    PayChannelOptionViewBiz payChannelOptionViewBiz;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFetchAvailableBankList() {
        List<PayMethodViewsBiz> testLIst = new ArrayList<>();
        testLIst.add(payMethodViewsBiz);

        List<PayChannelOptionViewBiz> testListChannel = new ArrayList<>();
        testListChannel.add(payChannelOptionViewBiz);

        when(bankListRequest.getMid()).thenReturn("test");
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(merchantMappingResponse);
        when(merchantMappingResponse.getResponse()).thenReturn(mappingMerchantData);
        when(mappingMerchantData.getAlipayId()).thenReturn("test");
        when(bizPaymentService.litePayviewConsult(any())).thenReturn(responseBean);
        when(responseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(responseBean.getResponse()).thenReturn(payviewConsultResponseBizBean);
        when(payviewConsultResponseBizBean.getPayMethodViews()).thenReturn(testLIst);
        when(payMethodViewsBiz.getPayMethod()).thenReturn("NET_BANKING");
        when(payMethodViewsBiz.getPayChannelOptionViews()).thenReturn(testListChannel);
        when(payChannelOptionViewBiz.isEnableStatus()).thenReturn(true);
        when(payChannelOptionViewBiz.getInstId()).thenReturn("test");
        when(payChannelOptionViewBiz.getInstName()).thenReturn("test");
        Assert.assertNotNull(bankListService.fetchAvailableBankList(bankListRequest, envInfoRequestBean));

    }

    @Test(expected = PaymentRequestValidationException.class)
    public void testFetchAvailableBankListWhenThrowsException() {
        when(bankListRequest.getMid()).thenReturn("test");
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(null);
        when(merchantMappingResponse.getFailureMessage()).thenReturn("test");
        bankListService.fetchAvailableBankList(bankListRequest, envInfoRequestBean);
    }

    @Test
    public void testFetchAvailableBankListReturnsEmptyList() {
        when(bankListRequest.getMid()).thenReturn("test");
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(merchantMappingResponse);
        when(merchantMappingResponse.getResponse()).thenReturn(mappingMerchantData);
        when(mappingMerchantData.getAlipayId()).thenReturn("test");
        when(bizPaymentService.litePayviewConsult(any())).thenReturn(responseBean);
        when(responseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(responseBean.getResponse()).thenReturn(null);

        Assert.assertNotNull(bankListService.fetchAvailableBankList(bankListRequest, envInfoRequestBean));

    }

}