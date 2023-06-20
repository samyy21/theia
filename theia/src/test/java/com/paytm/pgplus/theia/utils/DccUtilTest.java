package com.paytm.pgplus.theia.utils;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.common.model.DccPaymentDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.cache.IMerchantPreferenceService;
import com.paytm.pgplus.theia.nativ.model.common.DccPaymentDetailRequest;
import com.paytm.pgplus.theia.nativ.processor.IRequestProcessor;
import com.paytm.pgplus.theia.nativ.processor.factory.RequestProcessorFactory;
import com.paytm.pgplus.theia.nativ.utils.AOAUtilsTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DccUtilTest extends AOAUtilsTest {

    @InjectMocks
    DccUtil dccUtil;

    @Mock
    IRequestProcessor<DccPaymentDetailRequest, DccPaymentDetail> dccFetchRatesRequestProcessor;

    @Mock
    RequestProcessorFactory requestProcessorFactory;

    @Mock
    private IMerchantPreferenceService merchantPreferenceService;

    @Test
    public void testFetchDccRatesFromInsta() throws Exception {
        when(requestProcessorFactory.getRequestProcessor(RequestProcessorFactory.RequestType.FETCH_DCC_RATES))
                .thenReturn(dccFetchRatesRequestProcessor);
        when(dccFetchRatesRequestProcessor.process(any())).thenReturn(new DccPaymentDetail());
        PaymentRequestBean requestData = new PaymentRequestBean();
        WorkFlowRequestBean flowRequestBean = new WorkFlowRequestBean();
        flowRequestBean.setCardNo("1234567890");
        flowRequestBean.setDccServiceInstId("test");
        requestData.setTxnAmount("123");
        requestData.setMid("test");
        requestData.setOrderId("test");
        requestData.setPaymentTypeId("test");
        Assert.assertNotNull(dccUtil.fetchDccRatesFromInsta(requestData, flowRequestBean));

    }

}