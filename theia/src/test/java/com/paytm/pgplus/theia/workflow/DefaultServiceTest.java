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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultServiceTest {

    @InjectMocks
    private DefaultService defaultService;

    @Mock
    private IPaymentService defaultPaymentService;

    @Mock
    private ITheiaViewResolverService theiaViewResolverService;

    @Mock
    private TheiaResponseGenerator theiaResponseGenerator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processDefaultRequest() {

        PaymentRequestBean paymentRequestData = new PaymentRequestBean();
        Model model = mock(Model.class);
        when(defaultPaymentService.validatePaymentRequest(paymentRequestData)).thenReturn(
                ValidationResults.INVALID_REQUEST, ValidationResults.CHECKSUM_VALIDATION_FAILURE,
                ValidationResults.UNKNOWN_VALIDATION_FAILURE, ValidationResults.MERCHANT_SPECIFIC_VALIDATION_FAILURE,
                ValidationResults.VALIDATION_SUCCESS, ValidationResults.VALIDATION_SUCCESS);
        when(theiaResponseGenerator.getPageDetailsResponse(paymentRequestData, ResponseConstants.INVALID_CHECKSUM))
                .thenReturn(new PageDetailsResponse());
        when(theiaViewResolverService.returnOOPSPage(paymentRequestData.getRequest())).thenReturn("OOPS");
        when(theiaViewResolverService.returnOOPSPage(paymentRequestData.getRequest())).thenReturn("OOPS");
        PageDetailsResponse pageDetailsResponse = new PageDetailsResponse(true);
        pageDetailsResponse.setHtmlPage("htmlPage");
        when(defaultPaymentService.processPaymentRequest(paymentRequestData, model)).thenReturn(pageDetailsResponse);
        when(theiaViewResolverService.returnPaymentPage(paymentRequestData.getRequest())).thenReturn("success");
        assertNotNull(defaultService.processDefaultRequest(paymentRequestData, model));
        assertNotNull(defaultService.processDefaultRequest(paymentRequestData, model));
        assertNotNull(defaultService.processDefaultRequest(paymentRequestData, model));
        assertNotNull(defaultService.processDefaultRequest(paymentRequestData, model));
        assertNotNull(defaultService.processDefaultRequest(paymentRequestData, model));
        pageDetailsResponse.setSuccessfullyProcessed(false);
        when(defaultPaymentService.processPaymentRequest(paymentRequestData, model)).thenReturn(pageDetailsResponse);
        assertNotNull(defaultService.processDefaultRequest(paymentRequestData, model));

    }
}