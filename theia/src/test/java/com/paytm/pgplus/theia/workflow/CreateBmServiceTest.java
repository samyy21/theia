package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.cashier.pay.model.PaymentRequest;
import com.paytm.pgplus.common.enums.MandateMode;
import com.paytm.pgplus.mappingserviceclient.service.IBankInfoDataService;
import com.paytm.pgplus.payloadvault.subscription.response.*;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.theia.nativ.processor.MandateProcessor;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.MerchantResponseService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class CreateBmServiceTest {

    @InjectMocks
    CreateBmService createBmService;

    @Mock
    private MandateProcessor mandateProcessor;

    @Mock
    private IBankInfoDataService bankInfoDataService;

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Mock
    private MerchantResponseService merchantResponseService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void create() {

        PaymentRequestBean requestBean = new PaymentRequestBean();
        String mandateMode = MandateMode.E_MANDATE.getMappingName();
        when(mandateProcessor.createEMandate(any(), any(), any())).thenReturn(getMandateResponse());
        createBmService.create(requestBean, mandateMode);
        mandateMode = MandateMode.PAPER_MANDATE.getMappingName();
        when(merchantResponseService.getResponseForMandateMerchant(any(), any(), any(), any())).thenReturn(
                "merchantResponeUrl");
        PaperMandateCreateResponse paperMandateCreateResponse = new PaperMandateCreateResponse();
        paperMandateCreateResponse.setBody(new PaperMandateCreateResponseBody());
        paperMandateCreateResponse.getBody().setResultInfo(new ResultInfo());
        when(mandateProcessor.createPaperMandate(any(), any(), any())).thenReturn(paperMandateCreateResponse);
        createBmService.create(requestBean, mandateMode);
    }

    private MandateResponse getMandateResponse() {

        MandateResponse mandateResponse = new MandateResponse();
        mandateResponse.setMandateResponseBody(new MandateResponseBody());
        return mandateResponse;
    }

    @Test
    public void createBMForEnhancedFlow() {
        PaymentRequestBean requestBean = new PaymentRequestBean();
        String mandateMode = MandateMode.E_MANDATE.getMappingName();
        when(mandateProcessor.createEMandate(any(), any(), any())).thenReturn(getMandateResponse());
        createBmService.createBMForEnhancedFlow(requestBean);

    }

    @Test
    public void createBMForNativeJsonFlow() {

        PaymentRequestBean requestBean = new PaymentRequestBean();
        String mandateMode = MandateMode.E_MANDATE.getMappingName();
        when(mandateProcessor.createEMandate(any(), any(), any())).thenReturn(getMandateResponse());
        createBmService.createBMForNativeJsonFlow(requestBean);
    }
}