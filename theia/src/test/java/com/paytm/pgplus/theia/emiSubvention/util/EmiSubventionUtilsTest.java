package com.paytm.pgplus.theia.emiSubvention.util;

import com.paytm.pgplus.theia.accesstoken.util.AccessTokenUtils;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.banks.EmiBanksRequestBody;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.tenures.EmiTenuresRequestBody;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequest;
import com.paytm.pgplus.theia.emiSubvention.model.request.validate.ValidateEmiRequestBody;
import com.paytm.pgplus.theia.nativ.model.common.TokenRequestHeader;
import com.paytm.pgplus.theia.nativ.service.MockHttpServletRequest;
import com.paytm.pgplus.theia.paymentoffer.util.PaymentOfferUtils;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

public class EmiSubventionUtilsTest {

    @InjectMocks
    private EmiSubventionUtils emiSubventionUtils = new EmiSubventionUtils();

    @Mock
    private AccessTokenUtils accessTokenUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOfferUtils.class);
    private static final String REQUEST_HEADER_KEY = "REQUEST_HEADER_EMI_SUBVENTION";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testSetParamsForBanksRequest() {
        EmiBanksRequest request = new EmiBanksRequest();
        request.setBody(null);
        emiSubventionUtils.setParamsForBanksRequest(request, "12345678");

        EmiBanksRequest request1 = new EmiBanksRequest();
        EmiBanksRequestBody body = new EmiBanksRequestBody();
        body.setMid("mid");
        request1.setBody(body);
        emiSubventionUtils.setParamsForBanksRequest(request1, "12345678");
    }

    @Test
    public void testSetParamsForTenureRequest() {
        EmiTenuresRequest request = new EmiTenuresRequest();
        request.setBody(null);
        emiSubventionUtils.setParamsForTenureRequest(request, "123456");

        EmiTenuresRequest request1 = new EmiTenuresRequest();
        EmiTenuresRequestBody body = new EmiTenuresRequestBody();
        body.setCustomerId("custId");
        request1.setBody(body);
        emiSubventionUtils.setParamsForTenureRequest(request1, "123456");
    }

    @Test
    public void testSetParamsForValidateRequest() {
        ValidateEmiRequest request = new ValidateEmiRequest();
        request.setBody(null);
        emiSubventionUtils.setParamsForValidateRequest(request, "123456789");

        ValidateEmiRequest request1 = new ValidateEmiRequest();
        ValidateEmiRequestBody body = new ValidateEmiRequestBody();
        body.setOrderId("orderId");
        request1.setBody(body);
        emiSubventionUtils.setParamsForValidateRequest(request1, "123456789");
    }

    @Test
    public void testValidateAccessToken() {
        emiSubventionUtils.validateAccessToken("mid", "referenceId", "token");
    }

    @Test
    public void testCreateResponseHeader() {
        TokenRequestHeader requestHeader = new TokenRequestHeader();
        requestHeader.setVersion("version");
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        RequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes, true);
        EmiSubventionUtils.setRequestHeader(requestHeader);
        EmiSubventionUtils.createResponseHeader();
    }

    @Test
    public void testCreateResponseHeaderWhenRequestHeaderNUll() {
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        RequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes, true);
        EmiSubventionUtils.createResponseHeader();
    }

    @Test
    public void testCreateResponseHeader1() {
        TokenRequestHeader requestHeader = new TokenRequestHeader();
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        RequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes, true);
        requestHeader.setRequestId("requestId");
        EmiSubventionUtils.setRequestHeader(requestHeader);
        EmiSubventionUtils.createResponseHeader();
    }
}