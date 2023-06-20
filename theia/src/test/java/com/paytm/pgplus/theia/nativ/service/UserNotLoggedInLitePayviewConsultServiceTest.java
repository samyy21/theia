package com.paytm.pgplus.theia.nativ.service;

import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.biz.workflow.service.IWorkFlow;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.models.Money;
import com.paytm.pgplus.theia.nativ.model.common.CashierInfoContainerRequest;
import com.paytm.pgplus.theia.offline.enums.ResultCode;
import com.paytm.pgplus.theia.offline.enums.TokenType;
import com.paytm.pgplus.theia.offline.exceptions.RequestValidationException;
import com.paytm.pgplus.theia.offline.facade.ICommonFacade;
import com.paytm.pgplus.theia.offline.model.base.BaseHeader;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequest;
import com.paytm.pgplus.theia.offline.model.request.CashierInfoRequestBody;
import com.paytm.pgplus.theia.offline.model.request.RequestHeader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;

import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.INDUSTRY_TYPE_ID;
import static com.paytm.pgplus.theia.constants.TheiaConstant.RequestParams.MID;
import static org.junit.Assert.*;

/**
 * @createdOn 25-Jun-2021
 * @author kalluru nanda kishore
 */
public class UserNotLoggedInLitePayviewConsultServiceTest {

    @InjectMocks
    UserNotLoggedInLitePayviewConsultService userNotLoggedInLitePayviewConsultService;

    @Mock
    private IWorkFlow userNotLoggedInWorkflow;

    @Mock
    private ICommonFacade commonFacade;

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
    public void validate() {
        cashierInfoContainerRequest.getCashierInfoRequest().setBody(null);
        exceptionRule.expect(RequestValidationException.class);
        exceptionRule.expectMessage(ResultCode.REQUEST_PARAMS_VALIDATION_EXCEPTION.getResultMsg());
        userNotLoggedInLitePayviewConsultService.validate(cashierInfoContainerRequest);
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
        userNotLoggedInLitePayviewConsultService.makeBackwardCompatibleHttpServletRequest(httpServletRequest,
                cashierInfoContainerRequest.getCashierInfoRequest());
        assertEquals(cashierInfoRequestBody.getIndustryTypeId(), httpServletRequest.getAttribute(INDUSTRY_TYPE_ID));
        assertEquals(cashierInfoContainerRequest.getCashierInfoRequest().getHead().getMid(),
                httpServletRequest.getAttribute(MID));
    }

    @Test
    public void fetchWorkflow() {

        assertNotNull(userNotLoggedInLitePayviewConsultService.fetchWorkflow(new WorkFlowRequestBean()));
    }
}