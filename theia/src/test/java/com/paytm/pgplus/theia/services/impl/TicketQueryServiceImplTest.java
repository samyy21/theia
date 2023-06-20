package com.paytm.pgplus.theia.services.impl;

import com.paytm.pgplus.biz.mapping.models.MappingMerchantData;
import com.paytm.pgplus.facade.acquiring.models.TicketDetail;
import com.paytm.pgplus.facade.acquiring.models.response.TickQueryResponse;
import com.paytm.pgplus.facade.acquiring.models.response.TicketQueryResponseBody;
import com.paytm.pgplus.facade.acquiring.services.IAcquiringTicket;
import com.paytm.pgplus.facade.common.model.ResultInfo;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.pgproxycommon.models.GenericCoreResponseBean;
import com.paytm.pgplus.theia.cache.IMerchantMappingService;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TicketQueryServiceImplTest {

    @InjectMocks
    TicketQueryServiceImpl ticketQueryService;

    @Mock
    GenericCoreResponseBean<MappingMerchantData> genericCoreResponseBean;

    @Mock
    IMerchantMappingService merchantMappingService;

    @Mock
    MappingMerchantData mappingMerchantData;

    @Mock
    IAcquiringTicket acquiringTicket;

    @Mock
    TickQueryResponse tickQueryResponse;

    @Mock
    TicketQueryResponseBody queryResponseBody;

    @Mock
    ResultInfo resultInfo;

    @Mock
    TicketDetail ticketDetail;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFetchTicketQueryPRNWhenReturnNull() {
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(genericCoreResponseBean);
        when(genericCoreResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(genericCoreResponseBean.getResponse()).thenReturn(mappingMerchantData);
        when(mappingMerchantData.getAlipayId()).thenReturn("");
        Assert.assertNull(ticketQueryService.fetchTicketQueryPRN("test", "test"));
    }

    @Test
    public void testFetchTicketQueryPRNWhenReturnNotNUll() throws FacadeCheckedException {
        List<TicketDetail> testList = new ArrayList<>();
        testList.add(null);
        testList.add(new TicketDetail());
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(genericCoreResponseBean);
        when(genericCoreResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(genericCoreResponseBean.getResponse()).thenReturn(mappingMerchantData);
        when(mappingMerchantData.getAlipayId()).thenReturn("test");
        when(acquiringTicket.ticketQuery(any())).thenReturn(tickQueryResponse);
        when(tickQueryResponse.getBody()).thenReturn(queryResponseBody);
        when(queryResponseBody.getResultInfo()).thenReturn(resultInfo);
        when(resultInfo.getResultCode()).thenReturn("SUCCESS");
        when(queryResponseBody.getTicketDetails()).thenReturn(testList);
        Assert.assertEquals("", ticketQueryService.fetchTicketQueryPRN("test", "test"));

    }

    @Test
    public void testFetchTicketQueryPRNWhenReturnNotBlank() throws FacadeCheckedException {
        List<TicketDetail> testList = new ArrayList<>();
        testList.add(ticketDetail);
        testList.add(new TicketDetail());
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(genericCoreResponseBean);
        when(genericCoreResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(genericCoreResponseBean.getResponse()).thenReturn(mappingMerchantData);
        when(mappingMerchantData.getAlipayId()).thenReturn("test");
        when(acquiringTicket.ticketQuery(any())).thenReturn(tickQueryResponse);
        when(tickQueryResponse.getBody()).thenReturn(queryResponseBody);
        when(queryResponseBody.getResultInfo()).thenReturn(resultInfo);
        when(resultInfo.getResultCode()).thenReturn("SUCCESS");
        when(queryResponseBody.getTicketDetails()).thenReturn(testList);
        when(ticketDetail.getTicketType()).thenReturn("PRN");
        when(ticketDetail.getTicketValue()).thenReturn("test");
        String res = ticketQueryService.fetchTicketQueryPRN("test", "test");
        Assert.assertNotNull(res);
        // Assert.assertEquals("test",ticketQueryService.fetchTicketQueryPRN("test","test"));

    }

    @Test
    public void testFetchTicketQueryPRNWhenReturnBlank() throws FacadeCheckedException {
        List<TicketDetail> testList = new ArrayList<>();
        testList.add(ticketDetail);
        testList.add(new TicketDetail());
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(genericCoreResponseBean);
        when(genericCoreResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(genericCoreResponseBean.getResponse()).thenReturn(mappingMerchantData);
        when(mappingMerchantData.getAlipayId()).thenReturn("test");
        when(acquiringTicket.ticketQuery(any())).thenReturn(tickQueryResponse);
        when(tickQueryResponse.getBody()).thenReturn(queryResponseBody);
        when(queryResponseBody.getResultInfo()).thenReturn(resultInfo);
        when(resultInfo.getResultCode()).thenReturn("SUCCESS");
        when(queryResponseBody.getTicketDetails()).thenReturn(testList);
        when(ticketDetail.getTicketType()).thenReturn("test");
        when(ticketDetail.getTicketValue()).thenReturn("test");
        Assert.assertEquals("", ticketQueryService.fetchTicketQueryPRN("test", "test"));

    }

    @Test
    public void testFetchTicketQueryPRNWhenCatchesException() throws FacadeCheckedException {
        List<TicketDetail> testList = new ArrayList<>();
        testList.add(null);
        testList.add(new TicketDetail());
        when(merchantMappingService.fetchMerchanData(anyString())).thenReturn(genericCoreResponseBean);
        when(genericCoreResponseBean.isSuccessfullyProcessed()).thenReturn(true);
        when(genericCoreResponseBean.getResponse()).thenReturn(mappingMerchantData);
        when(mappingMerchantData.getAlipayId()).thenReturn("test");
        when(acquiringTicket.ticketQuery(any())).thenThrow(FacadeCheckedException.class);
        when(tickQueryResponse.getBody()).thenReturn(queryResponseBody);
        when(queryResponseBody.getResultInfo()).thenReturn(resultInfo);
        when(resultInfo.getResultCode()).thenReturn("SUCCESS");
        when(queryResponseBody.getTicketDetails()).thenReturn(testList);
        Assert.assertEquals("", ticketQueryService.fetchTicketQueryPRN("test", "test"));

    }

}