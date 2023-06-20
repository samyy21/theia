package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.theia.constants.TheiaConstant;
import com.paytm.pgplus.theia.exceptions.TheiaDataMappingException;
import com.paytm.pgplus.theia.nativ.model.common.CashierInfoContainerRequest;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;

/**
 * @createdOn 25-Jun-2021
 * @author kalluru nanda kishore
 */
public class UserLoggedInLitePayviewConsultServiceTest {

    @InjectMocks
    UserLoggedInLitePayviewConsultService userLoggedInLitePayviewConsultService;

    @Mock
    private IWorkFlow userLoggedInWorkflow;

    private CashierInfoContainerRequest cashierInfoContainerRequest;

    private CashierInfoRequest cashierInfoRequest;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        cashierInfoRequest = new CashierInfoRequest(new RequestHeader(new BaseHeader("M", "M", "M", "M")),
                new CashierInfoRequestBody());
        cashierInfoRequest.getBody().setChannelId(EChannelId.APP);
        cashierInfoRequest.getHead().setToken("test");
        cashierInfoRequest.getHead().setTokenType(TokenType.TXN_TOKEN);
        cashierInfoContainerRequest = new CashierInfoContainerRequest(cashierInfoRequest);
    }

    @Test
    public void testValidate() throws TheiaDataMappingException {

        exceptionRule.expect(RequestValidationException.class);
        cashierInfoRequest.getHead().setTokenType(TokenType.TXN_TOKEN);
        userLoggedInLitePayviewConsultService.validate(cashierInfoContainerRequest);

    }

    @Test
    public void makeBackwardCompatibleHttpServletRequest() {

        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        cashierInfoContainerRequest.getCashierInfoRequest().getHead().setMid("MID");
        CashierInfoRequestBody cashierInfoRequestBody = cashierInfoContainerRequest.getCashierInfoRequest().getBody();
        cashierInfoRequestBody.setIndustryTypeId("Industry");
        cashierInfoRequestBody.setOrderId("order1");
        cashierInfoRequestBody.setDeviceId("device");
        cashierInfoRequestBody.setOrderAmount(new Money());
        cashierInfoRequestBody.setCustId("customer1");
        cashierInfoRequestBody.setEmiOption("emi");
        cashierInfoRequestBody.setSubscriptionPaymentMode("cc");
        userLoggedInLitePayviewConsultService.makeBackwardCompatibleHttpServletRequest(httpServletRequest,
                cashierInfoRequest);
        assertEquals(cashierInfoRequest.getHead().getToken(),
                httpServletRequest.getAttribute(TheiaConstant.RequestParams.SSO_TOKEN));

    }

    @Test
    public void fetchWorkflow() {
        assertNotNull(userLoggedInLitePayviewConsultService.fetchWorkflow(new WorkFlowRequestBean()));
    }
}