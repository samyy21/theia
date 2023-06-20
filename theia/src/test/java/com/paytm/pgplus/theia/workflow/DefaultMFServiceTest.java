package com.paytm.pgplus.theia.workflow;

import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theia.enums.ValidationResults;
import com.paytm.pgplus.theia.models.response.PageDetailsResponse;
import com.paytm.pgplus.theia.services.IPaymentService;
import com.paytm.pgplus.theia.services.ITheiaViewResolverService;
import com.paytm.pgplus.theia.utils.TheiaResponseGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultMFServiceTest {

    @InjectMocks
    DefaultMFService defaultMFService;

    @Mock
    private IPaymentService defaultMFPaymentService;

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Mock
    private TheiaResponseGenerator theiaResponseGenerator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processDefaultMFRequest() {
        PaymentRequestBean paymentRequestData = new PaymentRequestBean();
        Model model = mock(Model.class);
        when(defaultMFPaymentService.validatePaymentRequest(paymentRequestData)).thenReturn(
                ValidationResults.INVALID_REQUEST, ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                ValidationResults.UNKNOWN_VALIDATION_FAILURE, ValidationResults.VALIDATION_SUCCESS,
                ValidationResults.VALIDATION_SUCCESS);
        when(theiaResponseGenerator.getPageDetailsResponse(paymentRequestData, ResponseConstants.INVALID_CHECKSUM))
                .thenReturn(new PageDetailsResponse());
        when(theiaViewResolverService.returnOOPSPage(paymentRequestData.getRequest())).thenReturn("OOPS");
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse(true);
        pageDetailsResponse.setHtmlPage("htmlPage");
        when(defaultMFPaymentService.processPaymentRequest(paymentRequestData, model)).thenReturn(pageDetailsResponse);
        when(theiaViewResolverService.returnPaymentPage(paymentRequestData.getRequest())).thenReturn("success");
        assertNotNull(defaultMFService.processDefaultMFRequest(paymentRequestData, model));
        assertNotNull(defaultMFService.processDefaultMFRequest(paymentRequestData, model));
        assertNotNull(defaultMFService.processDefaultMFRequest(paymentRequestData, model));
        assertNotNull(defaultMFService.processDefaultMFRequest(paymentRequestData, model));
        pageDetailsResponse.setSuccessfullyProcessed(false);
        when(defaultMFPaymentService.processPaymentRequest(paymentRequestData, model)).thenReturn(pageDetailsResponse);
        assertNotNull(defaultMFService.processDefaultMFRequest(paymentRequestData, model));

    }
}